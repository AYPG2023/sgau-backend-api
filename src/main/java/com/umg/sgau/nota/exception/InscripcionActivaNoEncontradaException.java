package com.umg.sgau.nota.exception;

public class InscripcionActivaNoEncontradaException extends RuntimeException {

    public InscripcionActivaNoEncontradaException(Long estudianteId, Long cursoId, Integer cicloAnio) {
        super("No existe una inscripcion activa para estudiante=" + estudianteId
                + ", curso=" + cursoId + ", ciclo=" + cicloAnio + ".");
    }
}
