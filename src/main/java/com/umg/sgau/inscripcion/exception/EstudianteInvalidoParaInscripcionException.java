package com.umg.sgau.inscripcion.exception;

public class EstudianteInvalidoParaInscripcionException extends RuntimeException {

    public EstudianteInvalidoParaInscripcionException(Long estudianteId) {
        super("El estudiante no existe o no es valido para inscripcion: " + estudianteId);
    }
}
