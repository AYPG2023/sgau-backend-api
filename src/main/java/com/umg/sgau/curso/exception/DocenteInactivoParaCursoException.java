package com.umg.sgau.curso.exception;

public class DocenteInactivoParaCursoException extends RuntimeException {

    public DocenteInactivoParaCursoException(Long docenteId) {
        super("El docente con ID " + docenteId + " esta inactivo y no puede asignarse a un curso.");
    }
}
