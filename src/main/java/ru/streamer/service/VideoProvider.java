package ru.streamer.service;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

public interface VideoProvider {
    Mono<ResponseEntity<Resource>> getVideo(String title, String rangeHeader);
}
