package com.umg.sgau.permiso.exception;

public class PermisoNoEncontradoException extends RuntimeException {

    public PermisoNoEncontradoException(Long id) {
        super("Permiso no encontrado con id: " + id);
    }

    public PermisoNoEncontradoException(String message) {
        super(message);
    }
}
