package com.umg.sgau.permiso.exception;

import com.umg.sgau.permiso.controller.PermisoController;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = PermisoController.class)
public class PermisoExceptionHandler {

    @ExceptionHandler(PermisoNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> manejarNoEncontrado(PermisoNoEncontradoException exception) {
        return crearRespuesta(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler({
            CodigoPermisoDuplicadoException.class,
            NombrePermisoDuplicadoException.class,
            PermisoInactivoException.class
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
