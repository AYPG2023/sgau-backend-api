package com.umg.sgau.nota.exception;

public class EstudianteInvalidoParaNotaException extends RuntimeException {

    public EstudianteInvalidoParaNotaException(Long estudianteId) {
        super("El estudiante no existe o no es valido para nota: " + estudianteId);
    }
}
