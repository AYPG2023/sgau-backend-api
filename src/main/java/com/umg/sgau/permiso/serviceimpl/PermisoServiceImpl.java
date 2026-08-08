package com.umg.sgau.permiso.serviceimpl;

import com.umg.sgau.permiso.entity.Permiso;
import com.umg.sgau.permiso.exception.CodigoPermisoDuplicadoException;
import com.umg.sgau.permiso.exception.NombrePermisoDuplicadoException;
import com.umg.sgau.permiso.exception.PermisoNoEncontradoException;
import com.umg.sgau.permiso.repository.PermisoRepository;
import com.umg.sgau.permiso.service.PermisoService;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PermisoServiceImpl implements PermisoService {

    private final PermisoRepository permisoRepository;

    public PermisoServiceImpl(PermisoRepository permisoRepository) {
        this.permisoRepository = permisoRepository;
    }

    @Override
    @Transactional
    public Permiso crear(Permiso permiso) {
        String codigo = normalizarCodigo(permiso.getCodigo());
        String nombre = normalizarTextoObligatorio(permiso.getNombre());
        String descripcion = normalizarTextoOpcional(permiso.getDescripcion());

        validarDuplicadosAlCrear(codigo, nombre);

        permiso.setCodigo(codigo);
        permiso.setNombre(nombre);
        permiso.setDescripcion(descripcion);
        permiso.setActivo(true);
        return permisoRepository.save(permiso);
    }

    @Override
    @Transactional(readOnly = true)
    public Permiso obtenerPorId(Long id) {
        return permisoRepository.findById(id)
                .orElseThrow(() -> new PermisoNoEncontradoException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Permiso> listar(String texto, Boolean activo, Pageable pageable) {
        String textoNormalizado = texto == null ? null : texto.trim();
        return permisoRepository.buscarConFiltros(textoNormalizado, activo, pageable);
    }

    @Override
    @Transactional
    public Permiso actualizar(Long id, Permiso permiso) {
        Permiso existente = obtenerPorId(id);
        String codigo = normalizarCodigo(permiso.getCodigo());
        String nombre = normalizarTextoObligatorio(permiso.getNombre());
        String descripcion = normalizarTextoOpcional(permiso.getDescripcion());

        if (permisoRepository.existsByCodigoIgnoreCaseAndIdNot(codigo, id)) {
            throw new CodigoPermisoDuplicadoException(codigo);
        }
        if (permisoRepository.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new NombrePermisoDuplicadoException(nombre);
        }

        existente.setCodigo(codigo);
        existente.setNombre(nombre);
        existente.setDescripcion(descripcion);
        return permisoRepository.save(existente);
    }

    @Override
    @Transactional
    public Permiso cambiarEstado(Long id, Boolean activo) {
        Permiso permiso = obtenerPorId(id);
        permiso.setActivo(activo);
        return permisoRepository.save(permiso);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Permiso> obtenerPermisosActivos() {
        return permisoRepository.findAll()
                .stream()
                .filter(permiso -> Boolean.TRUE.equals(permiso.getActivo()))
                .toList();
    }

    private void validarDuplicadosAlCrear(String codigo, String nombre) {
        if (permisoRepository.existsByCodigoIgnoreCase(codigo)) {
            throw new CodigoPermisoDuplicadoException(codigo);
        }
        if (permisoRepository.existsByNombreIgnoreCase(nombre)) {
            throw new NombrePermisoDuplicadoException(nombre);
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
