package ru.streamer.controllers;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

public interface VideoWebProvider {

    Mono<ResponseEntity<Resource>> getVideo(String range);

}
