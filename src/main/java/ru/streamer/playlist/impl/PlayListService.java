package ru.streamer.playlist.impl;


import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.streamer.constants.PathConstants.CURRENT_DIRECTORY;

@Component
@Slf4j
@Getter
public class PlayListService implements PlayListInitialization {


    private Map<String, String> playList;

    @Override
    @Benchmarked
    public void init() {
        log.info("Current home directory "+System.getProperty("user.dir"));
        try(Stream<Path> stream = Files.walk(Paths.get(CURRENT_DIRECTORY), Integer.MAX_VALUE)){
            playList = stream.filter(file -> !Files.isDirectory(file))
                    .filter(path -> FileExtensionPredicates.mp4Test(path))
                    .map(Path::toFile)
                    .collect(Collectors.toMap(
                            File::getName,
                            File::getAbsolutePath,
                            (s1,s2)-> s1));
        } catch (IOException e) {
            throw new ReadFileSystemException(e);
        }
    }
}
