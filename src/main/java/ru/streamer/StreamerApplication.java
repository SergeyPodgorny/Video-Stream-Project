package ru.streamer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.streamer.playlist.PlayListInitialization;

@SpringBootApplication
public class StreamerApplication implements CommandLineRunner {

	private final PlayListInitialization playListService;

	@Autowired
    public StreamerApplication(PlayListInitialization playListService) {
        this.playListService = playListService;
    }


    public static void main(String[] args) {
		SpringApplication.run(StreamerApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		playListService.init();
	}
}
