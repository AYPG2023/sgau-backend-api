package com.umg.sgau.curso.exception;

public class CursoAcademicoDuplicadoException extends RuntimeException {

    public CursoAcademicoDuplicadoException(
            String nombre,
            Long carreraId,
            Integer cicloAnio) {
        super(
                "Ya existe un curso activo con el nombre "
                        + nombre
                        + " para la carrera "
                        + carreraId
                        + " en el ciclo "
                        + cicloAnio);
    }
}
