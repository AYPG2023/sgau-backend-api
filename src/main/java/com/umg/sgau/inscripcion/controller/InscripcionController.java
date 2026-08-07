package com.umg.sgau.inscripcion.controller;

import com.umg.sgau.inscripcion.dto.InscripcionCreateRequestDTO;
import com.umg.sgau.inscripcion.dto.InscripcionResponseDTO;
import com.umg.sgau.inscripcion.dto.InscripcionStatusRequestDTO;
import com.umg.sgau.inscripcion.dto.InscripcionUpdateRequestDTO;
import com.umg.sgau.inscripcion.entity.Inscripcion;
import com.umg.sgau.inscripcion.mapper.InscripcionMapper;
import com.umg.sgau.inscripcion.service.InscripcionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST del dominio Inscripcion.
 *
 * Endpoints:
 *   POST   /api/inscripciones                  -> registrar
 *   GET    /api/inscripciones                  -> listar con filtros
 *   GET    /api/inscripciones/{id}             -> obtener por id
 *   PUT    /api/inscripciones/{id}             -> actualizar
 *   PATCH  /api/inscripciones/{id}/estado      -> anular (soft-delete)
 *   PATCH  /api/inscripciones/{id}/reactivar   -> reactivar
 *   GET    /api/inscripciones/estudiante/{id}  -> historial por estudiante
 */
@RestController
@RequestMapping("/api/inscripciones")
@RequiredArgsConstructor
public class InscripcionController {

    private final InscripcionService inscripcionService;

    /**
     * Registra una nueva inscripcion.
     * HTTP 201 Created.
     */
    @PostMapping
    public ResponseEntity<InscripcionResponseDTO> registrar(
            @Valid @RequestBody InscripcionCreateRequestDTO request) {
        Inscripcion inscripcion = InscripcionMapper.aEntidad(request);
        Inscripcion registrada = inscripcionService.registrar(
                inscripcion,
                request.getEstudianteId(),
                request.getCarreraId(),
                request.getCursoId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(InscripcionMapper.aResponseDTO(registrada));
    }

    /**
     * Lista inscripciones con filtros opcionales y paginacion.
     * Todos los query params son opcionales: un param ausente (null)
     * significa "no filtrar por este campo".
     */
    @GetMapping
    public ResponseEntity<Page<InscripcionResponseDTO>> listar(
            @RequestParam(required = false) Long estudianteId,
            @RequestParam(required = false) Long carreraId,
            @RequestParam(required = false) Long cursoId,
            @RequestParam(required = false) Integer cicloAnio,
            @RequestParam(required = false) String grado,
            @RequestParam(required = false) String seccion,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Boolean activo,
            Pageable pageable) {
        Page<Inscripcion> inscripciones = inscripcionService.listarConFiltros(
                estudianteId, carreraId, cursoId, cicloAnio,
                grado, seccion, estado, activo, pageable);
        return ResponseEntity.ok(inscripciones.map(InscripcionMapper::aResponseDTO));
    }

    /**
     * Obtiene una inscripcion por su ID.
     * HTTP 200 OK o 404 si no existe.
     */
    @GetMapping("/{id}")
    public ResponseEntity<InscripcionResponseDTO> obtenerPorId(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                InscripcionMapper.aResponseDTO(inscripcionService.obtenerPorId(id)));
    }

    /**
     * Actualiza los campos editables de una inscripcion existente.
     * No modifica: id, estudianteId, estado, activo, fechaInscripcion, auditoria.
     * HTTP 200 OK, 404 si no existe, 409 si genera duplicado.
     */
    @PutMapping("/{id}")
    public ResponseEntity<InscripcionResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody InscripcionUpdateRequestDTO request) {
        Inscripcion datos = new Inscripcion();
        InscripcionMapper.actualizarEntidad(request, datos);
        return ResponseEntity.ok(
                InscripcionMapper.aResponseDTO(
                        inscripcionService.actualizar(id, datos, request.getCarreraId(), request.getCursoId())));
    }

    /**
     * Anula (soft-delete) una inscripcion: estado=ANULADA, activo=false.
     * HTTP 200 OK, 404 si no existe, 409 si ya esta anulada.
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<InscripcionResponseDTO> anular(
            @PathVariable Long id,
            @Valid @RequestBody InscripcionStatusRequestDTO request) {
        return ResponseEntity.ok(
                InscripcionMapper.aResponseDTO(inscripcionService.anular(id, request.getMotivo())));
    }

    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<InscripcionResponseDTO> reactivar(@PathVariable Long id) {
        return ResponseEntity.ok(
                InscripcionMapper.aResponseDTO(inscripcionService.reactivar(id)));
    }

    /**
     * Historial de inscripciones de un estudiante,
     * ordenado por fecha de inscripcion descendente.
     */
    @GetMapping("/estudiante/{estudianteId}")
    public ResponseEntity<Page<InscripcionResponseDTO>> historialPorEstudiante(
            @PathVariable Long estudianteId,
            Pageable pageable) {
        return ResponseEntity.ok(
                inscripcionService.historialPorEstudiante(estudianteId, pageable)
                        .map(InscripcionMapper::aResponseDTO));
    }

    @GetMapping("/curso/{cursoId}")
    public ResponseEntity<Page<InscripcionResponseDTO>> inscripcionesPorCurso(
            @PathVariable Long cursoId,
            Pageable pageable) {
        return ResponseEntity.ok(
                inscripcionService.inscripcionesPorCurso(cursoId, pageable)
                        .map(InscripcionMapper::aResponseDTO));
    }

    @GetMapping("/estudiante/{estudianteId}/activas")
    public ResponseEntity<Page<InscripcionResponseDTO>> inscripcionesActivasPorEstudiante(
            @PathVariable Long estudianteId,
            Pageable pageable) {
        return ResponseEntity.ok(
                inscripcionService.inscripcionesActivasPorEstudiante(estudianteId, pageable)
                        .map(InscripcionMapper::aResponseDTO));
    }

    @GetMapping("/curso/{cursoId}/activas")
    public ResponseEntity<Page<InscripcionResponseDTO>> inscripcionesActivasPorCurso(
            @PathVariable Long cursoId,
            Pageable pageable) {
        return ResponseEntity.ok(
                inscripcionService.inscripcionesActivasPorCurso(cursoId, pageable)
                        .map(InscripcionMapper::aResponseDTO));
    }

    @GetMapping("/activas")
    public ResponseEntity<List<InscripcionResponseDTO>> obtenerActivas() {
        return ResponseEntity.ok(
                InscripcionMapper.aResponseDTOList(inscripcionService.obtenerActivas()));
    }

    @GetMapping("/activas/estudiantes")
    public ResponseEntity<List<Long>> obtenerEstudiantesConInscripcionActiva() {
        return ResponseEntity.ok(inscripcionService.obtenerEstudiantesConInscripcionActiva());
    }
}
