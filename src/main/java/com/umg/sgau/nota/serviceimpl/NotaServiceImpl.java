package com.umg.sgau.nota.serviceimpl;

import com.umg.sgau.curso.entity.Curso;
import com.umg.sgau.curso.exception.CursoNoEncontradoException;
import com.umg.sgau.curso.service.CursoService;
import com.umg.sgau.estudiante.entity.Estudiante;
import com.umg.sgau.estudiante.exception.EstudianteNoEncontradoException;
import com.umg.sgau.estudiante.service.EstudianteService;
import com.umg.sgau.inscripcion.service.InscripcionService;
import com.umg.sgau.nota.entity.Nota;
import com.umg.sgau.nota.exception.CursoInactivoParaNotaException;
import com.umg.sgau.nota.exception.CursoInvalidoParaNotaException;
import com.umg.sgau.nota.exception.EstudianteInactivoParaNotaException;
import com.umg.sgau.nota.exception.EstudianteInvalidoParaNotaException;
import com.umg.sgau.nota.exception.InscripcionActivaNoEncontradaException;
import com.umg.sgau.nota.exception.NotaDuplicadaException;
import com.umg.sgau.nota.exception.NotaInvalidaException;
import com.umg.sgau.nota.exception.NotaNoEncontradaException;
import com.umg.sgau.nota.repository.NotaRepository;
import com.umg.sgau.nota.service.NotaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class NotaServiceImpl implements NotaService {

    private final NotaRepository notaRepository;
    private final EstudianteService estudianteService;
    private final CursoService cursoService;
    private final InscripcionService inscripcionService;

    public NotaServiceImpl(
            NotaRepository notaRepository,
            EstudianteService estudianteService,
            CursoService cursoService,
            InscripcionService inscripcionService) {
        this.notaRepository = notaRepository;
        this.estudianteService = estudianteService;
        this.cursoService = cursoService;
        this.inscripcionService = inscripcionService;
    }

    @Override
    public Nota crear(Nota nota, Long estudianteId, Long cursoId) {
        validarDatosEditables(nota);
        nota.setTipoEvaluacion(normalizarTipoEvaluacion(nota.getTipoEvaluacion()));
        asignarReferencias(nota, estudianteId, cursoId);
        validarReferenciasActivas(nota);
        validarInscripcionActiva(nota);
        validarDuplicadoActivo(nota, null);
        nota.setActivo(true);

        return notaRepository.save(nota);
    }

    @Override
    public Nota obtenerPorId(Long id) {
        return notaRepository.findById(id)
                .orElseThrow(() -> new NotaNoEncontradaException(id));
    }

    @Override
    public Page<Nota> listar(
            Long estudianteId, Long cursoId, Integer cicloAnio,
            String tipoEvaluacion, Boolean activo, Pageable pageable) {
        String tipoNormalizado = tipoEvaluacion == null
                ? null
                : normalizarTipoEvaluacion(tipoEvaluacion);

        return notaRepository.buscarConFiltros(
                estudianteId, cursoId, cicloAnio, tipoNormalizado, activo, pageable);
    }

    @Override
    public Nota actualizar(Long id, Nota nota) {

        Nota existente = obtenerPorId(id);
        validarDatosEditables(nota);

        existente.setTipoEvaluacion(normalizarTipoEvaluacion(nota.getTipoEvaluacion()));
        existente.setCalificacion(nota.getCalificacion());
        existente.setObservaciones(nota.getObservaciones());

        validarDuplicadoActivo(existente, id);

        return notaRepository.save(existente);
    }

    @Override
    public Nota cambiarEstado(Long id, Boolean activo) {
        Nota nota = obtenerPorId(id);

        if (Boolean.TRUE.equals(activo)) {
            validarReferenciasActivas(nota);
            validarInscripcionActiva(nota);
            validarDuplicadoActivo(nota, id);
        }

        nota.setActivo(activo);
        return notaRepository.save(nota);
    }

    @Override
    public List<Nota> obtenerNotasActivasPorEstudiante(Long estudianteId) {
        validarEstudianteExistente(estudianteId);
        return notaRepository.findByEstudianteIdAndActivoTrue(estudianteId)
                .stream()
                .filter(nota -> Boolean.TRUE.equals(nota.getActivo()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Nota> obtenerNotasActivasPorEstudianteYCurso(Long estudianteId, Long cursoId) {
        validarEstudianteExistente(estudianteId);
        validarCursoExistente(cursoId);
        return notaRepository.findByEstudianteIdAndCursoIdAndActivoTrue(estudianteId, cursoId)
                .stream()
                .filter(nota -> Boolean.TRUE.equals(nota.getActivo()))
                .collect(Collectors.toList());
    }

    @Override
    public Page<Nota> obtenerNotasPorEstudiante(Long estudianteId, Pageable pageable) {
        validarEstudianteExistente(estudianteId);
        return notaRepository.findByEstudianteId(estudianteId, pageable);
    }

    @Override
    public Page<Nota> obtenerNotasActivasPorEstudiante(Long estudianteId, Pageable pageable) {
        validarEstudianteExistente(estudianteId);
        return notaRepository.findByEstudianteIdAndActivoTrue(estudianteId, pageable);
    }

    @Override
    public Page<Nota> obtenerNotasPorCurso(Long cursoId, Pageable pageable) {
        validarCursoExistente(cursoId);
        return notaRepository.findByCursoId(cursoId, pageable);
    }

    @Override
    public Page<Nota> obtenerNotasPorEstudianteYCurso(Long estudianteId, Long cursoId, Pageable pageable) {
        validarEstudianteExistente(estudianteId);
        validarCursoExistente(cursoId);
        return notaRepository.findByEstudianteIdAndCursoId(estudianteId, cursoId, pageable);
    }

    @Override
    public List<BigDecimal> obtenerCalificacionesActivas(Long estudianteId) {
        return obtenerNotasActivasPorEstudiante(estudianteId)
                .stream()
                .map(Nota::getCalificacion)
                .collect(Collectors.toList());
    }

    @Override
    public BigDecimal calcularPromedioGeneral(Long estudianteId) {
        double promedio = obtenerCalificacionesActivas(estudianteId)
                .stream()
                .map(BigDecimal::doubleValue)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.00);

        return BigDecimal.valueOf(promedio).setScale(2, RoundingMode.HALF_UP);
    }

    private void validarDatosEditables(Nota nota) {
        if (nota.getCalificacion() == null
                || nota.getCalificacion().compareTo(BigDecimal.ZERO) < 0
                || nota.getCalificacion().compareTo(new BigDecimal("100")) > 0) {
            throw new NotaInvalidaException("La calificacion debe estar entre 0 y 100.");
        }

        if (nota.getTipoEvaluacion() == null || nota.getTipoEvaluacion().isBlank()) {
            throw new NotaInvalidaException("El tipo de evaluacion es obligatorio.");
        }
    }

    private String normalizarTipoEvaluacion(String tipoEvaluacion) {
        return tipoEvaluacion.trim().toUpperCase(Locale.ROOT);
    }

    private void asignarReferencias(Nota nota, Long estudianteId, Long cursoId) {
        Estudiante estudiante = validarEstudianteExistente(estudianteId);
        if (!Boolean.TRUE.equals(estudiante.getActivo())) {
            throw new EstudianteInactivoParaNotaException(estudianteId);
        }
        nota.setEstudiante(estudiante);
        nota.setCurso(validarCursoExistente(cursoId));
    }

    private void validarReferenciasActivas(Nota nota) {
        Long estudianteId = getEstudianteId(nota);
        Estudiante estudiante = validarEstudianteExistente(estudianteId);
        if (!Boolean.TRUE.equals(estudiante.getActivo())) {
            throw new EstudianteInactivoParaNotaException(estudianteId);
        }
        nota.setEstudiante(estudiante);

        Long cursoId = getCursoId(nota);
        Curso curso = validarCursoExistente(cursoId);
        if (!Boolean.TRUE.equals(curso.getActivo())) {
            throw new CursoInactivoParaNotaException(cursoId);
        }
        nota.setCurso(curso);
    }

    private Estudiante validarEstudianteExistente(Long estudianteId) {
        if (estudianteId == null || estudianteId <= 0) {
            throw new EstudianteInvalidoParaNotaException(estudianteId);
        }

        try {
            return estudianteService.obtenerPorId(estudianteId);
        } catch (EstudianteNoEncontradoException exception) {
            throw new EstudianteInvalidoParaNotaException(estudianteId);
        }
    }

    private Curso validarCursoExistente(Long cursoId) {
        if (cursoId == null || cursoId <= 0) {
            throw new CursoInvalidoParaNotaException(cursoId);
        }

        try {
            return cursoService.obtenerPorId(cursoId);
        } catch (CursoNoEncontradoException exception) {
            throw new CursoInvalidoParaNotaException(cursoId);
        }
    }

    private void validarInscripcionActiva(Nota nota) {
        if (!inscripcionService.existeInscripcionActiva(
                getEstudianteId(nota),
                getCursoId(nota),
                nota.getCicloAnio())) {
            throw new InscripcionActivaNoEncontradaException(
                    getEstudianteId(nota),
                    getCursoId(nota),
                    nota.getCicloAnio());
        }
    }

    private void validarDuplicadoActivo(Nota nota, Long idExcluir) {
        boolean duplicada = idExcluir == null
                ? notaRepository.existsByEstudianteIdAndCursoIdAndCicloAnioAndTipoEvaluacionAndActivoTrue(
                getEstudianteId(nota), getCursoId(nota), nota.getCicloAnio(), nota.getTipoEvaluacion())
                : notaRepository.existsByEstudianteIdAndCursoIdAndCicloAnioAndTipoEvaluacionAndActivoTrueAndIdNot(
                getEstudianteId(nota), getCursoId(nota), nota.getCicloAnio(), nota.getTipoEvaluacion(), idExcluir);

        if (duplicada) {
            throw new NotaDuplicadaException(
                    "Ya existe una nota activa para el mismo estudiante, curso, ciclo y tipo de evaluacion.");
        }
    }

    private Long getEstudianteId(Nota nota) {
        return nota.getEstudiante() == null ? null : nota.getEstudiante().getId();
    }

    private Long getCursoId(Nota nota) {
        return nota.getCurso() == null ? null : nota.getCurso().getId();
    }
}
