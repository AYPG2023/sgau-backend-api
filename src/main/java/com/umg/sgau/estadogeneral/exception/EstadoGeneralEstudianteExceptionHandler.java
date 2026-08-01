package com.umg.sgau.estadogeneral.exception;

import com.umg.sgau.estadogeneral.controller.EstadoGeneralEstudianteController;
import com.umg.sgau.estudiante.exception.EstudianteNoEncontradoException;
import com.umg.sgau.historialacademico.exception.DatosHistorialInconsistentesException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = EstadoGeneralEstudianteController.class)
public class EstadoGeneralEstudianteExceptionHandler {

    @ExceptionHandler(EstudianteNoEncontradoException.class)
    public ResponseEntity<String> manejarEstudianteNoEncontrado(EstudianteNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> manejarSolicitudInvalida(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> manejarParametroInvalido(ConstraintViolationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler({
            DatosEstadoGeneralInconsistentesException.class,
            DatosHistorialInconsistentesException.class
    })
    public ResponseEntity<String> manejarInconsistencia(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("No fue posible construir el estado general del estudiante.");
    }
}
