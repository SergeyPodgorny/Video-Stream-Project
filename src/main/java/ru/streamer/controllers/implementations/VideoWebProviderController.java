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

    @Override
    @GetMapping(value = "video/**", produces = "video/mp4")
    public Mono<ResponseEntity<Resource>> getVideo(String range) {
        // Извлекаем путь к видео из URI (всё после "/video/")
        String requestUri = org.springframework.web.util.UriUtils.decode(
                org.springframework.web.context.request.RequestContextHolder
                        .getRequestAttributes()
                        .getAttribute("org.springframework.web.servlet.HandlerMapping.pathWithinHandlerMapping", 
                                org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST)
                        .toString(),
                java.nio.charset.StandardCharsets.UTF_8);
        
        String title = requestUri.replaceFirst("^video/", "");
        log.info("Video request: title={}, range={}", title, range);
        return videoService.getVideo(title, range);
    }

}
