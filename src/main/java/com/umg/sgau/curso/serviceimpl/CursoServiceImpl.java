package com.umg.sgau.curso.serviceimpl;

import com.umg.sgau.curso.entity.Curso;
import com.umg.sgau.curso.exception.CarreraCursoInvalidaException;
import com.umg.sgau.curso.exception.CodigoCursoDuplicadoException;
import com.umg.sgau.curso.exception.CursoAcademicoDuplicadoException;
import com.umg.sgau.curso.exception.CursoNoEncontradoException;
import com.umg.sgau.curso.exception.DocenteCursoInvalidoException;
import com.umg.sgau.curso.repository.CursoRepository;
import com.umg.sgau.curso.service.CursoService;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CursoServiceImpl implements CursoService {

    private final CursoRepository cursoRepository;

    public CursoServiceImpl(CursoRepository cursoRepository) {
        this.cursoRepository = cursoRepository;
    }

    @Override
    public Curso crear(Curso curso) {
        String codigoNormalizado = normalizarCodigo(curso.getCodigo());
        String nombreNormalizado = normalizarNombre(curso.getNombre());
        String descripcionNormalizada = normalizarDescripcion(curso.getDescripcion());

        validarCarreraId(curso.getCarreraId());
        validarDocenteIdOpcional(curso.getDocenteId());
        validarCodigoDisponible(codigoNormalizado);
        validarCursoAcademicoDisponible(
                nombreNormalizado,
                curso.getCarreraId(),
                curso.getCicloAnio());

        curso.setCodigo(codigoNormalizado);
        curso.setNombre(nombreNormalizado);
        curso.setDescripcion(descripcionNormalizada);

        if (curso.getActivo() == null) {
            curso.setActivo(true);
        }

        return cursoRepository.save(curso);
    }

    @Override
    public Curso obtenerPorId(Long id) {
        return cursoRepository.findById(id)
                .orElseThrow(() -> new CursoNoEncontradoException(id));
    }

    @Override
    public Page<Curso> listar(
            String texto,
            Long carreraId,
            Long docenteId,
            Integer cicloAnio,
            Boolean activo,
            Pageable pageable) {
        String textoNormalizado = texto == null ? null : texto.trim();

        return cursoRepository.buscarConFiltros(
                textoNormalizado,
                carreraId,
                docenteId,
                cicloAnio,
                activo,
                pageable);
    }

    @Override
    public Curso actualizar(Long id, Curso curso) {
        Curso existente = obtenerPorId(id);
        String codigoNormalizado = normalizarCodigo(curso.getCodigo());
        String nombreNormalizado = normalizarNombre(curso.getNombre());
        String descripcionNormalizada = normalizarDescripcion(curso.getDescripcion());

        validarCarreraId(curso.getCarreraId());
        validarDocenteIdOpcional(curso.getDocenteId());
        validarCodigoDisponibleParaActualizar(codigoNormalizado, id);
        validarCursoAcademicoDisponibleParaActualizar(
                nombreNormalizado,
                curso.getCarreraId(),
                curso.getCicloAnio(),
                id);

        existente.setCodigo(codigoNormalizado);
        existente.setNombre(nombreNormalizado);
        existente.setDescripcion(descripcionNormalizada);
        existente.setCreditos(curso.getCreditos());
        existente.setHorasSemanales(curso.getHorasSemanales());
        existente.setCarreraId(curso.getCarreraId());
        existente.setDocenteId(curso.getDocenteId());
        existente.setCicloAnio(curso.getCicloAnio());

        return cursoRepository.save(existente);
    }

    @Override
    public Curso cambiarEstado(Long id, Boolean activo) {
        Curso curso = obtenerPorId(id);
        curso.setActivo(activo);
        return cursoRepository.save(curso);
    }

    @Override
    public Curso asignarDocente(Long id, Long docenteId) {
        Curso curso = obtenerPorId(id);

        validarDocenteIdRequerido(docenteId);

        curso.setDocenteId(docenteId);

        return cursoRepository.save(curso);
    }

    @Override
    public List<Curso> obtenerCursosActivos() {
        return cursoRepository.findAll()
                .stream()
                .filter(curso -> Boolean.TRUE.equals(curso.getActivo()))
                .collect(Collectors.toList());
    }

    @Override
    public List<String> obtenerNombresDeCursosActivos() {
        return cursoRepository.findAll()
                .stream()
                .filter(curso -> Boolean.TRUE.equals(curso.getActivo()))
                .map(Curso::getNombre)
                .collect(Collectors.toList());
    }

    @Override
    public List<Curso> obtenerCursosPorDocente(Long docenteId) {
        validarDocenteIdRequerido(docenteId);

        return cursoRepository.findAll()
                .stream()
                .filter(curso -> Boolean.TRUE.equals(curso.getActivo()))
                .filter(curso -> docenteId.equals(curso.getDocenteId()))
                .collect(Collectors.toList());
    }

    private String normalizarCodigo(String codigo) {
        return codigo.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizarNombre(String nombre) {
        return nombre.trim();
    }

    private String normalizarDescripcion(String descripcion) {
        return descripcion == null ? null : descripcion.trim();
    }

    private void validarCarreraId(Long carreraId) {
        if (carreraId == null || carreraId <= 0) {
            throw new CarreraCursoInvalidaException(carreraId);
        }
    }

    private void validarDocenteIdOpcional(Long docenteId) {
        if (docenteId != null && docenteId <= 0) {
            throw new DocenteCursoInvalidoException(docenteId);
        }
    }

    private void validarDocenteIdRequerido(Long docenteId) {
        if (docenteId == null || docenteId <= 0) {
            throw new DocenteCursoInvalidoException(docenteId);
        }
    }

    private void validarCodigoDisponible(String codigo) {
        if (cursoRepository.existsByCodigo(codigo)) {
            throw new CodigoCursoDuplicadoException(codigo);
        }
    }

    private void validarCodigoDisponibleParaActualizar(String codigo, Long id) {
        if (cursoRepository.existsByCodigoAndIdNot(codigo, id)) {
            throw new CodigoCursoDuplicadoException(codigo);
        }
    }

    private void validarCursoAcademicoDisponible(
            String nombre,
            Long carreraId,
            Integer cicloAnio) {
        if (cursoRepository.existsByNombreIgnoreCaseAndCarreraIdAndCicloAnioAndActivoTrue(
                nombre,
                carreraId,
                cicloAnio)) {
            throw new CursoAcademicoDuplicadoException(nombre, carreraId, cicloAnio);
        }
    }

    private void validarCursoAcademicoDisponibleParaActualizar(
            String nombre,
            Long carreraId,
            Integer cicloAnio,
            Long id) {
        if (cursoRepository.existsByNombreIgnoreCaseAndCarreraIdAndCicloAnioAndActivoTrueAndIdNot(
                nombre,
                carreraId,
                cicloAnio,
                id)) {
            throw new CursoAcademicoDuplicadoException(nombre, carreraId, cicloAnio);
        }
    }
}
