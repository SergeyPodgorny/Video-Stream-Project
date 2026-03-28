package ru.streamer.playlist.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.streamer.annotations.Benchmarked;
import ru.streamer.dto.VideoFolder;
import ru.streamer.dto.VideoInfo;
import ru.streamer.exceptions.ReadFileSystemException;
import ru.streamer.playlist.PlayListInitialization;
import ru.streamer.playlist.predicates.FileExtensionPredicates;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.streamer.constants.PathConstants.getCurrentDirectory;

@Component
public class PlayListService implements PlayListInitialization {

    private static final Logger log = LoggerFactory.getLogger(PlayListService.class);
    private static final String VIDEO_PATH_PREFIX = "/video/";

    private Map<String, String> playList = Collections.emptyMap();
    private Map<String, String> fileExtensions = Collections.emptyMap();
    private List<VideoFolder> videoFolders = Collections.emptyList();
    private Map<String, String> playListByRelativePath = Collections.emptyMap();
    private Map<String, List<VideoInfo>> videosByFolderPath = Collections.emptyMap();

    @Override
    @Benchmarked
    public void init() {
        log.info("Current home directory: {}", getCurrentDirectory());
        try (Stream<Path> stream = Files.walk(Paths.get(getCurrentDirectory()), Integer.MAX_VALUE)) {
            var fileList = stream.filter(file -> !Files.isDirectory(file))
                    .filter(FileExtensionPredicates::isSupportedVideoFile)
                    .map(Path::toFile)
                    .toList();

            if (fileList.isEmpty()) {
                log.warn("No video files found in current directory tree. Playlist will be empty.");
                videoFolders = Collections.emptyList();
                playList = Collections.emptyMap();
                fileExtensions = Collections.emptyMap();
                playListByRelativePath = Collections.emptyMap();
                videosByFolderPath = Collections.emptyMap();
                return;
            }

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

            videoFolders = buildFolderHierarchy(fileList);
            
            playListByRelativePath = fileList.stream()
                    .collect(Collectors.toMap(
                            file -> {
                                Path folderRelativePath = Paths.get(getCurrentDirectory()).relativize(file.getParentFile().toPath());
                                String folderPathStr = folderRelativePath.toString().replace("\\", "/");
                                return folderPathStr.isEmpty()
                                        ? file.getName()
                                        : folderPathStr + "/" + file.getName();
                            },
                            File::getAbsolutePath,
                            (s1, s2) -> s1));
            
            videosByFolderPath = fileList.stream()
                    .collect(Collectors.groupingBy(
                            file -> {
                                Path relativePath = Paths.get(getCurrentDirectory()).relativize(file.getParentFile().toPath());
                                String pathStr = relativePath.toString().replace("\\", "/");
                                // Для корневой директории возвращаем "." вместо пустой строки
                                return pathStr.isEmpty() ? "." : pathStr;
                            },
                            Collectors.mapping(
                                    file -> {
                                        Path folderRelativePath = Paths.get(getCurrentDirectory()).relativize(file.getParentFile().toPath());
                                        String folderPathStr = folderRelativePath.toString().replace("\\", "/");
                                        String videoPath = folderPathStr.isEmpty()
                                                ? file.getName()
                                                : folderPathStr + "/" + file.getName();
                                        return VideoInfo.builder()
                                                .title(file.getName())
                                                .url("/streamer_page.html?video=" + encodeUrl(videoPath))
                                                .format(extractFileExtension(file.getName()).toUpperCase())
                                                .requiresTranscoding("transcode".equals(fileExtensions.get(file.getName())))
                                                .build();
                                    },
                                    Collectors.toList()
                            )
                    ));

            log.info("Loaded {} video files from {} folders ({} native, {} require transcoding)",
                    playList.size(),
                    countFolders(videoFolders),
                    fileExtensions.values().stream().filter("native"::equals).count(),
                    fileExtensions.values().stream().filter("transcode"::equals).count());
        } catch (IOException e) {
            throw new ReadFileSystemException(e);
        }
    }

    private int countFolders(List<VideoFolder> folders) {
        int count = 0;
        for (VideoFolder folder : folders) {
            count++;
            if (folder.getSubFolders() != null) {
                count += countFolders(folder.getSubFolders());
            }
        }
        return count;
    }

