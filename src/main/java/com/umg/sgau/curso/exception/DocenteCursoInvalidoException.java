package com.umg.sgau.curso.exception;

public class DocenteCursoInvalidoException extends RuntimeException {

    public DocenteCursoInvalidoException(Long docenteId) {
        super("El identificador del docente no es v\u00e1lido: " + docenteId);
    }
}
