package com.umg.sgau.curso.exception;

public class CursoNoEncontradoException extends RuntimeException {

    public CursoNoEncontradoException(Long id) {
        super("No se encontr\u00f3 el curso con ID: " + id);
    }
}
