package ru.streamer.playlist.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.streamer.annotations.Benchmarked;
import ru.streamer.exceptions.ReadFileSystemException;
import ru.streamer.playlist.PlayListInitialization;
import ru.streamer.playlist.predicates.FileExtensionPredicates;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.streamer.constants.PathConstants.CURRENT_DIRECTORY;

@Component
public class PlayListService implements PlayListInitialization {

    private static final Logger log = LoggerFactory.getLogger(PlayListService.class);
    private static final String VIDEO_PATH_PREFIX = "/video/";

    private Map<String, String> playList = Collections.emptyMap();
    private Map<String, String> fileExtensions = Collections.emptyMap();

    @Override
    @Benchmarked
    public void init() {
        log.info("Current home directory: {}", System.getProperty("user.dir"));
        try (Stream<Path> stream = Files.walk(Paths.get(CURRENT_DIRECTORY), Integer.MAX_VALUE)) {
            var fileList = stream.filter(file -> !Files.isDirectory(file))
                    .filter(FileExtensionPredicates::isSupportedVideoFile)
                    .map(Path::toFile)
                    .toList();

            playList = fileList.stream()
                    .collect(Collectors.toMap(
                            File::getName,
                            File::getAbsolutePath,
                            (s1, s2) -> s1));

            fileExtensions = fileList.stream()
                    .collect(Collectors.toMap(
                            File::getName,
                            file -> FileExtensionPredicates.isBrowserNativeFormat(file.toPath()) ? "native" : "transcode",
                            (s1, s2) -> s1));

            log.info("Loaded {} video files ({} native, {} require transcoding)", 
                    playList.size(),
                    fileExtensions.values().stream().filter("native"::equals).count(),
                    fileExtensions.values().stream().filter("transcode"::equals).count());
        } catch (IOException e) {
            throw new ReadFileSystemException(e);
        }
    }

    @Override
    public Map<String, String> getPlayList() {
        return playList;
    }

    public Optional<String> getVideoPath(String title) {
        return Optional.ofNullable(playList.get(title));
    }

    public boolean existsByTitle(String title) {
        return playList.containsKey(title);
    }

    public boolean requiresTranscoding(String title) {
        return "transcode".equals(fileExtensions.get(title));
    }

    public String getFileExtension(String title) {
        return title.substring(title.lastIndexOf('.') + 1).toLowerCase();
    }
}
