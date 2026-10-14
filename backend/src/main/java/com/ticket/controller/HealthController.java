package com.ticket.controller;

import com.ticket.common.Result;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    @Resource
    private DataSource dataSource;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> info = new HashMap<>();
        info.put("time", LocalDateTime.now());

        try (Connection conn = dataSource.getConnection()) {
            info.put("db", conn.isValid(2) ? "UP" : "DOWN");
        } catch (Exception e) {
            info.put("db", "DOWN");
        }

        try {
            redisTemplate.opsForValue().get("health:check");
            info.put("redis", "UP");
        } catch (Exception e) {
            info.put("redis", "DOWN");
        }

        boolean allUp = "UP".equals(info.get("db")) && "UP".equals(info.get("redis"));
        info.put("status", allUp ? "UP" : "DEGRADED");
        return allUp ? Result.ok(info) : Result.ok(info);
    }
}