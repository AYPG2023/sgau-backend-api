package com.umg.sgau.inscripcion.serviceimpl;

import com.umg.sgau.inscripcion.entity.Inscripcion;
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

    @Override
    public Inscripcion registrar(Inscripcion inscripcion) {

        // 1. Validar que no exista una inscripcion activa identica
        if (inscripcionRepository.existsByEstudianteIdAndCarreraIdAndGradoAndSeccionAndCicloAnioAndActivoTrue(
                inscripcion.getEstudianteId(), inscripcion.getCarreraId(), inscripcion.getGrado(),
                inscripcion.getSeccion(), inscripcion.getCicloAnio())) {
            throw new InscripcionDuplicadaException(
                    inscripcion.getEstudianteId(), inscripcion.getCarreraId(), inscripcion.getGrado(),
                    inscripcion.getSeccion(), inscripcion.getCicloAnio());
        }

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

        // 1. Buscar la inscripcion existente
        Inscripcion existente = obtenerPorId(id);

        // 2. Validar que los nuevos datos no generen un duplicado
        //    (excluyendo el ID actual para no compararse con sigo mismo)
        if (inscripcionRepository.existsByEstudianteIdAndCarreraIdAndGradoAndSeccionAndCicloAnioAndActivoTrueAndIdNot(
                existente.getEstudianteId(), inscripcion.getCarreraId(), inscripcion.getGrado(),
                inscripcion.getSeccion(), inscripcion.getCicloAnio(), id)) {
            throw new InscripcionDuplicadaException(
                    existente.getEstudianteId(), inscripcion.getCarreraId(), inscripcion.getGrado(),
                    inscripcion.getSeccion(), inscripcion.getCicloAnio());
        }

        existente.setCarreraId(inscripcion.getCarreraId());
        existente.setCursoId(inscripcion.getCursoId());
        existente.setGrado(inscripcion.getGrado());
        existente.setSeccion(inscripcion.getSeccion());
        existente.setCicloAnio(inscripcion.getCicloAnio());
        existente.setObservaciones(inscripcion.getObservaciones());

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
    @Transactional(readOnly = true)
    public Page<Inscripcion> historialPorEstudiante(Long estudianteId, Pageable pageable) {
        return inscripcionRepository.findByEstudianteId(estudianteId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Inscripcion> obtenerActivas() {
        return inscripcionRepository.findAll()
                .stream()
                .filter(inscripcion -> Boolean.TRUE.equals(inscripcion.getActivo()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> obtenerEstudiantesConInscripcionActiva() {
        return inscripcionRepository.findAll()
                .stream()
                .filter(inscripcion -> Boolean.TRUE.equals(inscripcion.getActivo()))
                .map(Inscripcion::getEstudianteId)
                .collect(Collectors.toList());
    }
}
