package com.umg.sgau.permiso.service;

import com.umg.sgau.permiso.entity.Permiso;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PermisoService {

    Permiso crear(Permiso permiso);

    Permiso obtenerPorId(Long id);

    Page<Permiso> listar(String texto, Boolean activo, Pageable pageable);

    Permiso actualizar(Long id, Permiso permiso);

    Permiso cambiarEstado(Long id, Boolean activo);

    List<Permiso> obtenerPermisosActivos();
}
