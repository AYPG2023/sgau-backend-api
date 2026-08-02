package com.umg.sgau.inscripcion.exception;

public class CursoInactivoParaInscripcionException extends RuntimeException {

    public CursoInactivoParaInscripcionException(Long cursoId) {
        super("El curso con ID " + cursoId + " esta inactivo para inscripcion.");
    }
}
