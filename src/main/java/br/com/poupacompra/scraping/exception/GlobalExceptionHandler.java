package br.com.poupacompra.scraping.exception;

import java.time.LocalDateTime;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(ScrapingException.class)
    public ResponseEntity<Map<String, Object>> handleScrapingException(ScrapingException ex) {
        log.error("Erro de scraping: {}", ex.getMessage(), ex);
        
        Map<String, Object> body = Map.of(
            "error", ex.getMessage(),
            "url", ex.getUrl() != null ? ex.getUrl() : "",
            "timestamp", LocalDateTime.now().toString()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Argumento inválido: {}", ex.getMessage());
        
        Map<String, Object> body = Map.of(
            "error", ex.getMessage(),
            "timestamp", LocalDateTime.now().toString()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Erro inesperado: {}", ex.getMessage(), ex);
        
        Map<String, Object> body = Map.of(
            "error", "Erro interno do servidor: " + ex.getMessage(),
            "timestamp", LocalDateTime.now().toString()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
