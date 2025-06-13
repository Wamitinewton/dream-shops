package com.newton.dream_shops.config;

import java.util.Properties;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisHealthIndicator implements HealthIndicator {

    private final RedisConnectionFactory redisConnectionFactory;

    @Override
    public Health health() {
        try {
            RedisConnection connection = redisConnectionFactory.getConnection();
            if (connection != null) {
                try {
                    connection.ping();
                    return Health.up()
                            .withDetail("redis", "Available")
                            .withDetail("version", getRedisVersion(connection))
                            .build();
                } finally {
                    connection.close();
                }
            }
            return Health.down()
                    .withDetail("redis", "Connection is null")
                    .build();
        } catch (Exception e) {
            log.error("Redis health check failed", e);
            return Health.down()
                    .withDetail("redis", "Connection failed")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }

    private String getRedisVersion(RedisConnection connection) {
        try {
            Properties info = connection.serverCommands().info("server");
            return info.getProperty("redis_version", "Unknown");
        } catch (Exception e) {
            return "Unknown";
        }
    }

}
