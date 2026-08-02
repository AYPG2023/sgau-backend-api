package com.umg.sgau.inscripcion.serviceimpl;

import com.umg.sgau.curso.entity.Curso;
import com.umg.sgau.curso.exception.CursoNoEncontradoException;
import com.umg.sgau.curso.service.CursoService;
import com.umg.sgau.estudiante.entity.Estudiante;
import com.umg.sgau.estudiante.exception.EstudianteNoEncontradoException;
import com.umg.sgau.estudiante.service.EstudianteService;
import com.umg.sgau.inscripcion.entity.Inscripcion;
import com.umg.sgau.inscripcion.exception.CursoInactivoParaInscripcionException;
import com.umg.sgau.inscripcion.exception.CursoInvalidoParaInscripcionException;
import com.umg.sgau.inscripcion.exception.CursoNoPerteneceCarreraException;
import com.umg.sgau.inscripcion.exception.EstudianteInactivoParaInscripcionException;
import com.umg.sgau.inscripcion.exception.EstudianteInvalidoParaInscripcionException;
import com.umg.sgau.inscripcion.exception.InscripcionDuplicadaException;
import com.umg.sgau.inscripcion.exception.InscripcionNoEncontradaException;
import com.umg.sgau.inscripcion.repository.InscripcionRepository;
import com.umg.sgau.inscripcion.service.InscripcionService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class InscripcionServiceImpl implements InscripcionService {

    private final InscripcionRepository inscripcionRepository;
    private final EstudianteService estudianteService;
    private final CursoService cursoService;

    @Override
    public Inscripcion registrar(Inscripcion inscripcion) {
        validarReferenciasActivas(inscripcion);
        validarDuplicadoActivo(inscripcion, null);

        // El @PrePersist pone estado=ACTIVA y activo=true.
        return inscripcionRepository.save(inscripcion);
    }

    @Override
    @Transactional(readOnly = true)
    public Inscripcion obtenerPorId(Long id) {
        return inscripcionRepository.findById(id)
                .orElseThrow(() -> new InscripcionNoEncontradaException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Inscripcion> listarConFiltros(
            Long estudianteId, Long carreraId, Long cursoId,
            Integer cicloAnio, String grado, String seccion,
            String estado, Boolean activo, Pageable pageable) {

        return inscripcionRepository.buscarConFiltros(
                        estudianteId, carreraId, cursoId, cicloAnio,
                        grado, seccion, estado, activo, pageable);
    }

    @Override
    public Inscripcion actualizar(Long id, Inscripcion inscripcion) {

        Inscripcion existente = obtenerPorId(id);

        existente.setCarreraId(inscripcion.getCarreraId());
        if (inscripcion.getCursoId() != null) {
            existente.setCursoId(inscripcion.getCursoId());
        }
        existente.setGrado(inscripcion.getGrado());
        existente.setSeccion(inscripcion.getSeccion());
        existente.setCicloAnio(inscripcion.getCicloAnio());
        existente.setObservaciones(inscripcion.getObservaciones());

        validarReferenciasActivas(existente);
        validarDuplicadoActivo(existente, id);

        return inscripcionRepository.save(existente);
    }

    @Override
    public Inscripcion anular(Long id, String motivo) {

        Inscripcion inscripcion = obtenerPorId(id);

        // Regla de negocio: no se puede anular una inscripcion que ya esta anulada
        if ("ANULADA".equals(inscripcion.getEstado())) {
            throw new IllegalStateException(
                    "La inscripcion con ID " + id + " ya esta anulada.");
        }

        // Soft-delete: cambiar estado a ANULADA, activo a false
        inscripcion.setEstado("ANULADA");
        inscripcion.setActivo(false);
        if (motivo != null && !motivo.isBlank()) {
            inscripcion.setObservaciones(motivo);
        }

        return inscripcionRepository.save(inscripcion);
    }

    @Override
    public Inscripcion reactivar(Long id) {
        Inscripcion inscripcion = obtenerPorId(id);

        if (Boolean.TRUE.equals(inscripcion.getActivo())) {
            throw new IllegalStateException(
                    "La inscripcion con ID " + id + " ya esta activa.");
        }

        validarReferenciasActivas(inscripcion);
        validarDuplicadoActivo(inscripcion, id);

        inscripcion.setEstado("ACTIVA");
        inscripcion.setActivo(true);

        return inscripcionRepository.save(inscripcion);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Inscripcion> historialPorEstudiante(Long estudianteId, Pageable pageable) {
        validarEstudianteExistente(estudianteId);
        return inscripcionRepository.findByEstudianteId(estudianteId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Inscripcion> inscripcionesPorCurso(Long cursoId, Pageable pageable) {
        validarCursoExistente(cursoId);
        return inscripcionRepository.findByCursoId(cursoId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Inscripcion> inscripcionesActivasPorEstudiante(Long estudianteId, Pageable pageable) {
        validarEstudianteExistente(estudianteId);
        return inscripcionRepository.findByEstudianteIdAndActivoTrue(estudianteId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Inscripcion> inscripcionesActivasPorCurso(Long cursoId, Pageable pageable) {
        validarCursoExistente(cursoId);
        return inscripcionRepository.findByCursoIdAndActivoTrue(cursoId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Inscripcion> obtenerActivas() {
        return inscripcionRepository.findByActivoTrue()
                .stream()
                .filter(inscripcion -> Boolean.TRUE.equals(inscripcion.getActivo()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> obtenerEstudiantesConInscripcionActiva() {
        return obtenerActivas()
                .stream()
                .filter(inscripcion -> Boolean.TRUE.equals(inscripcion.getActivo()))
                .map(Inscripcion::getEstudianteId)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeInscripcionActiva(Long estudianteId, Long cursoId, Integer cicloAnio) {
        if (estudianteId == null || cursoId == null || cicloAnio == null) {
            return false;
        }

        return inscripcionRepository.existsByEstudianteIdAndCursoIdAndCicloAnioAndActivoTrue(
                estudianteId, cursoId, cicloAnio);
    }

    private void validarReferenciasActivas(Inscripcion inscripcion) {
        Estudiante estudiante = validarEstudianteExistente(inscripcion.getEstudianteId());
        if (!Boolean.TRUE.equals(estudiante.getActivo())) {
            throw new EstudianteInactivoParaInscripcionException(inscripcion.getEstudianteId());
        }

        Curso curso = null;
        if (inscripcion.getCursoId() != null) {
            curso = validarCursoExistente(inscripcion.getCursoId());
            if (!Boolean.TRUE.equals(curso.getActivo())) {
                throw new CursoInactivoParaInscripcionException(inscripcion.getCursoId());
            }
        }

        validarCursoPerteneceCarrera(inscripcion, curso);
    }

    private Estudiante validarEstudianteExistente(Long estudianteId) {
        if (estudianteId == null || estudianteId <= 0) {
            throw new EstudianteInvalidoParaInscripcionException(estudianteId);
        }

        try {
            return estudianteService.obtenerPorId(estudianteId);
        } catch (EstudianteNoEncontradoException exception) {
            throw new EstudianteInvalidoParaInscripcionException(estudianteId);
        }
    }

    private Curso validarCursoExistente(Long cursoId) {
        if (cursoId == null || cursoId <= 0) {
            throw new CursoInvalidoParaInscripcionException(cursoId);
        }

        try {
            return cursoService.obtenerPorId(cursoId);
        } catch (CursoNoEncontradoException exception) {
            throw new CursoInvalidoParaInscripcionException(cursoId);
        }
    }

    private void validarCursoPerteneceCarrera(Inscripcion inscripcion, Curso curso) {
        if (curso != null && !curso.getCarreraId().equals(inscripcion.getCarreraId())) {
            throw new CursoNoPerteneceCarreraException(inscripcion.getCursoId(), inscripcion.getCarreraId());
        }
    }

    private void validarDuplicadoActivo(Inscripcion inscripcion, Long idExcluir) {
        boolean duplicada = inscripcion.getCursoId() == null
                ? existeDuplicadoActivoSinCurso(inscripcion, idExcluir)
                : existeDuplicadoActivoConCurso(inscripcion, idExcluir);

        if (!duplicada) {
            return;
        }

        if (inscripcion.getCursoId() == null) {
            throw new InscripcionDuplicadaException(
                    inscripcion.getEstudianteId(), inscripcion.getCarreraId(), inscripcion.getGrado(),
                    inscripcion.getSeccion(), inscripcion.getCicloAnio());
        }

        throw new InscripcionDuplicadaException(
                inscripcion.getEstudianteId(), inscripcion.getCursoId(), inscripcion.getCicloAnio());
    }

    private boolean existeDuplicadoActivoConCurso(Inscripcion inscripcion, Long idExcluir) {
        if (idExcluir == null) {
            return inscripcionRepository.existsByEstudianteIdAndCursoIdAndCicloAnioAndActivoTrue(
                    inscripcion.getEstudianteId(), inscripcion.getCursoId(), inscripcion.getCicloAnio());
        }

        return inscripcionRepository.existsByEstudianteIdAndCursoIdAndCicloAnioAndActivoTrueAndIdNot(
                inscripcion.getEstudianteId(), inscripcion.getCursoId(), inscripcion.getCicloAnio(), idExcluir);
    }

    private boolean existeDuplicadoActivoSinCurso(Inscripcion inscripcion, Long idExcluir) {
        if (idExcluir == null) {
            return inscripcionRepository.existsByEstudianteIdAndCarreraIdAndGradoAndSeccionAndCicloAnioAndActivoTrue(
                    inscripcion.getEstudianteId(), inscripcion.getCarreraId(), inscripcion.getGrado(),
                    inscripcion.getSeccion(), inscripcion.getCicloAnio());
        }

        return inscripcionRepository.existsByEstudianteIdAndCarreraIdAndGradoAndSeccionAndCicloAnioAndActivoTrueAndIdNot(
                inscripcion.getEstudianteId(), inscripcion.getCarreraId(), inscripcion.getGrado(),
                inscripcion.getSeccion(), inscripcion.getCicloAnio(), idExcluir);
    }
}
