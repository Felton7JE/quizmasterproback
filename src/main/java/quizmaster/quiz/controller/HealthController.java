package quizmaster.quiz.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("timestamp", LocalDateTime.now());
        status.put("version", "1.0.0");
        status.put("service", "QuizMaster Backend");
        return ResponseEntity.ok(status);
    }
    
    @GetMapping("/detailed")
    public ResponseEntity<Map<String, Object>> detailedHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("version", "1.0.0");
        health.put("service", "QuizMaster Backend");
        
        Map<String, Object> components = new HashMap<>();
        components.put("database", Map.of("status", "UP"));
        components.put("websocket", Map.of("status", "UP"));
        components.put("memory", Map.of(
            "used", Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory(),
            "total", Runtime.getRuntime().totalMemory(),
            "max", Runtime.getRuntime().maxMemory()
        ));
        
        health.put("components", components);
        return ResponseEntity.ok(health);
    }
}
