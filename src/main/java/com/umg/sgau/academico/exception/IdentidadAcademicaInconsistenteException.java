package com.umg.sgau.academico.exception;

public class IdentidadAcademicaInconsistenteException extends IllegalArgumentException {
    public IdentidadAcademicaInconsistenteException(String detalle) {
        super("La cuenta y el perfil academico representan identidades contradictorias: " + detalle
                + ". El vinculo requiere revision manual.");
    }
}
