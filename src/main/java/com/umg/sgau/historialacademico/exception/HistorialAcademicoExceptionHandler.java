package com.umg.sgau.historialacademico.exception;

import com.umg.sgau.estudiante.controller.EstudianteController;
import com.umg.sgau.estudiante.exception.EstudianteNoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = EstudianteController.class)
public class HistorialAcademicoExceptionHandler {

    @ExceptionHandler(EstudianteNoEncontradoException.class)
    public ResponseEntity<String> manejarEstudianteNoEncontrado(EstudianteNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> manejarSolicitudInvalida(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(DatosHistorialInconsistentesException.class)
    public ResponseEntity<String> manejarDatosInconsistentes(DatosHistorialInconsistentesException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }
}
