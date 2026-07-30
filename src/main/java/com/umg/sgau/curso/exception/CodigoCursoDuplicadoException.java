package com.umg.sgau.curso.exception;

public class CodigoCursoDuplicadoException extends RuntimeException {

    public CodigoCursoDuplicadoException(String codigo) {
        super("Ya existe un curso con el c\u00f3digo: " + codigo);
    }
}
