package ru.streamer.service.implementations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.streamer.playlist.impl.PlayListService;
import ru.streamer.service.FfmpegService;
import ru.streamer.service.VideoProvider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import org.springframework.core.io.InputStreamResource;

@Service
public class StreamingService implements VideoProvider {

    private static final Logger log = LoggerFactory.getLogger(StreamingService.class);
    private final PlayListService playListService;
    private final FfmpegService ffmpegService;
    private final Executor virtualThreadExecutor;

    public StreamingService(PlayListService playListService, FfmpegService ffmpegService, Executor virtualThreadExecutor) {
        this.playListService = playListService;
        this.ffmpegService = ffmpegService;
        this.virtualThreadExecutor = virtualThreadExecutor;
    }

    @Override
    public Mono<ResponseEntity<Resource>> getVideo(String title, String rangeHeader) {
        log.info("Requested video: {}, range: {}", title, rangeHeader);

        if (!playListService.existsByTitle(title)) {
            log.warn("Video not found: {}", title);
            return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        }

        boolean needsTranscoding = playListService.requiresTranscoding(title);

        if (needsTranscoding) {
            if (!ffmpegService.isAvailable()) {
                log.error("FFmpeg not available for transcoding: {}", title);
                return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(new org.springframework.core.io.ByteArrayResource(
                                "FFmpeg not available. Cannot transcode this video.".getBytes())));
            }
            return streamTranscodedVideo(title, rangeHeader);
        } else {
            return streamNativeVideo(title, rangeHeader);
        }
    }

    private Mono<ResponseEntity<Resource>> streamNativeVideo(String title, String rangeHeader) {
        return Mono.fromCallable(() -> {
            String filePath = playListService.getVideoPath(title)
                    .orElseThrow(() -> new IllegalArgumentException("Video not found: " + title));

            Path path = Paths.get(filePath);
            Resource resource = new org.springframework.core.io.FileSystemResource(path);
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

    private Mono<ResponseEntity<Resource>> streamTranscodedVideo(String title, String rangeHeader) {
        return Mono.fromFuture(() -> CompletableFuture.supplyAsync(() -> {
            try {
                String filePath = playListService.getVideoPath(title)
                        .orElseThrow(() -> new IllegalArgumentException("Video not found: " + title));

                log.info("Starting transcoding for: {}", title);

                PipedInputStream pipedInputStream = new PipedInputStream(1024 * 1024);
                PipedOutputStream pipedOutputStream = new PipedOutputStream(pipedInputStream);

                ProcessBuilder processBuilder = ffmpegService.createTranscodingProcessBuilder(filePath);
                Process process = processBuilder.start();

                CompletableFuture.runAsync(() -> {
                    try (InputStream ffmpegOutput = process.getInputStream();
                         pipedOutputStream) {
                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = ffmpegOutput.read(buffer)) != -1) {
                            pipedOutputStream.write(buffer, 0, bytesRead);
                        }
                    } catch (IOException e) {
                        log.error("Error copying transcoded data: {}", e.getMessage());
                    }
                }, virtualThreadExecutor);

                Resource streamingResource = new InputStreamResource(pipedInputStream) {
                    @Override
                    public long contentLength() {
                        return -1;
                    }

                    @Override
                    public String getFilename() {
                        return title;
                    }
                };

                log.info("Transcoding stream started for: {}", title);
                return ResponseEntity.ok()
                        .contentType(MediaType.valueOf("video/mp4"))
                        .header(HttpHeaders.TRANSFER_ENCODING, "chunked")
                        .header("X-Transcoded", "true")
                        .body(streamingResource);

            } catch (Exception e) {
                log.error("Error starting transcoding: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }, virtualThreadExecutor));
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
