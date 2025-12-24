package ru.streamer.service;

import org.springframework.core.io.Resource;
import reactor.core.publisher.Mono;

public interface VideoProvider {
    Mono<Resource> getVideo(String title);
}
