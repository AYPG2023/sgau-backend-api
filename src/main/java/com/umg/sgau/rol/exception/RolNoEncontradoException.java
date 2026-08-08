package com.umg.sgau.rol.exception;

public class RolNoEncontradoException extends RuntimeException {

    public RolNoEncontradoException(Long id) {
        super("Rol no encontrado con id: " + id);
    }

    public RolNoEncontradoException(String message) {
        super(message);
    }
}
