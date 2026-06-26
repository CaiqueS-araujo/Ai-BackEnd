package com.serratec.chat.service;

import com.serratec.chat.dto.response.HealthResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class HealthService {

    private final DataSource dataSource;

    @Value("${spring.application.name:chat-backend}")
    private String serviceName;

    @Value("${app.version:0.1.0}")
    private String version;

    public HealthService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public HealthResponse check() {
        String dbStatus = checkDatabase();
        String overallStatus = "DOWN".equals(dbStatus) ? "DOWN" : "UP";

        Map<String, String> checks = new LinkedHashMap<>();
        checks.put("database", dbStatus);

        return new HealthResponse(
                overallStatus,
                serviceName,
                version,
                Instant.now(),
                checks);
    }

    private String checkDatabase() {
        try (Connection conn = dataSource.getConnection()) {
            if (conn.isValid(2)) {
                return "UP";
            }
            return "DOWN";
        } catch (SQLException e) {
            return "DOWN";
        }
    }
}
