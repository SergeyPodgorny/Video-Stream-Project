package ru.streamer.dto;

public class VideoInfo {
    private String title;
    private String url;
    private String format;
    private boolean requiresTranscoding;

    public VideoInfo() {
    }

    public VideoInfo(String title, String url, String format, boolean requiresTranscoding) {
        this.title = title;
        this.url = url;
        this.format = format;
        this.requiresTranscoding = requiresTranscoding;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public boolean isRequiresTranscoding() {
        return requiresTranscoding;
    }

    public void setRequiresTranscoding(boolean requiresTranscoding) {
        this.requiresTranscoding = requiresTranscoding;
    }

    public static VideoInfoBuilder builder() {
        return new VideoInfoBuilder();
    }

    public static class VideoInfoBuilder {
        private String title;
        private String url;
        private String format;
        private boolean requiresTranscoding;

        public VideoInfoBuilder title(String title) {
            this.title = title;
            return this;
        }

        public VideoInfoBuilder url(String url) {
            this.url = url;
            return this;
        }

        public VideoInfoBuilder format(String format) {
            this.format = format;
            return this;
        }

        public VideoInfoBuilder requiresTranscoding(boolean requiresTranscoding) {
            this.requiresTranscoding = requiresTranscoding;
            return this;
        }

        public VideoInfo build() {
            return new VideoInfo(title, url, format, requiresTranscoding);
        }
    }
}
