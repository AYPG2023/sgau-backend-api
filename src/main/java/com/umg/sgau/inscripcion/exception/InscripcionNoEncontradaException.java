package com.umg.sgau.inscripcion.exception;

/**
 * Se lanza cuando no existe una inscripcion con el ID solicitado.
 * El ExceptionHandler la mapea a HTTP 404.
 */
public class InscripcionNoEncontradaException extends RuntimeException {

    public InscripcionNoEncontradaException(Long id) {
        super("No se encontro la inscripcion con ID: " + id);
    }
}
