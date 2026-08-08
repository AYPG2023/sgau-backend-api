package com.umg.sgau.rol.exception;

import com.umg.sgau.rol.controller.RolController;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = RolController.class)
public class RolExceptionHandler {

    @ExceptionHandler(RolNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> manejarNoEncontrado(RolNoEncontradoException exception) {
        return crearRespuesta(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler({
            CodigoRolDuplicadoException.class,
            NombreRolDuplicadoException.class,
            RolInactivoException.class
    })
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
