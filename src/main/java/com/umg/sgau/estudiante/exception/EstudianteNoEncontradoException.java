package com.umg.sgau.estudiante.exception;

/**
 * Se lanza cuando no existe un estudiante con el identificador solicitado.
 */
public class EstudianteNoEncontradoException extends RuntimeException {

    public EstudianteNoEncontradoException(Long id) {
        super("Estudiante no encontrado con id: " + id);
    }

}