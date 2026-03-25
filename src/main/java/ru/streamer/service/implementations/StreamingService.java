package ru.streamer.service.implementations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.streamer.playlist.impl.PlayListService;
import ru.streamer.service.VideoProvider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class StreamingService implements VideoProvider {

    private final PlayListService playListService;

    @Override
    public Mono<ResponseEntity<Resource>> getVideo(String title, String rangeHeader) {
        log.info("Requested video: {}", title);

        if (!playListService.existsByTitle(title)) {
            log.warn("Video not found: {}", title);
            return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        }

        return Mono.fromCallable(() -> {
            String filePath = playListService.getVideoPath(title)
                    .orElseThrow(() -> new IllegalArgumentException("Video not found: " + title));

            Path path = Paths.get(filePath);
            Resource resource = new org.springframework.core.io.FileSystemResource(path);
            File file = resource.getFile();
            long fileSize = file.length();

            if (rangeHeader == null || rangeHeader.isEmpty()) {
                log.info("Streaming full video: {}", title);
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, "video/mp4")
                        .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileSize))
                        .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                        .body(resource);
            }

            return handleRangeRequest(rangeHeader, resource, fileSize, title);
        }).flatMap(Mono::just)
                .onErrorResume(e -> {
                    log.error("Error streaming video: {}", e.getMessage());
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
                    .header(HttpHeaders.CONTENT_TYPE, "video/mp4")
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
