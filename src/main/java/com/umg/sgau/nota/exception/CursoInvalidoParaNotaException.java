package com.umg.sgau.nota.exception;

public class CursoInvalidoParaNotaException extends RuntimeException {

    public CursoInvalidoParaNotaException(Long cursoId) {
        super("El curso no existe o no es valido para nota: " + cursoId);
    }
}
