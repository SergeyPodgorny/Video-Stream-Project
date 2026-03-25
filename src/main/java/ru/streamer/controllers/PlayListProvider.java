package ru.streamer.controllers;

import ru.streamer.dto.VideoInfo;

import java.util.List;

public interface PlayListProvider {

    List<VideoInfo> getPlayList();

}
