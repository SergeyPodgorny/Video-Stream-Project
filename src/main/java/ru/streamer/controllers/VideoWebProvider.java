package ru.streamer.controllers;


import org.springframework.core.io.Resource;
import reactor.core.publisher.Mono;

public interface VideoWebProvider {

    Mono<Resource> streamVideo(String title, String range);

}
