package com.umg.sgau.usuario.exception;

import com.umg.sgau.rol.exception.RolInactivoException;
import com.umg.sgau.rol.exception.RolNoEncontradoException;
import com.umg.sgau.usuario.controller.UsuarioController;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = UsuarioController.class)
public class UsuarioExceptionHandler {

    @ExceptionHandler({UsuarioNoEncontradoException.class, RolNoEncontradoException.class})
    public ResponseEntity<Map<String, Object>> manejarNoEncontrado(RuntimeException exception) {
        return crearRespuesta(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(RolInactivoException.class)
    public ResponseEntity<Map<String, Object>> manejarConflicto(RuntimeException exception) {
        return crearRespuesta(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> manejarSolicitudInvalida(IllegalArgumentException exception) {
        return crearRespuesta(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    private ResponseEntity<Map<String, Object>> crearRespuesta(HttpStatus estado, String mensaje) {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("timestamp", LocalDateTime.now());
        respuesta.put("status", estado.value());
        respuesta.put("error", estado.getReasonPhrase());
        respuesta.put("message", mensaje);
        return ResponseEntity.status(estado).body(respuesta);
    }
}
