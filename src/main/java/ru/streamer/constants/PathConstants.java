package ru.streamer.constants;

import java.nio.file.Path;

public final class PathConstants {

    public static final String CURRENT_DIRECTORY = System.getProperty("user.dir");

    public static final Path CURRENT_PATH = Path.of(CURRENT_DIRECTORY+"/playlist");

    private PathConstants() {
    }
}
