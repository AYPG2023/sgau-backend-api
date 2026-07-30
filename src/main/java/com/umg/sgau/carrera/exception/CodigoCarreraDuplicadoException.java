package com.umg.sgau.carrera.exception;

public class CodigoCarreraDuplicadoException extends RuntimeException {

    public CodigoCarreraDuplicadoException(String codigo) {
        super("Ya existe una carrera con el código: " + codigo);
    }
}
