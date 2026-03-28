package ru.streamer.controllers.implementations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.streamer.controllers.VideoWebProvider;
import ru.streamer.service.VideoProvider;

@RestController
public class VideoWebProviderController implements VideoWebProvider {

    private static final Logger log = LoggerFactory.getLogger(VideoWebProviderController.class);
    private final VideoProvider videoService;

    public VideoWebProviderController(VideoProvider videoService) {
        this.videoService = videoService;
    }

    @Override
    @GetMapping(value = "/video/**", produces = "video/mp4")
    public Mono<ResponseEntity<Resource>> getVideo(
            ServerWebExchange exchange,
            @RequestHeader(value = "Range", required = false) String range) {
        
        // Извлекаем путь к видео из URI (всё после "/video/")
        String path = exchange.getRequest().getPath().value();
        String title = path.replaceFirst("^/video/", "");
        
        log.info("Video request: title={}, range={}", title, range);
        return videoService.getVideo(title, range);
    }

    @GetMapping(value = "/video/transcoded/**", produces = "video/mp4")
    public Flux<DataBuffer> getTranscodedVideo(ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().value();
        String title = path.replaceFirst("^/video/transcoded/", "");
        
        log.info("Transcoded video request: {}", title);
        
        // Устанавливаем заголовки ответа
        exchange.getResponse().getHeaders().setContentType(MediaType.valueOf("video/mp4"));
        exchange.getResponse().getHeaders().set(HttpHeaders.TRANSFER_ENCODING, "chunked");
        
        return videoService.getTranscodedVideo(title);
    }

}
