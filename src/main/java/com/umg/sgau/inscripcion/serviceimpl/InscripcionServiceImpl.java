package com.umg.sgau.inscripcion.serviceimpl;

import com.umg.sgau.inscripcion.dto.InscripcionCreateRequestDTO;
import com.umg.sgau.inscripcion.dto.InscripcionResponseDTO;
import com.umg.sgau.inscripcion.dto.InscripcionStatusRequestDTO;
import com.umg.sgau.inscripcion.dto.InscripcionUpdateRequestDTO;
import com.umg.sgau.inscripcion.entity.Inscripcion;
import com.umg.sgau.inscripcion.exception.InscripcionDuplicadaException;
import com.umg.sgau.inscripcion.exception.InscripcionNoEncontradaException;
import com.umg.sgau.inscripcion.mapper.InscripcionMapper;
import com.umg.sgau.inscripcion.repository.InscripcionRepository;
import com.umg.sgau.inscripcion.service.InscripcionService;
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
    public InscripcionResponseDTO registrar(InscripcionCreateRequestDTO dto) {

        // 1. Validar que no exista una inscripcion activa identica
        if (inscripcionRepository.existsByEstudianteIdAndCarreraIdAndGradoAndSeccionAndCicloAnioAndActivoTrue(
                dto.getEstudianteId(), dto.getCarreraId(), dto.getGrado(),
                dto.getSeccion(), dto.getCicloAnio())) {
            throw new InscripcionDuplicadaException(
                    dto.getEstudianteId(), dto.getCarreraId(), dto.getGrado(),
                    dto.getSeccion(), dto.getCicloAnio());
        }

        // 2. Convertir DTO a entidad
        Inscripcion inscripcion = InscripcionMapper.aEntidad(dto);

        // 3. Guardar (el @PrePersist pone estado=ACTIVA y activo=true)
        Inscripcion guardada = inscripcionRepository.save(inscripcion);

        // 4. Convertir a DTO de respuesta
        return InscripcionMapper.aResponseDTO(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public InscripcionResponseDTO obtenerPorId(Long id) {
        Inscripcion inscripcion = inscripcionRepository.findById(id)
                .orElseThrow(() -> new InscripcionNoEncontradaException(id));
        return InscripcionMapper.aResponseDTO(inscripcion);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InscripcionResponseDTO> listarConFiltros(
            Long estudianteId, Long carreraId, Long cursoId,
            Integer cicloAnio, String grado, String seccion,
            String estado, Boolean activo, Pageable pageable) {

        return inscripcionRepository.buscarConFiltros(
                        estudianteId, carreraId, cursoId, cicloAnio,
                        grado, seccion, estado, activo, pageable)
                .map(InscripcionMapper::aResponseDTO);
    }

    @Override
    public InscripcionResponseDTO actualizar(Long id, InscripcionUpdateRequestDTO dto) {

        // 1. Buscar la inscripcion existente
        Inscripcion existente = inscripcionRepository.findById(id)
                .orElseThrow(() -> new InscripcionNoEncontradaException(id));

        // 2. Validar que los nuevos datos no generen un duplicado
        //    (excluyendo el ID actual para no compararse con sigo mismo)
        if (inscripcionRepository.existsByEstudianteIdAndCarreraIdAndGradoAndSeccionAndCicloAnioAndActivoTrueAndIdNot(
                existente.getEstudianteId(), dto.getCarreraId(), dto.getGrado(),
                dto.getSeccion(), dto.getCicloAnio(), id)) {
            throw new InscripcionDuplicadaException(
                    existente.getEstudianteId(), dto.getCarreraId(), dto.getGrado(),
                    dto.getSeccion(), dto.getCicloAnio());
        }

        // 3. Modificar solo los campos editables (el mapper respeta la regla:
        //    nunca toca id, estudianteId, estado, activo, fechaInscripcion, auditoria)
        InscripcionMapper.actualizarEntidad(dto, existente);

        // 4. Guardar
        Inscripcion actualizada = inscripcionRepository.save(existente);
        return InscripcionMapper.aResponseDTO(actualizada);
    }

    @Override
    public InscripcionResponseDTO anular(Long id, InscripcionStatusRequestDTO dto) {

        Inscripcion inscripcion = inscripcionRepository.findById(id)
                .orElseThrow(() -> new InscripcionNoEncontradaException(id));

        // Regla de negocio: no se puede anular una inscripcion que ya esta anulada
        if ("ANULADA".equals(inscripcion.getEstado())) {
            throw new IllegalStateException(
                    "La inscripcion con ID " + id + " ya esta anulada.");
        }

        // Soft-delete: cambiar estado a ANULADA, activo a false
        inscripcion.setEstado("ANULADA");
        inscripcion.setActivo(false);
        if (dto.getMotivo() != null && !dto.getMotivo().isBlank()) {
            inscripcion.setObservaciones(dto.getMotivo());
        }

        return InscripcionMapper.aResponseDTO(inscripcionRepository.save(inscripcion));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InscripcionResponseDTO> historialPorEstudiante(Long estudianteId, Pageable pageable) {
        return inscripcionRepository.findByEstudianteId(estudianteId, pageable)
                .map(InscripcionMapper::aResponseDTO);
    }
}
