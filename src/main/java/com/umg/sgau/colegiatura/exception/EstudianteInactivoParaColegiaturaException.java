package com.umg.sgau.colegiatura.exception;

public class EstudianteInactivoParaColegiaturaException extends RuntimeException {

    public EstudianteInactivoParaColegiaturaException(Long estudianteId) {
        super("El estudiante con ID " + estudianteId + " esta inactivo para colegiatura.");
    }
}
