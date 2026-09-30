package com.umg.sgau.colegiatura.exception;

import com.umg.sgau.colegiatura.controller.SolicitudPagoController;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = SolicitudPagoController.class)
public class SolicitudPagoExceptionHandler {
    @ExceptionHandler(ColegiaturaNoEncontradaException.class)
    public ResponseEntity<String> noEncontrada(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler({PagoExcedeSaldoException.class, DataIntegrityViolationException.class})
    public ResponseEntity<String> conflicto(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex instanceof DataIntegrityViolationException
                ? "Boleta o clave de idempotencia duplicada; consulta el historial antes de reintentar." : ex.getMessage());
    }

    @ExceptionHandler({PagoColegiaturaInvalidoException.class, IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<String> invalida(RuntimeException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
