package com.umg.sgau.config;

import java.util.LinkedHashMap;
import java.util.Map;

public final class PermissionCatalog {

    private PermissionCatalog() {
    }

    public static final String USUARIOS_CREAR = "USUARIOS_CREAR";
    public static final String USUARIOS_LEER = "USUARIOS_LEER";
    public static final String USUARIOS_EDITAR = "USUARIOS_EDITAR";
    public static final String USUARIOS_ELIMINAR = "USUARIOS_ELIMINAR";
    public static final String USUARIOS_ASIGNAR_ROLES = "USUARIOS_ASIGNAR_ROLES";
    public static final String ROLES_CREAR = "ROLES_CREAR";
    public static final String ROLES_LEER = "ROLES_LEER";
    public static final String ROLES_EDITAR = "ROLES_EDITAR";
    public static final String ROLES_CAMBIAR_ESTADO = "ROLES_CAMBIAR_ESTADO";
    public static final String ROLES_ASIGNAR_PERMISOS = "ROLES_ASIGNAR_PERMISOS";
    public static final String PERMISOS_CREAR = "PERMISOS_CREAR";
    public static final String PERMISOS_LEER = "PERMISOS_LEER";
    public static final String PERMISOS_EDITAR = "PERMISOS_EDITAR";
    public static final String PERMISOS_CAMBIAR_ESTADO = "PERMISOS_CAMBIAR_ESTADO";
    public static final String CARRERAS_CREAR = "CARRERAS_CREAR";
    public static final String CARRERAS_LEER = "CARRERAS_LEER";
    public static final String CARRERAS_EDITAR = "CARRERAS_EDITAR";
    public static final String CARRERAS_CAMBIAR_ESTADO = "CARRERAS_CAMBIAR_ESTADO";
    public static final String CURSOS_CREAR = "CURSOS_CREAR";
    public static final String CURSOS_LEER = "CURSOS_LEER";
    public static final String CURSOS_EDITAR = "CURSOS_EDITAR";
    public static final String CURSOS_CAMBIAR_ESTADO = "CURSOS_CAMBIAR_ESTADO";
    public static final String CURSOS_ASIGNAR_DOCENTE = "CURSOS_ASIGNAR_DOCENTE";
    public static final String DOCENTES_CREAR = "DOCENTES_CREAR";
    public static final String DOCENTES_LEER = "DOCENTES_LEER";
    public static final String DOCENTES_EDITAR = "DOCENTES_EDITAR";
    public static final String DOCENTES_CAMBIAR_ESTADO = "DOCENTES_CAMBIAR_ESTADO";
    public static final String DOCENTES_ELIMINAR = "DOCENTES_ELIMINAR";
    public static final String ESTUDIANTES_CREAR = "ESTUDIANTES_CREAR";
    public static final String ESTUDIANTES_LEER = "ESTUDIANTES_LEER";
    public static final String ESTUDIANTES_EDITAR = "ESTUDIANTES_EDITAR";
    public static final String ESTUDIANTES_CAMBIAR_ESTADO = "ESTUDIANTES_CAMBIAR_ESTADO";
    public static final String INSCRIPCIONES_CREAR = "INSCRIPCIONES_CREAR";
    public static final String INSCRIPCIONES_LEER = "INSCRIPCIONES_LEER";
    public static final String INSCRIPCIONES_EDITAR = "INSCRIPCIONES_EDITAR";
    public static final String INSCRIPCIONES_CAMBIAR_ESTADO = "INSCRIPCIONES_CAMBIAR_ESTADO";
    public static final String NOTAS_CREAR = "NOTAS_CREAR";
    public static final String NOTAS_LEER = "NOTAS_LEER";
    public static final String NOTAS_EDITAR = "NOTAS_EDITAR";
    public static final String NOTAS_CAMBIAR_ESTADO = "NOTAS_CAMBIAR_ESTADO";
    public static final String COLEGIATURAS_CREAR = "COLEGIATURAS_CREAR";
    public static final String COLEGIATURAS_LEER = "COLEGIATURAS_LEER";
    public static final String COLEGIATURAS_EDITAR = "COLEGIATURAS_EDITAR";
    public static final String COLEGIATURAS_REGISTRAR_PAGO = "COLEGIATURAS_REGISTRAR_PAGO";
    public static final String COLEGIATURAS_CAMBIAR_ESTADO = "COLEGIATURAS_CAMBIAR_ESTADO";

    public static Map<String, String> todos() {
        Map<String, String> permisos = new LinkedHashMap<>();
        agregarModulo(permisos, "USUARIOS", "usuarios", "CREAR", "LEER", "EDITAR", "ELIMINAR", "ASIGNAR_ROLES");
        agregarModulo(permisos, "ROLES", "roles", "CREAR", "LEER", "EDITAR", "CAMBIAR_ESTADO", "ASIGNAR_PERMISOS");
        agregarModulo(permisos, "PERMISOS", "permisos", "CREAR", "LEER", "EDITAR", "CAMBIAR_ESTADO");
        agregarModulo(permisos, "CARRERAS", "carreras", "CREAR", "LEER", "EDITAR", "CAMBIAR_ESTADO");
        agregarModulo(permisos, "CURSOS", "cursos", "CREAR", "LEER", "EDITAR", "CAMBIAR_ESTADO", "ASIGNAR_DOCENTE");
        agregarModulo(permisos, "DOCENTES", "docentes", "CREAR", "LEER", "EDITAR", "CAMBIAR_ESTADO", "ELIMINAR");
        agregarModulo(permisos, "ESTUDIANTES", "estudiantes", "CREAR", "LEER", "EDITAR", "CAMBIAR_ESTADO");
        agregarModulo(permisos, "INSCRIPCIONES", "inscripciones", "CREAR", "LEER", "EDITAR", "CAMBIAR_ESTADO");
        agregarModulo(permisos, "NOTAS", "notas", "CREAR", "LEER", "EDITAR", "CAMBIAR_ESTADO");
        agregarModulo(permisos, "COLEGIATURAS", "colegiaturas", "CREAR", "LEER", "EDITAR", "REGISTRAR_PAGO", "CAMBIAR_ESTADO");
        return Map.copyOf(permisos);
    }

    private static void agregarModulo(Map<String, String> permisos, String modulo, String nombreModulo, String... acciones) {
        for (String accion : acciones) {
            permisos.put(modulo + "_" + accion, accion.replace('_', ' ') + " " + nombreModulo);
        }
    }
}
