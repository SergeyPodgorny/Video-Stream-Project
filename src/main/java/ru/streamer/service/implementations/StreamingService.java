package ru.streamer.service.implementations;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.streamer.playlist.PlayListInitialization;
import ru.streamer.service.VideoProvider;

@Service
@Slf4j
@RequiredArgsConstructor
public class StreamingService implements VideoProvider {

    private final PlayListInitialization playList;
    private final ResourceLoader resourceLoader;
    private final String routeFormat ="file:/%s";
    private final String titleFormat = "Played file title /%s";
    private final String routeLogFormat = "Played file route /%s";


    public Mono<Resource> getVideo(String title){
        log.info(String.format(titleFormat, title));
        var currentRoute = String.format(routeFormat,playList.getPlayList().get(title));
        log.info(String.format(routeLogFormat,currentRoute));
        return Mono.fromSupplier(()-> resourceLoader.getResource(currentRoute));
    }

}
