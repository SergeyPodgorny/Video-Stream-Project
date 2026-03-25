package ru.streamer.playlist;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.streamer.StreamerApplication;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = StreamerApplication.class)
public class FileSearchTest {

    private static final Logger log = LoggerFactory.getLogger(FileSearchTest.class);
    private final PlayListInitialization searcher;

    @Autowired
    public FileSearchTest(PlayListInitialization searcher) {
        this.searcher = searcher;
    }

    @Test
    void searchFiles_shouldNotReturnNull() {
//        searcher.getPlayList().entrySet().forEach(System.out::println);
        assertThat(searcher.getPlayList()).isNotNull();
    }

    @BeforeEach
    void testSetup(){
        searcher.init();
    }

}
