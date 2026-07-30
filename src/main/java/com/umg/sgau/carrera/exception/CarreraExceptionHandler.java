package com.umg.sgau.carrera.exception;

import com.umg.sgau.carrera.controller.CarreraController;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = CarreraController.class)
public class CarreraExceptionHandler {

    @ExceptionHandler(CarreraNoEncontradaException.class)
    public ResponseEntity<Map<String, Object>> manejarCarreraNoEncontrada(
            CarreraNoEncontradaException exception) {
        return crearRespuesta(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(CodigoCarreraDuplicadoException.class)
    public ResponseEntity<Map<String, Object>> manejarCodigoCarreraDuplicado(
            CodigoCarreraDuplicadoException exception) {
        return crearRespuesta(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(NombreCarreraDuplicadoException.class)
    public ResponseEntity<Map<String, Object>> manejarNombreCarreraDuplicado(
            NombreCarreraDuplicadoException exception) {
        return crearRespuesta(HttpStatus.CONFLICT, exception.getMessage());
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
