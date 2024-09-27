package com.avalanches.frameworksanddrivers.databases.config;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class RedisConfig {

    @Autowired
    private Environment ambiente;

    @Bean
    public RedisClient redisClient() {
        String host = ambiente.getProperty("spring.data.redis.host");
        String port = ambiente.getProperty("spring.data.redis.port");
        String password = ambiente.getProperty("spring.data.redis.password");

        String redisUrl = String.format("redis://%s:%s", host, port);
        if (password != null && !password.isEmpty()) {
            redisUrl = String.format("redis://:%s@%s:%s", password, host, port);
        }

        return RedisClient.create(redisUrl);
    }

    @Bean
    public StatefulRedisConnection<String, String> redisConnection(RedisClient redisClient) {
        return redisClient.connect();
    }

    @Bean
    public RedisCommands<String, String> redisCommands(StatefulRedisConnection<String, String> connection) {
        return connection.sync();
    }
}
