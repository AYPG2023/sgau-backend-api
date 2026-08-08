package com.umg.sgau.rol.exception;

public class CodigoRolDuplicadoException extends RuntimeException {

    public CodigoRolDuplicadoException(String codigo) {
        super("Ya existe un rol con el codigo: " + codigo);
    }
}
