package ru.streamer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.streamer.playlist.PlayListInitialization;

@SpringBootApplication
public class StreamerApplication implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(StreamerApplication.class);
	
	private final PlayListInitialization playListService;

	public StreamerApplication(PlayListInitialization playListService) {
		this.playListService = playListService;
	}

	public static void main(String[] args) {
		SpringApplication.run(StreamerApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		try {
			playListService.init();
			log.info("Application started successfully");
		} catch (Exception e) {
			log.error("Failed to initialize playlist: {}", e.getMessage(), e);
			throw e;
		}
	}
}
