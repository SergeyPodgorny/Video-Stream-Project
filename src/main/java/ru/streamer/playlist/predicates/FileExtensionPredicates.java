package ru.streamer.playlist.predicates;

import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class FileExtensionPredicates {

    public static boolean mp4Test(Path path) {
        var extension = FilenameUtils.getExtension(String.valueOf(path.getFileName()));
        return extension.equalsIgnoreCase("mp4");
    }

    public static boolean mkvTest(Path path) {
        var extension = FilenameUtils.getExtension(String.valueOf(path.getFileName()));
        return extension.equalsIgnoreCase("mkv");
    }


    public static boolean aviTest(Path path) {
        var extension = FilenameUtils.getExtension(String.valueOf(path.getFileName()));
        return extension.equalsIgnoreCase("avi");
    }


}
