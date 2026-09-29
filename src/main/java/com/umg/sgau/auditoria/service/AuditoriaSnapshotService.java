package com.umg.sgau.auditoria.service;

import com.umg.sgau.carrera.entity.Carrera;
import com.umg.sgau.colegiatura.entity.Colegiatura;
import com.umg.sgau.curso.entity.Curso;
import com.umg.sgau.docente.entity.Docente;
import com.umg.sgau.estudiante.entity.Estudiante;
import com.umg.sgau.inscripcion.entity.Inscripcion;
import com.umg.sgau.nota.entity.Nota;
import com.umg.sgau.permiso.entity.Permiso;
import com.umg.sgau.rol.entity.Rol;
import com.umg.sgau.usuario.entity.Usuario;
import jakarta.persistence.EntityManager;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditoriaSnapshotService {
    private static final Map<String, Class<?>> TIPOS = Map.ofEntries(
            Map.entry("USUARIOS", Usuario.class), Map.entry("ROLES", Rol.class),
            Map.entry("PERMISOS", Permiso.class), Map.entry("CARRERAS", Carrera.class),
            Map.entry("CURSOS", Curso.class), Map.entry("DOCENTES", Docente.class),
            Map.entry("ESTUDIANTES", Estudiante.class), Map.entry("INSCRIPCIONES", Inscripcion.class),
            Map.entry("NOTAS", Nota.class), Map.entry("COLEGIATURAS", Colegiatura.class));
    private final EntityManager entityManager;
    public AuditoriaSnapshotService(EntityManager entityManager) { this.entityManager = entityManager; }

    @Transactional(readOnly = true)
    public Map<String, Object> obtener(String modulo, String id) {
        Class<?> tipo = TIPOS.get(modulo);
        if (tipo == null || id == null || !id.matches("\\d+")) return null;
        Object entidad = entityManager.find(tipo, Long.valueOf(id));
        return entidad == null ? null : describir(entidad);
    }

    private Map<String, Object> describir(Object entidad) {
        Map<String, Object> valores = new LinkedHashMap<>();
        for (Field campo : entidad.getClass().getDeclaredFields()) {
            if (Modifier.isStatic(campo.getModifiers()) || campo.getName().equals("password")) continue;
            try {
                campo.setAccessible(true); Object valor = campo.get(entidad);
                if (valor == null || esEscalar(valor)) valores.put(campo.getName(), valor);
                else if (valor instanceof Collection<?> coleccion)
                    valores.put(campo.getName()+"Ids", coleccion.stream().map(this::idRelacionado).toList());
                else valores.put(campo.getName()+"Id", idRelacionado(valor));
            } catch (ReflectiveOperationException ignored) { }
        }
        return valores;
    }

    private boolean esEscalar(Object valor) {
        return valor instanceof String || valor instanceof Number || valor instanceof Boolean || valor instanceof Enum<?> || valor instanceof Temporal;
    }
    private Object idRelacionado(Object valor) {
        try { Method getter=valor.getClass().getMethod("getId"); return getter.invoke(valor); }
        catch (ReflectiveOperationException ex) { return null; }
    }
}
