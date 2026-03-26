package ru.streamer.controllers.implementations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.streamer.dto.VideoFolder;
import ru.streamer.dto.VideoInfo;
import ru.streamer.playlist.impl.PlayListService;

import java.util.List;


@RestController
public class SynchronousPlayListController {

    private static final Logger log = LoggerFactory.getLogger(SynchronousPlayListController.class);
    private final PlayListService service;

    public SynchronousPlayListController(PlayListService service) {
        this.service = service;
    }

    @GetMapping(value = "/playlist", produces = "application/json")
    public List<VideoFolder> getPlayList() {
        return service.getVideoFolders();
    }

    @GetMapping(value = "/playlist/folder", produces = "application/json")
    public List<VideoInfo> getVideosInFolder(@RequestParam String path) {
        log.info("Getting videos in folder: {}", path);
        return service.getVideosInFolder(path);
    }

}
