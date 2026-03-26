package ru.streamer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.resources.LoopResources;

import java.time.Duration;

@Configuration
public class ReactorNettyConfig {

    @Bean
    public ConnectionProvider connectionProvider() {
        return ConnectionProvider.builder("video-streamer-connection-provider")
                .maxConnections(100)
                .maxIdleTime(Duration.ofMinutes(1))
                .maxLifeTime(Duration.ofMinutes(5))
                .evictInBackground(Duration.ofSeconds(30))
                .build();
    }

    @Bean
    public LoopResources loopResources() {
        return LoopResources.create("video-streamer-loop-resources", 4, true);
    }
}
