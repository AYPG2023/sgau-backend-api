package com.umg.sgau.colegiatura.exception;

public class EstudianteInvalidoParaColegiaturaException extends RuntimeException {

    public EstudianteInvalidoParaColegiaturaException(Long estudianteId) {
        super("El estudiante no existe o no es valido para colegiatura: " + estudianteId);
    }
}
