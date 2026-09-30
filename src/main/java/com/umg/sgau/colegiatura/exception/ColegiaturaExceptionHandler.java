package com.umg.sgau.colegiatura.exception;

import com.umg.sgau.colegiatura.controller.ColegiaturaController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice(assignableTypes = ColegiaturaController.class)
public class ColegiaturaExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> manejarValidacion(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("message", "Revisa los campos indicados.");
        body.put("fieldErrors", fieldErrors);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> manejarFormatoInvalido(HttpMessageNotReadableException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        fieldErrors.put("fechaEmision", "Verifica que las fechas usen el formato AAAA-MM-DD y sean válidas.");
        fieldErrors.put("fechaVencimiento", "Verifica que las fechas usen el formato AAAA-MM-DD y sean válidas.");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("message", "El formato de la solicitud no es válido.");
        body.put("fieldErrors", fieldErrors);
        return ResponseEntity.badRequest().body(body);
    }

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
    public ResponseEntity<Map<String, Object>> manejarSolicitudInvalida(RuntimeException ex) {
        String message = ex.getMessage() == null ? "La solicitud no es válida." : ex.getMessage();
        String normalized = message.toLowerCase(java.util.Locale.ROOT);
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        if (normalized.contains("estudiante")) fieldErrors.put("estudianteId", message);
        if (normalized.contains("ciclo")) fieldErrors.put("cicloAnio", message);
        if (normalized.contains("fecha")) fieldErrors.put("fechaVencimiento", message);
        if (normalized.contains("concepto")) fieldErrors.put("concepto", message);
        if (normalized.contains("monto") || normalized.contains("pago")) fieldErrors.put("montoTotal", message);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("message", message);
        body.put("fieldErrors", fieldErrors);
        return ResponseEntity.badRequest().body(body);
    }
}
