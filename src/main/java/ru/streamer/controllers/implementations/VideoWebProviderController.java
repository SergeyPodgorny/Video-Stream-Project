package ru.streamer.controllers.implementations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
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

    @GetMapping(value = "video/{title}", produces = "video/mp4")
    public Mono<ResponseEntity<Resource>> streamVideo(
            @PathVariable String title,
            @RequestHeader(value = "Range", required = false) String range) {
        log.info("Video request: title={}, range={}", title, range);
        return videoService.getVideo(title, range);
    }

}
