package ru.streamer.controllers;

import ru.streamer.dto.VideoFolder;

import java.util.List;

public interface PlayListProvider {

    List<VideoFolder> getPlayList();

}
