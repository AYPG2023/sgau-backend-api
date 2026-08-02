package com.umg.sgau.curso.exception;

public class CursoSinDocenteException extends RuntimeException {

    public CursoSinDocenteException(Long id) {
        super("El curso con ID " + id + " no tiene docente asignado.");
    }
}
