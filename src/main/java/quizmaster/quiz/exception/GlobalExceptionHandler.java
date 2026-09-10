package quizmaster.quiz.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        Map<String, String> error = new HashMap<>();
        // Envia a mensagem exata (ex: "Energia insuficiente...") para o frontend
        error.put("message", ex.getMessage());
        
        // Retorna 422 (Unprocessable Entity) para que o Flutter pegue a mensagem limpa
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }
}
