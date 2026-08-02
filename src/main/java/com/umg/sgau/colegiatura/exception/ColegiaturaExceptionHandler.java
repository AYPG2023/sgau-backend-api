package com.umg.sgau.colegiatura.exception;

import com.umg.sgau.colegiatura.controller.ColegiaturaController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = ColegiaturaController.class)
public class ColegiaturaExceptionHandler {

    @ExceptionHandler({
            ColegiaturaNoEncontradaException.class,
            EstudianteInvalidoParaColegiaturaException.class
    })
    public ResponseEntity<String> manejarNoEncontrado(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler({
            ColegiaturaDuplicadaException.class,
            ColegiaturaSinSaldoPendienteException.class,
            PagoExcedeSaldoException.class
    })
    public ResponseEntity<String> manejarConflicto(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler({
            EstudianteInactivoParaColegiaturaException.class,
            PagoColegiaturaInvalidoException.class,
            IllegalArgumentException.class,
            IllegalStateException.class
    })
    public ResponseEntity<String> manejarSolicitudInvalida(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
