package ru.streamer.controllers.implementations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.streamer.controllers.PlayListProvider;
import ru.streamer.dto.VideoInfo;
import ru.streamer.playlist.PlayListInitialization;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
public class SynchronousPlayListController implements PlayListProvider {

    private static final Logger log = LoggerFactory.getLogger(SynchronousPlayListController.class);
    private final PlayListInitialization service;

    public SynchronousPlayListController(PlayListInitialization service) {
        this.service = service;
    }

    @GetMapping(value = "/playlist", produces = "application/json")
    @Override
    public List<VideoInfo> getPlayList() {
        return service.getPlayList().keySet().stream()
                .map(title -> {
                    String extension = title.substring(title.lastIndexOf('.') + 1).toLowerCase();
                    boolean needsTranscoding = service instanceof ru.streamer.playlist.impl.PlayListService 
                        ? ((ru.streamer.playlist.impl.PlayListService) service).requiresTranscoding(title)
                        : false;
                    return VideoInfo.builder()
                            .title(title)
                            .url("/streamer_page.html?video=" + title)
                            .format(extension.toUpperCase())
                            .requiresTranscoding(needsTranscoding)
                            .build();
                })
                .collect(Collectors.toList());
    }

}
