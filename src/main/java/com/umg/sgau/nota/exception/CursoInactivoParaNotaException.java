package com.umg.sgau.nota.exception;

public class CursoInactivoParaNotaException extends RuntimeException {

    public CursoInactivoParaNotaException(Long cursoId) {
        super("El curso con ID " + cursoId + " esta inactivo para registrar notas.");
    }
}
