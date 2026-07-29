package com.umg.sgau.carrera.exception;

public class NombreCarreraDuplicadoException extends RuntimeException {

    public NombreCarreraDuplicadoException(String nombre) {
        super("Ya existe una carrera con el nombre: " + nombre);
    }
}
