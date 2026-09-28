package com.umg.sgau.config;

import com.umg.sgau.permiso.entity.Permiso;
import com.umg.sgau.permiso.repository.PermisoRepository;
import com.umg.sgau.rol.entity.Rol;
import com.umg.sgau.rol.repository.RolRepository;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PermissionCatalogInitializer implements ApplicationRunner {

    private final PermisoRepository permisoRepository;
    private final RolRepository rolRepository;

    public PermissionCatalogInitializer(PermisoRepository permisoRepository, RolRepository rolRepository) {
        this.permisoRepository = permisoRepository;
        this.rolRepository = rolRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        var permisosAdmin = new HashSet<Permiso>();
        PermissionCatalog.todos().forEach((codigo, nombre) -> {
            Permiso permiso = permisoRepository.findByCodigoIgnoreCase(codigo).orElseGet(Permiso::new);
            permiso.setCodigo(codigo);
            permiso.setNombre(nombre);
            permiso.setDescripcion("Autoriza " + nombre.toLowerCase());
            permiso.setActivo(true);
            permisosAdmin.add(permisoRepository.save(permiso));
        });

        rolRepository.findByCodigoIgnoreCase("ADMIN").ifPresent(admin -> asignarAlAdministrador(admin, permisosAdmin));
        asignarMatrizInicial("ESTUDIANTE", permisosAdmin, Set.of(
                PermissionCatalog.ESTUDIANTES_LEER,
                PermissionCatalog.CURSOS_LEER,
                PermissionCatalog.INSCRIPCIONES_LEER,
                PermissionCatalog.NOTAS_LEER,
                PermissionCatalog.COLEGIATURAS_LEER));
        asignarMatrizInicial("DOCENTE", permisosAdmin, Set.of(
                PermissionCatalog.CURSOS_LEER,
                PermissionCatalog.INSCRIPCIONES_LEER,
                PermissionCatalog.NOTAS_LEER,
                PermissionCatalog.NOTAS_CREAR,
                PermissionCatalog.NOTAS_EDITAR,
                PermissionCatalog.NOTAS_CAMBIAR_ESTADO));
    }

    private void asignarAlAdministrador(Rol admin, HashSet<Permiso> permisos) {
        if (admin.getPermisos() == null) {
            admin.setPermisos(new HashSet<>());
        }
        admin.getPermisos().addAll(permisos);
        rolRepository.save(admin);
    }

    private void asignarMatrizInicial(String codigoRol, Set<Permiso> catalogo, Set<String> codigos) {
        rolRepository.findByCodigoIgnoreCase(codigoRol).ifPresent(rol -> {
            if (rol.getPermisos() != null && !rol.getPermisos().isEmpty()) {
                return;
            }
            Map<String, Permiso> porCodigo = catalogo.stream()
                    .collect(java.util.stream.Collectors.toMap(Permiso::getCodigo, permiso -> permiso));
            Set<Permiso> permisos = new HashSet<>();
            codigos.forEach(codigo -> permisos.add(porCodigo.get(codigo)));
            rol.setPermisos(permisos);
            rolRepository.save(rol);
        });
    }
}
