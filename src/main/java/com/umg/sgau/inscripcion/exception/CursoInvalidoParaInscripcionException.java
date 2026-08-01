package com.umg.sgau.inscripcion.exception;

public class CursoInvalidoParaInscripcionException extends RuntimeException {

    public CursoInvalidoParaInscripcionException(Long cursoId) {
        super("El curso no existe o no es valido para inscripcion: " + cursoId);
    }
}
