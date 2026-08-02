package com.umg.sgau.inscripcion.exception;

/**
 * Se lanza cuando se intenta crear o modificar una inscripcion
 * y ya existe una activa con los mismos datos identificadores.
 * El ExceptionHandler la mapea a HTTP 409.
 */
public class InscripcionDuplicadaException extends RuntimeException {

    public InscripcionDuplicadaException(Long estudianteId, Long cursoId, Integer cicloAnio) {
        super(String.format(
                "Ya existe una inscripcion activa: estudiante=%d, curso=%d, ciclo=%d",
                estudianteId, cursoId, cicloAnio));
    }

    public InscripcionDuplicadaException(
            Long estudianteId, Long carreraId, String grado,
            String seccion, Integer cicloAnio) {
        super(String.format(
                "Ya existe una inscripcion activa: estudiante=%d, carrera=%d, grado=%s, seccion=%s, ciclo=%d",
                estudianteId, carreraId, grado, seccion, cicloAnio));
    }
}
