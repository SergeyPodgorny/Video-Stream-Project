package ru.streamer.controllers.implementations;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.streamer.controllers.PlayListProvider;
import ru.streamer.dto.VideoInfo;
import ru.streamer.playlist.PlayListInitialization;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequiredArgsConstructor
public class SynchronousPlayListController implements PlayListProvider {

    private final PlayListInitialization service;

    @GetMapping(value = "/playlist", produces = "application/json")
    @Override
    public List<VideoInfo> getPlayList() {
        return service.getPlayList().keySet().stream()
                .map(title -> VideoInfo.builder()
                        .title(title)
                        .url("/streamer_page.html?video=" + title)
                        .build())
                .collect(Collectors.toList());
    }

}
