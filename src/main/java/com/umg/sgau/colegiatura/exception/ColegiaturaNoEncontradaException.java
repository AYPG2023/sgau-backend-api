package com.umg.sgau.colegiatura.exception;

public class ColegiaturaNoEncontradaException extends RuntimeException {

    public ColegiaturaNoEncontradaException(Long id) {
        super("Colegiatura no encontrada con id: " + id);
    }
}
