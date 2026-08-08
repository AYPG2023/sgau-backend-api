package com.umg.sgau.permiso.exception;

public class NombrePermisoDuplicadoException extends RuntimeException {

    public NombrePermisoDuplicadoException(String nombre) {
        super("Ya existe un permiso con el nombre: " + nombre);
    }
}
