package com.avalanches.frameworksanddrivers.databases.config;

import com.avalanches.frameworksanddrivers.databases.interfaces.BancoDeDadosContextoInterface;
import io.lettuce.core.api.sync.RedisCommands;
import jakarta.inject.Inject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class BancoDeDadosContexto implements BancoDeDadosContextoInterface {

    private final JdbcTemplate jdbcTemplate;
    private final RedisCommands<String, String> redisCommands;

    @Inject
    public BancoDeDadosContexto(JdbcTemplate jdbcTemplate, RedisCommands<String, String> redisCommands) {
        this.jdbcTemplate = jdbcTemplate;
        this.redisCommands = redisCommands;
    }

    @Override
    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    @Override
    public RedisCommands<String, String> getRedisCommands() {
        return redisCommands;
    }
}
