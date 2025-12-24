package ru.streamer.controllers.implementations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.streamer.controllers.PlayListProvider;
import ru.streamer.playlist.PlayListInitialization;

import java.util.Map;


@RestController
public class SynchronousPlayListController  implements PlayListProvider {


    private final PlayListInitialization service;

    @Autowired
    public SynchronousPlayListController(PlayListInitialization service) {
        this.service = service;
    }


    @GetMapping(value= "/playlist", produces="application/json")
    @Override
    public Map<String,String> getPlayList() {
        return service.getPlayList();
    }

}
