package ru.streamer.controllers;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public interface VideoWebProvider {

    Mono<ResponseEntity<Resource>> getVideo(ServerWebExchange exchange, String range);

}
