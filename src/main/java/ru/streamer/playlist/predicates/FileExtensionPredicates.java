package ru.streamer.playlist.predicates;

import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Set;

@Component
public final class FileExtensionPredicates {

    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(
            "mp4", "webm", "mkv", "avi", "mov", "wmv", "flv"
    );

    private static final Set<String> BROWSER_NATIVE_EXTENSIONS = Set.of(
            "mp4", "webm", "ogg"
    );

    private FileExtensionPredicates() {
    }

    public static boolean isSupportedVideoFile(Path path) {
        var extension = FilenameUtils.getExtension(String.valueOf(path.getFileName())).toLowerCase();
        return SUPPORTED_EXTENSIONS.contains(extension);
    }

    public static boolean isBrowserNativeFormat(Path path) {
        var extension = FilenameUtils.getExtension(String.valueOf(path.getFileName())).toLowerCase();
        return BROWSER_NATIVE_EXTENSIONS.contains(extension);
    }

    public static boolean requiresTranscoding(Path path) {
        return isSupportedVideoFile(path) && !isBrowserNativeFormat(path);
    }

    public static boolean mp4Test(Path path) {
        var extension = FilenameUtils.getExtension(String.valueOf(path.getFileName())).toLowerCase();
        return "mp4".equals(extension);
    }

    public static boolean mkvTest(Path path) {
        var extension = FilenameUtils.getExtension(String.valueOf(path.getFileName())).toLowerCase();
        return "mkv".equals(extension);
    }

    public static boolean aviTest(Path path) {
        var extension = FilenameUtils.getExtension(String.valueOf(path.getFileName())).toLowerCase();
        return "avi".equals(extension);
    }

    public static boolean webmTest(Path path) {
        var extension = FilenameUtils.getExtension(String.valueOf(path.getFileName())).toLowerCase();
        return "webm".equals(extension);
    }
}
