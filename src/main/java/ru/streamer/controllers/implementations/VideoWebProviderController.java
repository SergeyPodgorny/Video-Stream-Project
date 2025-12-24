package ru.streamer.controllers.implementations;



import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.streamer.controllers.VideoWebProvider;
import ru.streamer.service.VideoProvider;

@RestController
public class VideoWebProviderController implements VideoWebProvider {


    private final VideoProvider videoService;

    @Autowired
    public VideoWebProviderController(VideoProvider videoService) {
        this.videoService = videoService;
    }


    @GetMapping(value = "video/{title}", produces = "video/mp4")
    public Mono<Resource> streamVideo(@PathVariable String title, @RequestHeader("Range") String range) {
        return videoService.getVideo(title);
    }

}
