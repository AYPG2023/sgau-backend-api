package com.umg.sgau.academico.exception;

public class VinculacionAcademicaNoEncontradaException extends RuntimeException {
    public VinculacionAcademicaNoEncontradaException(String tipo) {
        super("El usuario autenticado no tiene un registro de " + tipo + " vinculado a su correo.");
    }
}
