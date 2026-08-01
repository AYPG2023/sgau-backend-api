package com.umg.sgau.curso.exception;

public class CarreraInactivaParaCursoException extends RuntimeException {

    public CarreraInactivaParaCursoException(Long carreraId) {
        super("La carrera con ID " + carreraId + " esta inactiva y no puede asociarse a un curso.");
    }
}
