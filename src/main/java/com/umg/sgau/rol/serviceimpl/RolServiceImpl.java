package com.umg.sgau.rol.serviceimpl;

import com.umg.sgau.permiso.entity.Permiso;
import com.umg.sgau.permiso.exception.PermisoInactivoException;
import com.umg.sgau.permiso.exception.PermisoNoEncontradoException;
import com.umg.sgau.permiso.repository.PermisoRepository;
import com.umg.sgau.rol.entity.Rol;
import com.umg.sgau.rol.exception.CodigoRolDuplicadoException;
import com.umg.sgau.rol.exception.NombreRolDuplicadoException;
import com.umg.sgau.rol.exception.RolInactivoException;
import com.umg.sgau.rol.exception.RolNoEncontradoException;
import com.umg.sgau.rol.repository.RolRepository;
import com.umg.sgau.rol.service.RolService;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RolServiceImpl implements RolService {

    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;

    public RolServiceImpl(RolRepository rolRepository, PermisoRepository permisoRepository) {
        this.rolRepository = rolRepository;
        this.permisoRepository = permisoRepository;
    }

    @Override
    @Transactional
    public Rol crear(Rol rol) {
        String codigo = normalizarCodigo(rol.getCodigo());
        String nombre = normalizarTextoObligatorio(rol.getNombre());
        String descripcion = normalizarTextoOpcional(rol.getDescripcion());

        validarDuplicadosAlCrear(codigo, nombre);

        rol.setCodigo(codigo);
        rol.setNombre(nombre);
        rol.setDescripcion(descripcion);
        rol.setActivo(true);
        rol.setPermisos(new HashSet<>());
        return rolRepository.save(rol);
    }

    @Override
    @Transactional(readOnly = true)
    public Rol obtenerPorId(Long id) {
        return rolRepository.findWithPermisosById(id)
                .orElseThrow(() -> new RolNoEncontradoException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Rol> listar(String texto, Boolean activo, Pageable pageable) {
        String textoNormalizado = texto == null ? null : texto.trim();
        return rolRepository.buscarConFiltros(textoNormalizado, activo, pageable);
    }

    @Override
    @Transactional
    public Rol actualizar(Long id, Rol rol) {
        Rol existente = obtenerPorId(id);
        String codigo = normalizarCodigo(rol.getCodigo());
        String nombre = normalizarTextoObligatorio(rol.getNombre());
        String descripcion = normalizarTextoOpcional(rol.getDescripcion());

        if (rolRepository.existsByCodigoIgnoreCaseAndIdNot(codigo, id)) {
            throw new CodigoRolDuplicadoException(codigo);
        }
        if (rolRepository.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new NombreRolDuplicadoException(nombre);
        }

        existente.setCodigo(codigo);
        existente.setNombre(nombre);
        existente.setDescripcion(descripcion);
        return rolRepository.save(existente);
    }

    @Override
    @Transactional
    public Rol cambiarEstado(Long id, Boolean activo) {
        Rol rol = obtenerPorId(id);
        rol.setActivo(activo);
        return rolRepository.save(rol);
    }

    @Override
    @Transactional
    public Rol asignarPermisos(Long rolId, Set<Long> permisoIds) {
        if (permisoIds == null) {
            throw new IllegalArgumentException("La lista de permisos es obligatoria");
        }

        Rol rol = obtenerPorId(rolId);
        if (!Boolean.TRUE.equals(rol.getActivo())) {
            throw new RolInactivoException("No se pueden asignar permisos a un rol inactivo");
        }

        List<Permiso> permisosEncontrados = permisoRepository.findAllById(permisoIds);
        if (permisosEncontrados.size() != permisoIds.size()) {
            throw new PermisoNoEncontradoException("Uno o mas permisos no existen");
        }

        boolean contieneInactivos = permisosEncontrados.stream()
                .anyMatch(permiso -> !Boolean.TRUE.equals(permiso.getActivo()));
        if (contieneInactivos) {
            throw new PermisoInactivoException("No se pueden asignar permisos inactivos");
        }

        rol.setPermisos(new HashSet<>(permisosEncontrados));
        return rolRepository.save(rol);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Permiso> obtenerPermisosDelRol(Long rolId) {
        return new HashSet<>(obtenerPorId(rolId).getPermisos());
    }

    private void validarDuplicadosAlCrear(String codigo, String nombre) {
        if (rolRepository.existsByCodigoIgnoreCase(codigo)) {
            throw new CodigoRolDuplicadoException(codigo);
        }
        if (rolRepository.existsByNombreIgnoreCase(nombre)) {
            throw new NombreRolDuplicadoException(nombre);
        }
    }

    private String normalizarCodigo(String codigo) {
        return codigo.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizarTextoObligatorio(String texto) {
        return texto.trim();
    }

    private String normalizarTextoOpcional(String texto) {
        return texto == null ? "" : texto.trim();
    }
}
