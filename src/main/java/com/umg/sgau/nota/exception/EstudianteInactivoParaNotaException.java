package com.umg.sgau.nota.exception;

public class EstudianteInactivoParaNotaException extends RuntimeException {

    public EstudianteInactivoParaNotaException(Long estudianteId) {
        super("El estudiante con ID " + estudianteId + " esta inactivo para registrar notas.");
    }
}
