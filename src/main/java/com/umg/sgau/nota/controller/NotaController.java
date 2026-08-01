package com.umg.sgau.nota.controller;

import com.umg.sgau.nota.dto.NotaCreateRequestDTO;
import com.umg.sgau.nota.dto.NotaResponseDTO;
import com.umg.sgau.nota.dto.NotaStatusRequestDTO;
import com.umg.sgau.nota.dto.NotaUpdateRequestDTO;
import com.umg.sgau.nota.dto.PromedioResponseDTO;
import com.umg.sgau.nota.entity.Nota;
import com.umg.sgau.nota.mapper.NotaMapper;
import com.umg.sgau.nota.service.NotaService;
import jakarta.validation.Valid;
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

import java.math.BigDecimal;
import java.util.List;

/**
 * Controlador REST del dominio Nota.
 *
 * Endpoints:
 *   POST   /api/notas                              -> registrar nota
 *   GET    /api/notas                              -> listar con filtros y paginacion
 *   GET    /api/notas/{id}                         -> obtener por ID
 *   PUT    /api/notas/{id}                         -> actualizar
 *   PATCH  /api/notas/{id}/estado                   -> habilitar o inhabilitar
 *   GET    /api/notas/estudiante/{id}               -> notas del estudiante
 *   GET    /api/notas/estudiante/{id}/promedio       -> promedio general
 *   GET    /api/notas/estudiante/{id}/calificaciones -> calificaciones (map + collect)
 */
@RestController
@RequestMapping("/api/notas")
public class NotaController {

    private final NotaService notaService;

    public NotaController(NotaService notaService) {
        this.notaService = notaService;
    }

    @PostMapping
    public ResponseEntity<NotaResponseDTO> crear(
            @Valid @RequestBody NotaCreateRequestDTO request) {
        Nota nota = NotaMapper.aEntidad(request);
        Nota creada = notaService.crear(nota);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(NotaMapper.aResponseDTO(creada));
    }

    @GetMapping
    public ResponseEntity<Page<NotaResponseDTO>> listar(
            @RequestParam(required = false) Long estudianteId,
            @RequestParam(required = false) Long cursoId,
            @RequestParam(required = false) Integer cicloAnio,
            @RequestParam(required = false) String tipoEvaluacion,
            @RequestParam(required = false) Boolean activo,
            Pageable pageable) {
        Page<Nota> pagina = notaService.listar(
                estudianteId, cursoId, cicloAnio,
                tipoEvaluacion, activo, pageable);
        return ResponseEntity.ok(pagina.map(NotaMapper::aResponseDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotaResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(
                NotaMapper.aResponseDTO(notaService.obtenerPorId(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NotaResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody NotaUpdateRequestDTO request) {
        Nota datos = new Nota();
        NotaMapper.actualizarEntidad(request, datos);
        Nota actualizada = notaService.actualizar(id, datos);
        return ResponseEntity.ok(NotaMapper.aResponseDTO(actualizada));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<NotaResponseDTO> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody NotaStatusRequestDTO request) {
        Nota nota = notaService.cambiarEstado(id, request.getActivo());
        return ResponseEntity.ok(NotaMapper.aResponseDTO(nota));
    }

    @GetMapping("/estudiante/{estudianteId}")
    public ResponseEntity<List<NotaResponseDTO>> notasDelEstudiante(
            @PathVariable Long estudianteId,
            @RequestParam Integer cicloAnio) {
        List<Nota> notas = notaService.obtenerNotasActivasPorEstudiante(
                estudianteId, cicloAnio);
        return ResponseEntity.ok(NotaMapper.aResponseDTOList(notas));
    }

    @GetMapping("/estudiante/{estudianteId}/promedio")
    public ResponseEntity<PromedioResponseDTO> promedio(
            @PathVariable Long estudianteId,
            @RequestParam Integer cicloAnio) {
        BigDecimal promedio = notaService.calcularPromedioGeneral(
                estudianteId, cicloAnio);
        List<Nota> notas = notaService.obtenerNotasActivasPorEstudiante(
                estudianteId, cicloAnio);
        PromedioResponseDTO response = PromedioResponseDTO.builder()
                .estudianteId(estudianteId)
                .cicloAnio(cicloAnio)
                .promedioGeneral(promedio)
                .cantidadNotas(notas.size())
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/estudiante/{estudianteId}/calificaciones")
    public ResponseEntity<List<BigDecimal>> calificaciones(
            @PathVariable Long estudianteId,
            @RequestParam Integer cicloAnio) {
        return ResponseEntity.ok(
                notaService.obtenerCalificacionesActivas(estudianteId, cicloAnio));
    }
}
