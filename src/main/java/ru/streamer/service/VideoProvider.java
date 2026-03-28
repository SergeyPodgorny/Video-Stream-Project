package ru.streamer.service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface VideoProvider {
    Mono<ResponseEntity<Resource>> getVideo(String title, String rangeHeader);
    Flux<DataBuffer> getTranscodedVideo(String title);
}
