package com.umg.sgau.academico.exception;

import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AcademicIdentityExceptionHandler {
    @ExceptionHandler(IdentidadAcademicaInconsistenteException.class)
    public ResponseEntity<Map<String,Object>> manejar(IdentidadAcademicaInconsistenteException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "timestamp", LocalDateTime.now(), "status", 409,
                "error", "Conflict", "message", ex.getMessage()));
    }
}
