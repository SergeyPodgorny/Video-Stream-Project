package ru.streamer.playlist;

import ru.streamer.dto.VideoFolder;

import java.util.List;
import java.util.Map;

public interface PlayListInitialization {

    void init();

    Map<String, String> getPlayList();

    List<VideoFolder> getVideoFolders();

}
