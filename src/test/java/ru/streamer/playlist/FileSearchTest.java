package ru.streamer.playlist;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.streamer.StreamerApplication;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = StreamerApplication.class)
@Slf4j
public class FileSearchTest {


    PlayListInitialization searcher;

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
