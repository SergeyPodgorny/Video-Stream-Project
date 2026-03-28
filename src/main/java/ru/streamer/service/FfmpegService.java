package ru.streamer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.streamer.exceptions.TranscodingException;

import jakarta.annotation.PostConstruct;
import java.io.IOException;

@Service
public class FfmpegService {

    private static final Logger log = LoggerFactory.getLogger(FfmpegService.class);
    private boolean available = false;
    private String ffmpegPath = "ffmpeg";

    // Стандартные пути установки ffmpeg на Windows
    private static final String[] FFMPEG_PATHS = {
        "ffmpeg", // PATH
        "C:\\Program Files\\ffmpeg\\ffmpeg-8.1-essentials_build\\bin\\ffmpeg.exe",
        "C:\\Program Files\\ffmpeg\\bin\\ffmpeg.exe",
        "C:\\Program Files (x86)\\ffmpeg\\bin\\ffmpeg.exe",
        System.getenv("USERPROFILE") + "\\ffmpeg\\bin\\ffmpeg.exe"
    };

    @PostConstruct
    public void init() {
        // Ищем ffmpeg в стандартных путях
        for (String path : FFMPEG_PATHS) {
            if (path.contains("%") && path.contains("%")) {
                // Пропускаем пути с переменными окружения, которые не раскрылись
                continue;
            }
            if (testFfmpeg(path)) {
                this.ffmpegPath = path;
                this.available = true;
                log.info("FFmpeg found and initialized at: {}", ffmpegPath);
                return;
            }
        }
        
        this.available = false;
        log.debug("FFmpeg not found in PATH or standard locations. Transcoding will be unavailable.");
    }

    private boolean testFfmpeg(String path) {
        try {
            ProcessBuilder test = new ProcessBuilder(path, "-version");
            Process process = test.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    public boolean isAvailable() {
        return available;
    }

    public ProcessBuilder createTranscodingProcessBuilder(String inputPath) {
        if (!available) {
            throw new TranscodingException("FFmpeg is not available");
        }

        ProcessBuilder processBuilder = new ProcessBuilder(
                ffmpegPath,
                "-i", inputPath,
                "-c:v", "libx264",
                "-c:a", "aac",
                "-f", "mp4",
                "pipe:1"
        );

        processBuilder.redirectErrorStream(true);
        return processBuilder;
    }
}
