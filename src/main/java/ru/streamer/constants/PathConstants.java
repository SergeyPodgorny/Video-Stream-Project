package ru.streamer.constants;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PathConstants {

    private static String videoDirectory;
    private static String currentDirectory;

    @Value("${video.directory:#{null}}")
    public void setVideoDirectory(String value) {
        videoDirectory = value;
    }

    @PostConstruct
    public void init() {
        currentDirectory = videoDirectory != null ? videoDirectory : System.getProperty("user.dir");
    }

    public static String getCurrentDirectory() {
        return currentDirectory;
    }

    private PathConstants() {
    }
}
