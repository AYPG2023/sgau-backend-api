package com.umg.sgau.nota.serviceimpl;

import com.umg.sgau.nota.entity.Nota;
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

    public NotaServiceImpl(NotaRepository notaRepository) {
        this.notaRepository = notaRepository;
    }

    @Override
    public Nota crear(Nota nota) {

        // 1. Validar identificadores positivos
        if (nota.getEstudianteId() == null || nota.getEstudianteId() <= 0) {
            throw new NotaInvalidaException("El identificador del estudiante debe ser positivo.");
        }
        if (nota.getCursoId() == null || nota.getCursoId() <= 0) {
            throw new NotaInvalidaException("El identificador del curso debe ser positivo.");
        }

        // 2. Validar calificacion entre 0 y 100
        if (nota.getCalificacion() == null
                || nota.getCalificacion().compareTo(BigDecimal.ZERO) < 0
                || nota.getCalificacion().compareTo(new BigDecimal("100")) > 0) {
            throw new NotaInvalidaException("La calificacion debe estar entre 0 y 100.");
        }

        // 3. Normalizar tipoEvaluacion en mayusculas
        if (nota.getTipoEvaluacion() == null || nota.getTipoEvaluacion().isBlank()) {
            throw new NotaInvalidaException("El tipo de evaluacion es obligatorio.");
        }
        nota.setTipoEvaluacion(nota.getTipoEvaluacion().trim().toUpperCase(Locale.ROOT));

        // 4. Evitar notas activas duplicadas
        if (notaRepository.existsByEstudianteIdAndCursoIdAndCicloAnioAndTipoEvaluacionAndActivoTrue(
                nota.getEstudianteId(), nota.getCursoId(), nota.getCicloAnio(),
                nota.getTipoEvaluacion())) {
            throw new NotaDuplicadaException(
                    "Ya existe una nota activa para el mismo estudiante, curso, ciclo y tipo de evaluacion.");
        }

        // 5. Asignar activo true (el @PrePersist tambien lo hace como respaldo)
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
        return notaRepository.buscarConFiltros(
                estudianteId, cursoId, cicloAnio, tipoEvaluacion, activo, pageable);
    }

    @Override
    public Nota actualizar(Long id, Nota nota) {

        // 1. Verificar que la nota exista
        Nota existente = obtenerPorId(id);

        // 2. Validar calificacion entre 0 y 100
        if (nota.getCalificacion() == null
                || nota.getCalificacion().compareTo(BigDecimal.ZERO) < 0
                || nota.getCalificacion().compareTo(new BigDecimal("100")) > 0) {
            throw new NotaInvalidaException("La calificacion debe estar entre 0 y 100.");
        }

        // 3. Normalizar tipoEvaluacion
        if (nota.getTipoEvaluacion() == null || nota.getTipoEvaluacion().isBlank()) {
            throw new NotaInvalidaException("El tipo de evaluacion es obligatorio.");
        }
        String tipoNormalizado = nota.getTipoEvaluacion().trim().toUpperCase(Locale.ROOT);

        // 4. Validar duplicados excluyendo el mismo ID
        if (notaRepository.existsByEstudianteIdAndCursoIdAndCicloAnioAndTipoEvaluacionAndActivoTrueAndIdNot(
                existente.getEstudianteId(), existente.getCursoId(), existente.getCicloAnio(),
                tipoNormalizado, id)) {
            throw new NotaDuplicadaException(
                    "Ya existe una nota activa para el mismo estudiante, curso, ciclo y tipo de evaluacion.");
        }

        // 5. Actualizar solo campos permitidos (sin tocar estudiante, curso, ciclo, activo ni auditoria)
        existente.setTipoEvaluacion(tipoNormalizado);
        existente.setCalificacion(nota.getCalificacion());
        existente.setObservaciones(nota.getObservaciones());

        return notaRepository.save(existente);
    }

    @Override
    public Nota cambiarEstado(Long id, Boolean activo) {
        Nota nota = obtenerPorId(id);
        nota.setActivo(activo);
        return notaRepository.save(nota);
    }

    @Override
    public List<Nota> obtenerNotasActivasPorEstudiante(Long estudianteId, Integer cicloAnio) {
        return notaRepository.findAll()
                .stream()
                .filter(nota -> Boolean.TRUE.equals(nota.getActivo()))
                .filter(nota -> nota.getEstudianteId().equals(estudianteId))
                .filter(nota -> nota.getCicloAnio().equals(cicloAnio))
                .collect(Collectors.toList());
    }

    @Override
    public List<BigDecimal> obtenerCalificacionesActivas(Long estudianteId, Integer cicloAnio) {
        return obtenerNotasActivasPorEstudiante(estudianteId, cicloAnio)
                .stream()
                .map(Nota::getCalificacion)
                .collect(Collectors.toList());
    }

    @Override
    public BigDecimal calcularPromedioGeneral(Long estudianteId, Integer cicloAnio) {
        List<BigDecimal> calificaciones = obtenerCalificacionesActivas(estudianteId, cicloAnio);

        if (calificaciones.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal suma = calificaciones.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return suma.divide(
                BigDecimal.valueOf(calificaciones.size()),
                2,
                RoundingMode.HALF_UP);
    }
}
