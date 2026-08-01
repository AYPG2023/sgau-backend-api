package com.umg.sgau.inscripcion.exception;

import com.umg.sgau.inscripcion.controller.InscripcionController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Convierte excepciones del dominio Inscripcion en respuestas HTTP estructuradas.
 * Solo aplica al InscripcionController gracias a assignableTypes.
 */
@RestControllerAdvice(assignableTypes = InscripcionController.class)
public class InscripcionExceptionHandler {

    @ExceptionHandler(InscripcionNoEncontradaException.class)
    public ResponseEntity<Map<String, Object>> manejarNoEncontrada(
            InscripcionNoEncontradaException ex) {
        return crearRespuesta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(InscripcionDuplicadaException.class)
    public ResponseEntity<Map<String, Object>> manejarDuplicada(
            InscripcionDuplicadaException ex) {
        return crearRespuesta(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler({
            EstudianteInvalidoParaInscripcionException.class,
            CursoInvalidoParaInscripcionException.class
    })
    public ResponseEntity<Map<String, Object>> manejarReferenciaInexistente(RuntimeException ex) {
        return crearRespuesta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler({
            EstudianteInactivoParaInscripcionException.class,
            CursoInactivoParaInscripcionException.class,
            CursoNoPerteneceCarreraException.class
    })
    public ResponseEntity<Map<String, Object>> manejarReferenciaInvalida(RuntimeException ex) {
        return crearRespuesta(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> manejarEstadoInvalido(
            IllegalStateException ex) {
        return crearRespuesta(HttpStatus.CONFLICT, ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> crearRespuesta(
            HttpStatus estado, String mensaje) {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("timestamp", LocalDateTime.now());
        respuesta.put("status", estado.value());
        respuesta.put("error", estado.getReasonPhrase());
        respuesta.put("message", mensaje);
        return ResponseEntity.status(estado).body(respuesta);
    }
}
