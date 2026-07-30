package com.umg.sgau.curso.exception;

public class CarreraCursoInvalidaException extends RuntimeException {

    public CarreraCursoInvalidaException(Long carreraId) {
        super("El identificador de la carrera no es v\u00e1lido: " + carreraId);
    }
}
