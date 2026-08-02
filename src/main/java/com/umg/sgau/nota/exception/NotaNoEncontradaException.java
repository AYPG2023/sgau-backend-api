package com.umg.sgau.nota.exception;

public class NotaNoEncontradaException extends RuntimeException {

    public NotaNoEncontradaException(Long id) {
        super("Nota no encontrada con id: " + id);
    }
}
