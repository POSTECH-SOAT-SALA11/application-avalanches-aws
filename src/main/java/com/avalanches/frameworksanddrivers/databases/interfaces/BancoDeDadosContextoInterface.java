package com.avalanches.frameworksanddrivers.databases.interfaces;

import io.lettuce.core.api.sync.RedisCommands;
import org.springframework.jdbc.core.JdbcTemplate;

public interface BancoDeDadosContextoInterface {

    JdbcTemplate getJdbcTemplate();

    RedisCommands<String, String> getRedisCommands();
}
