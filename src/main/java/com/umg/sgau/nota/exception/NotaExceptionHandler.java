package com.umg.sgau.nota.exception;

import com.umg.sgau.nota.controller.NotaController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = NotaController.class)
public class NotaExceptionHandler {

    @ExceptionHandler(NotaNoEncontradaException.class)
    public ResponseEntity<String> manejarNoEncontrada(NotaNoEncontradaException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(NotaDuplicadaException.class)
    public ResponseEntity<String> manejarDuplicada(NotaDuplicadaException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(NotaInvalidaException.class)
    public ResponseEntity<String> manejarInvalida(NotaInvalidaException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
