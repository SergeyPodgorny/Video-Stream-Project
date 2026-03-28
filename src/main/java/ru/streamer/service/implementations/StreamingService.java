package ru.streamer.service.implementations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.streamer.playlist.impl.PlayListService;
import ru.streamer.service.FfmpegService;
import ru.streamer.service.VideoProvider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class StreamingService implements VideoProvider {

    private static final Logger log = LoggerFactory.getLogger(StreamingService.class);
    private final PlayListService playListService;
    private final FfmpegService ffmpegService;

    public StreamingService(PlayListService playListService, FfmpegService ffmpegService) {
        this.playListService = playListService;
        this.ffmpegService = ffmpegService;
    }

    @Override
    public Mono<ResponseEntity<Resource>> getVideo(String title, String rangeHeader) {
        // Декодируем URL (пути приходят в закодированном виде)
        String decodedTitle = java.net.URLDecoder.decode(title, java.nio.charset.StandardCharsets.UTF_8);
        log.info("Requested video: {}, range: {}", decodedTitle, rangeHeader);

        if (!playListService.existsByTitle(decodedTitle)) {
            log.warn("Video not found: {}", decodedTitle);
            return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        }

        boolean needsTranscoding = playListService.requiresTranscoding(decodedTitle);

        if (needsTranscoding) {
            if (!ffmpegService.isAvailable()) {
                log.error("FFmpeg not available for transcoding: {}", decodedTitle);
                return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(new org.springframework.core.io.ByteArrayResource(
                                "FFmpeg not available. Cannot transcode this video.".getBytes())));
            }
            // Для транскодированного видео возвращаем специальный маркер
            return Mono.just(ResponseEntity.status(HttpStatus.OK)
                    .header("X-Transcode-Needed", "true")
                    .header("X-Video-Path", title)
                    .build());
        } else {
            return streamNativeVideo(decodedTitle, rangeHeader);
        }
    }

    @Override
    public Flux<DataBuffer> getTranscodedVideo(String title) {
        String decodedTitle = java.net.URLDecoder.decode(title, java.nio.charset.StandardCharsets.UTF_8);

        return Flux.<DataBuffer>create(emitter -> {
            try {
                String filePath = playListService.getVideoPath(decodedTitle)
                        .orElseThrow(() -> new IllegalArgumentException("Video not found: " + decodedTitle));

                log.info("Starting transcoding for: {}", decodedTitle);

                ProcessBuilder processBuilder = ffmpegService.createTranscodingProcessBuilder(filePath);
                Process process = processBuilder.start();

                // Читаем вывод ffmpeg в потоке
                try (InputStream inputStream = process.getInputStream()) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        if (emitter.isCancelled()) {
                            process.destroy();
                            return;
                        }
                        // Создаём DataBuffer из байтов
                        DataBuffer dataBuffer = new DefaultDataBufferFactory().allocateBuffer(bytesRead);
                        dataBuffer.write(buffer, 0, bytesRead);
                        emitter.next(dataBuffer);
                    }
                }

                process.waitFor();
                emitter.complete();
            } catch (Exception e) {
                log.error("Error during transcoding: {}", e.getMessage(), e);
                emitter.error(e);
            }
        }, reactor.core.publisher.FluxSink.OverflowStrategy.BUFFER)
        .subscribeOn(Schedulers.boundedElastic());
    }

    private Mono<ResponseEntity<Resource>> streamNativeVideo(String title, String rangeHeader) {
        return Mono.fromCallable(() -> {
            String filePath = playListService.getVideoPath(title)
                    .orElseThrow(() -> new IllegalArgumentException("Video not found: " + title));

            Path path = Paths.get(filePath);
            Resource resource = new FileSystemResource(path);
            File file = resource.getFile();
            long fileSize = file.length();

            if (rangeHeader == null || rangeHeader.isEmpty()) {
                log.info("Streaming full video (native): {}", title);
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType("video/" + playListService.getFileExtension(title)))
                        .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileSize))
                        .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                        .body(resource);
            }

            return handleRangeRequest(rangeHeader, resource, fileSize, title);
        }).flatMap(Mono::just)
                .onErrorResume(e -> {
                    log.error("Error streaming native video: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    private ResponseEntity<Resource> handleRangeRequest(String rangeHeader, Resource resource, long fileSize, String title) {
        try {
            String range = rangeHeader.replace("bytes=", "");
            String[] ranges = range.split("-");

            long start = Long.parseLong(ranges[0]);
            long end = ranges.length > 1 && !ranges[1].isEmpty()
                    ? Long.parseLong(ranges[1])
                    : fileSize - 1;

            start = Math.max(0, Math.min(start, fileSize - 1));
            end = Math.max(start, Math.min(end, fileSize - 1));

            long contentLength = end - start + 1;

            log.info("Range request for {}: {}-{} (of {})", title, start, end, fileSize);

            byte[] content = readPartialContent(resource.getFile(), start, contentLength);

            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .contentType(MediaType.valueOf("video/" + playListService.getFileExtension(title)))
                    .header(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileSize)
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(contentLength))
                    .body(new org.springframework.core.io.ByteArrayResource(content));

        } catch (IOException e) {
            log.error("Error reading partial content: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private byte[] readPartialContent(File file, long start, long length) throws IOException {
        try (InputStream inputStream = Files.newInputStream(file.toPath())) {
            long skipped = inputStream.skip(start);
            if (skipped != start) {
                throw new IOException("Could not skip to position " + start);
            }
            return inputStream.readNBytes(Math.toIntExact(length));
        }
    }
}
