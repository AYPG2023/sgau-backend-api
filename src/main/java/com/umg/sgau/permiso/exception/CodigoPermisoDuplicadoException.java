package com.umg.sgau.permiso.exception;

public class CodigoPermisoDuplicadoException extends RuntimeException {

    public CodigoPermisoDuplicadoException(String codigo) {
        super("Ya existe un permiso con el codigo: " + codigo);
    }
}