    private List<VideoFolder> buildFolderHierarchy(List<File> fileList) {
        Path rootPath = Paths.get(getCurrentDirectory());

        // Группируем файлы по их родительским папкам
        Map<Path, List<File>> filesByFolder = fileList.stream()
                .collect(Collectors.groupingBy(file -> file.getParentFile().toPath()));

        // Собираем все уникальные пути папок
        Set<Path> allFolderPaths = new HashSet<>(filesByFolder.keySet());

        // Добавляем все родительские папки для создания полной иерархии
        Set<Path> allPaths = new HashSet<>();
        for (Path folderPath : allFolderPaths) {
            Path current = folderPath;
            while (current != null && !current.equals(rootPath)) {
                allPaths.add(current);
                current = current.getParent();
            }
        }

        // Если есть файлы в корневой директории, добавляем специальную папку "."
        boolean hasRootFiles = filesByFolder.containsKey(rootPath);
        if (hasRootFiles) {
            allPaths.add(rootPath);
        }

        // Создаём карту папок
        Map<Path, VideoFolder> folderMap = new LinkedHashMap<>();

        for (Path folderPath : allPaths) {
            Path relativePath = rootPath.relativize(folderPath);
            String[] parts = relativePath.toString().replace("\\", "/").split("/");

            Path currentPath = rootPath;
            VideoFolder parentFolder = null;

            for (int i = 0; i < parts.length; i++) {
                // Обработка корневой директории
                if (parts.length == 1 && parts[0].isEmpty()) {
                    continue; // Пропускаем пустую часть для корня
                }
                
                currentPath = currentPath.resolve(parts[i]);
                String relativePathStr = rootPath.relativize(currentPath).toString().replace("\\", "/");

                if (!folderMap.containsKey(currentPath)) {
                    boolean hasVideos = filesByFolder.containsKey(currentPath);

                    VideoFolder folder = VideoFolder.builder()
                            .name(parts[i])
                            .path(currentPath.toString())
                            .relativePath(relativePathStr)
                            .hasVideos(hasVideos)
                            .build();

                    if (parentFolder != null) {
                        parentFolder.getSubFolders().add(folder);
                    }
                    folderMap.put(currentPath, folder);
                }

                parentFolder = folderMap.get(currentPath);
            }
        }

        // Получаем корневые папки (те, чей родитель - rootPath)
        List<VideoFolder> rootFolders = folderMap.entrySet().stream()
                .filter(e -> e.getKey().getParent().equals(rootPath))
                .map(Map.Entry::getValue)
                .sorted(Comparator.comparing(VideoFolder::getName))
                .collect(Collectors.toList());

        // Сортируем все подпапки и обновляем folderCount
        sortSubFolders(rootFolders);
        updateFolderCounts(rootFolders);

        return rootFolders;
    }

    private void updateFolderCounts(List<VideoFolder> folders) {
        for (VideoFolder folder : folders) {
            folder.setFolderCount(folder.getSubFolders().size());
            if (folder.getSubFolders() != null && !folder.getSubFolders().isEmpty()) {
                updateFolderCounts(folder.getSubFolders());
            }
        }
    }

    private void sortSubFolders(List<VideoFolder> folders) {
        folders.sort(Comparator.comparing(VideoFolder::getName));
        for (VideoFolder folder : folders) {
            if (folder.getSubFolders() != null && !folder.getSubFolders().isEmpty()) {
                sortSubFolders(folder.getSubFolders());
            }
        }
    }

    private String extractFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }

    private String encodeUrl(String path) {
        return path.replace(" ", "%20")
                   .replace("#", "%23")
                   .replace("&", "%26")
                   .replace("+", "%2B");
    }

    private String decodeUrl(String path) {
        try {
            return java.net.URLDecoder.decode(path, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return path;
        }
    }

    @Override
    public Map<String, String> getPlayList() {
        return playList;
    }

    public Optional<String> getVideoPath(String title) {
        String decodedTitle = decodeUrl(title);
        return Optional.ofNullable(playListByRelativePath.get(decodedTitle))
                .or(() -> Optional.ofNullable(playList.get(title)))
                .or(() -> Optional.ofNullable(playList.get(decodedTitle)));
    }

    public boolean existsByTitle(String title) {
        String decodedTitle = decodeUrl(title);
        boolean exists = playListByRelativePath.containsKey(decodedTitle)
                || playList.containsKey(title)
                || playList.containsKey(decodedTitle);
        
        if (!exists) {
            log.warn("Video not found. Requested: '{}'. Available keys in playListByRelativePath: {}", 
                    decodedTitle, playListByRelativePath.keySet());
        }
        return exists;
    }

    public boolean requiresTranscoding(String title) {
        String decodedTitle = decodeUrl(title);
        String fileName = findFileNameByTitle(decodedTitle);
        return "transcode".equals(fileExtensions.get(fileName));
    }

    public String getFileExtension(String title) {
        String decodedTitle = decodeUrl(title);
        String fileName = findFileNameByTitle(decodedTitle);
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }

    private String findFileNameByTitle(String title) {
        if (title.contains("/")) {
            String[] parts = title.split("/");
            return parts[parts.length - 1];
        }
        return title;
    }

    @Override
    public List<VideoFolder> getVideoFolders() {
        return videoFolders;
    }

    public List<VideoInfo> getVideosInFolder(String folderPath) {
        String decodedPath = decodeUrl(folderPath);
        // Нормализуем путь: пустая строка, "/" или "." означают корневую директорию
        if (decodedPath.isEmpty() || decodedPath.equals("/") || decodedPath.equals(".")) {
            decodedPath = ".";
        }
        return videosByFolderPath.getOrDefault(decodedPath, Collections.emptyList());
    }
}
