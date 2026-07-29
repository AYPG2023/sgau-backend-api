package com.umg.sgau.carrera.exception;

public class CarreraNoEncontradaException extends RuntimeException {

    public CarreraNoEncontradaException(Long id) {
        super("No se encontró la carrera con ID: " + id);
    }
}
