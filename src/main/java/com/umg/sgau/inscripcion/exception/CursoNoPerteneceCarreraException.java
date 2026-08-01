package com.umg.sgau.inscripcion.exception;

public class CursoNoPerteneceCarreraException extends RuntimeException {

    public CursoNoPerteneceCarreraException(Long cursoId, Long carreraId) {
        super("El curso " + cursoId + " no pertenece a la carrera " + carreraId + ".");
    }
}
