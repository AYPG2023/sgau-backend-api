package com.umg.sgau.inscripcion.exception;

public class EstudianteInactivoParaInscripcionException extends RuntimeException {

    public EstudianteInactivoParaInscripcionException(Long estudianteId) {
        super("El estudiante con ID " + estudianteId + " esta inactivo para inscripcion.");
    }
}
