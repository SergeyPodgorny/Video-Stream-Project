package ru.streamer.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VideoFolder {
    private String name;
    private String path;
    private String relativePath;
    private List<VideoFolder> subFolders;
    private int folderCount;
    private boolean hasVideos;

    public VideoFolder() {
        this.subFolders = new ArrayList<>();
    }

    public VideoFolder(String name, String path, String relativePath, boolean hasVideos) {
        this.name = name;
        this.path = path;
        this.relativePath = relativePath;
        this.subFolders = new ArrayList<>();
        this.folderCount = 0;
        this.hasVideos = hasVideos;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public List<VideoFolder> getSubFolders() {
        return subFolders;
    }

    public void setSubFolders(List<VideoFolder> subFolders) {
        this.subFolders = subFolders;
        this.folderCount = subFolders != null ? subFolders.size() : 0;
    }

    public int getFolderCount() {
        return folderCount;
    }

    public void setFolderCount(int folderCount) {
        this.folderCount = folderCount;
    }

    public boolean isHasVideos() {
        return hasVideos;
    }

    public void setHasVideos(boolean hasVideos) {
        this.hasVideos = hasVideos;
    }

    public static VideoFolderBuilder builder() {
        return new VideoFolderBuilder();
    }

    public static class VideoFolderBuilder {
        private String name;
        private String path;
        private String relativePath;
        private boolean hasVideos;

        public VideoFolderBuilder name(String name) {
            this.name = name;
            return this;
        }

        public VideoFolderBuilder path(String path) {
            this.path = path;
            return this;
        }

        public VideoFolderBuilder relativePath(String relativePath) {
            this.relativePath = relativePath;
            return this;
        }

        public VideoFolderBuilder hasVideos(boolean hasVideos) {
            this.hasVideos = hasVideos;
            return this;
        }

        public VideoFolder build() {
            return new VideoFolder(name, path, relativePath, hasVideos);
        }
    }
}
