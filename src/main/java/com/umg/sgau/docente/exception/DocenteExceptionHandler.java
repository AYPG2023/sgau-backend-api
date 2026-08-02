package com.umg.sgau.docente.exception;

import com.umg.sgau.docente.controller.DocenteController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = DocenteController.class)
public class DocenteExceptionHandler {

    @ExceptionHandler(DocenteNoEncontradoException.class)
    public ResponseEntity<String> manejarNoEncontrado(DocenteNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(DocenteDuplicadoException.class)
    public ResponseEntity<String> manejarDuplicado(DocenteDuplicadoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
}
