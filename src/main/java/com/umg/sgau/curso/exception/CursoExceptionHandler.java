package com.umg.sgau.curso.exception;

import com.umg.sgau.curso.controller.CursoController;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = CursoController.class)
public class CursoExceptionHandler {

    @ExceptionHandler(CursoNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> manejarCursoNoEncontrado(
            CursoNoEncontradoException exception) {
        return crearRespuesta(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(CodigoCursoDuplicadoException.class)
    public ResponseEntity<Map<String, Object>> manejarCodigoCursoDuplicado(
            CodigoCursoDuplicadoException exception) {
        return crearRespuesta(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(CursoAcademicoDuplicadoException.class)
    public ResponseEntity<Map<String, Object>> manejarCursoAcademicoDuplicado(
            CursoAcademicoDuplicadoException exception) {
        return crearRespuesta(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(CarreraCursoInvalidaException.class)
    public ResponseEntity<Map<String, Object>> manejarCarreraCursoInvalida(
            CarreraCursoInvalidaException exception) {
        return crearRespuesta(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(DocenteCursoInvalidoException.class)
    public ResponseEntity<Map<String, Object>> manejarDocenteCursoInvalido(
            DocenteCursoInvalidoException exception) {
        return crearRespuesta(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    private ResponseEntity<Map<String, Object>> crearRespuesta(
            HttpStatus estado,
            String mensaje) {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("timestamp", LocalDateTime.now());
        respuesta.put("status", estado.value());
        respuesta.put("error", estado.getReasonPhrase());
        respuesta.put("message", mensaje);

        return ResponseEntity
                .status(estado)
                .body(respuesta);
    }
}
