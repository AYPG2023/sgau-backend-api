package com.umg.sgau.usuario.exception;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class AltaUsuarioValidationException extends RuntimeException {
    private final Map<String, String> errores;

    public AltaUsuarioValidationException(Map<String, String> errores) {
        super("La solicitud contiene datos invalidos");
        this.errores = Collections.unmodifiableMap(new LinkedHashMap<>(errores));
    }

    public Map<String, String> getErrores() {
        return errores;
    }
}
