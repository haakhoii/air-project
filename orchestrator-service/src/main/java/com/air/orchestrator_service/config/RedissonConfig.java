package com.air.orchestrator_service.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Value("${redisson.address}")
    private String redissonAddress;

    @Value("${redisson.connectionPoolSize:64}")
    private int connectionPoolSize;

    @Value("${redisson.connectionMinimumIdleSize:10}")
    private int connectionMinimumIdleSize;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config cfg = new Config();
        cfg.useSingleServer()
                .setAddress(redissonAddress)
                .setConnectionPoolSize(connectionPoolSize)
                .setConnectionMinimumIdleSize(connectionMinimumIdleSize);
        return Redisson.create(cfg);
    }
}
