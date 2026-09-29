package com.umg.sgau.academico.service;

import com.umg.sgau.academico.exception.IdentidadAcademicaInconsistenteException;
import com.umg.sgau.usuario.entity.Usuario;
import java.text.Normalizer;
import java.util.Locale;

/** Usuario es la fuente de verdad de nombre, apellido y correo compartidos. */
public final class AcademicIdentityPolicy {
    private AcademicIdentityPolicy() {}

    public static void validar(Usuario usuario, String nombre, String apellido, String correo) {
        if (!normalizar(usuario.getNombre()).equals(normalizar(nombre)))
            throw new IdentidadAcademicaInconsistenteException("el nombre no coincide");
        if (!normalizar(usuario.getApellido()).equals(normalizar(apellido)))
            throw new IdentidadAcademicaInconsistenteException("el apellido no coincide");
        if (!normalizarCorreo(usuario.getEmail()).equals(normalizarCorreo(correo)))
            throw new IdentidadAcademicaInconsistenteException("el correo no coincide");
    }

    public static String normalizar(String valor) {
        if (valor == null) return "";
        String sinAcentos = Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return sinAcentos.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    public static String normalizarCorreo(String valor) {
        return valor == null ? "" : valor.trim().toLowerCase(Locale.ROOT);
    }
}
