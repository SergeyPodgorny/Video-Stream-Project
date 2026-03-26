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
    
    @Value("${ffmpeg.path:ffmpeg}")
    private String ffmpegPath;

    @PostConstruct
    public void init() {
        try {
            ProcessBuilder test = new ProcessBuilder(ffmpegPath, "-version");
            Process process = test.start();
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                this.available = true;
                log.info("FFmpeg found and initialized at: {}", ffmpegPath);
            } else {
                this.available = false;
                log.debug("FFmpeg test failed with exit code: {}. Transcoding will be unavailable.", exitCode);
            }
        } catch (IOException e) {
            this.available = false;
            log.debug("FFmpeg not found at '{}'. Transcoding will be unavailable. Install ffmpeg and add to PATH.", ffmpegPath);
        } catch (InterruptedException e) {
            this.available = false;
            Thread.currentThread().interrupt();
            log.debug("FFmpeg initialization was interrupted");
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
