package com.umg.sgau.academico.exception;

import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AcademicoExceptionHandler {
    @ExceptionHandler(VinculacionAcademicaNoEncontradaException.class)
    public ResponseEntity<Map<String, Object>> vinculacion(VinculacionAcademicaNoEncontradaException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "timestamp", LocalDateTime.now(),
                "status", 404,
                "code", "VINCULACION_ACADEMICA_NO_ENCONTRADA",
                "message", ex.getMessage()));
    }
}
