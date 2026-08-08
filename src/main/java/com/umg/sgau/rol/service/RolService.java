package com.umg.sgau.rol.service;

import com.umg.sgau.permiso.entity.Permiso;
import com.umg.sgau.rol.entity.Rol;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RolService {

    Rol crear(Rol rol);

    Rol obtenerPorId(Long id);

    Page<Rol> listar(String texto, Boolean activo, Pageable pageable);

    Rol actualizar(Long id, Rol rol);

    Rol cambiarEstado(Long id, Boolean activo);

    Rol asignarPermisos(Long rolId, Set<Long> permisoIds);

    Set<Permiso> obtenerPermisosDelRol(Long rolId);
}
