package com.umg.sgau.curso.exception;

public class CursoInactivoException extends RuntimeException {

    public CursoInactivoException(Long id) {
        super("El curso con ID " + id + " esta inactivo.");
    }
}
