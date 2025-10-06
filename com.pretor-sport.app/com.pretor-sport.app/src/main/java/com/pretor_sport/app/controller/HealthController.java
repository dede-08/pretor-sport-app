package com.pretor_sport.app.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
public class HealthController {

    private final DataSource dataSource;
    private final Environment environment;

    //endpoint basico de salud
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            health.put("status", "UP");
            health.put("timestamp", LocalDateTime.now());
            health.put("application", "Pretor Sport Backend");
            health.put("version", "1.0.0");
            
            //verificar conexion a la base de datos
            boolean dbHealthy = checkDatabaseHealth();
            health.put("database", dbHealthy ? "UP" : "DOWN");
            
            //informacion del entorno
            Map<String, Object> environment = new HashMap<>();
            environment.put("activeProfiles", this.environment.getActiveProfiles());
            environment.put("javaVersion", System.getProperty("java.version"));
            environment.put("serverPort", this.environment.getProperty("server.port", "8080"));
            health.put("environment", environment);
            
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
            return ResponseEntity.status(503).body(health);
        }
    }

    //enpoint detallado de salud (solo disponible para administradores)
    @GetMapping("/detailed")
    public ResponseEntity<Map<String, Object>> detailedHealth() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            //información basica
            health.put("status", "UP");
            health.put("timestamp", LocalDateTime.now());
            health.put("application", "Pretor Sport Backend");
            
            //info de la base de datos
            Map<String, Object> database = new HashMap<>();
            database.put("status", checkDatabaseHealth() ? "UP" : "DOWN");
            database.put("url", environment.getProperty("spring.datasource.url"));
            database.put("driverClassName", environment.getProperty("spring.datasource.driver-class-name"));
            health.put("database", database);
            
            //info de la JVM
            Runtime runtime = Runtime.getRuntime();
            Map<String, Object> jvm = new HashMap<>();
            jvm.put("maxMemory", runtime.maxMemory() / 1024 / 1024 + " MB");
            jvm.put("totalMemory", runtime.totalMemory() / 1024 / 1024 + " MB");
            jvm.put("freeMemory", runtime.freeMemory() / 1024 / 1024 + " MB");
            jvm.put("usedMemory", (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024 + " MB");
            jvm.put("availableProcessors", runtime.availableProcessors());
            health.put("jvm", jvm);
            
            //info del sistema
            Map<String, Object> system = new HashMap<>();
            system.put("osName", System.getProperty("os.name"));
            system.put("osVersion", System.getProperty("os.version"));
            system.put("javaVendor", System.getProperty("java.vendor"));
            system.put("javaVersion", System.getProperty("java.version"));
            health.put("system", system);
            
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
            return ResponseEntity.status(503).body(health);
        }
    }

    private boolean checkDatabaseHealth() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(5); //timeout de 5 segundos
        } catch (Exception e) {
            return false;
        }
    }
}
