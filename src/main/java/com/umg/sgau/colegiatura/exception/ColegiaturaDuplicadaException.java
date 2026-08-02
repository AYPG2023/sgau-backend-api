package com.umg.sgau.colegiatura.exception;

public class ColegiaturaDuplicadaException extends RuntimeException {

    public ColegiaturaDuplicadaException(Long estudianteId, Integer cicloAnio, String concepto) {
        super("Ya existe una colegiatura activa para estudiante=" + estudianteId
                + ", ciclo=" + cicloAnio + ", concepto=" + concepto + ".");
    }
}
