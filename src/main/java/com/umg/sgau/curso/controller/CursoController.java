package com.umg.sgau.curso.controller;

import com.umg.sgau.curso.dto.CursoAsignacionDocenteRequestDTO;
import com.umg.sgau.curso.dto.CursoCreateRequestDTO;
import com.umg.sgau.curso.dto.CursoResponseDTO;
import com.umg.sgau.curso.dto.CursoStatusRequestDTO;
import com.umg.sgau.curso.dto.CursoSummaryDTO;
import com.umg.sgau.curso.dto.CursoUpdateRequestDTO;
import com.umg.sgau.curso.entity.Curso;
import com.umg.sgau.curso.mapper.CursoMapper;
import com.umg.sgau.curso.service.CursoService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
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

@RestController
@RequestMapping("/api/cursos")
public class CursoController {

    private final CursoService cursoService;

    public CursoController(CursoService cursoService) {
        this.cursoService = cursoService;
    }

    @PostMapping
    public ResponseEntity<CursoResponseDTO> crear(
            @Valid @RequestBody CursoCreateRequestDTO request) {
        Curso curso = CursoMapper.aEntidad(request);
        Curso creado = cursoService.crear(curso);
        CursoResponseDTO response = CursoMapper.aResponseDTO(creado);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<Page<CursoResponseDTO>> listar(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) Long carreraId,
            @RequestParam(required = false) Long docenteId,
            @RequestParam(required = false) Integer cicloAnio,
            @RequestParam(required = false) Boolean activo,
            Pageable pageable) {
        Page<Curso> pagina = cursoService.listar(
                texto,
                carreraId,
                docenteId,
                cicloAnio,
                activo,
                pageable);

        Page<CursoResponseDTO> respuesta = pagina.map(CursoMapper::aResponseDTO);

        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/activos")
    public ResponseEntity<List<CursoSummaryDTO>> obtenerCursosActivos() {
        List<Curso> cursos = cursoService.obtenerCursosActivos();

        List<CursoSummaryDTO> respuesta = cursos.stream()
                .map(CursoMapper::aSummaryDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/nombres-activos")
    public ResponseEntity<List<String>> obtenerNombresDeCursosActivos() {
        return ResponseEntity.ok(cursoService.obtenerNombresDeCursosActivos());
    }

    @GetMapping("/docente/{docenteId}")
    public ResponseEntity<List<CursoSummaryDTO>> obtenerCursosPorDocente(
            @PathVariable Long docenteId) {
        List<Curso> cursos = cursoService.obtenerCursosPorDocente(docenteId);

        List<CursoSummaryDTO> respuesta = cursos.stream()
                .map(CursoMapper::aSummaryDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CursoResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(
                CursoMapper.aResponseDTO(
                        cursoService.obtenerPorId(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CursoResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody CursoUpdateRequestDTO request) {
        Curso datos = new Curso();

        CursoMapper.actualizarEntidad(request, datos);

        Curso actualizado = cursoService.actualizar(id, datos);

        return ResponseEntity.ok(CursoMapper.aResponseDTO(actualizado));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<CursoResponseDTO> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CursoStatusRequestDTO request) {
        Curso actualizado = cursoService.cambiarEstado(id, request.getActivo());

        return ResponseEntity.ok(CursoMapper.aResponseDTO(actualizado));
    }

    @PatchMapping("/{id}/docente")
    public ResponseEntity<CursoResponseDTO> asignarDocente(
            @PathVariable Long id,
            @Valid @RequestBody CursoAsignacionDocenteRequestDTO request) {
        Curso actualizado = cursoService.asignarDocente(id, request.getDocenteId());

        return ResponseEntity.ok(CursoMapper.aResponseDTO(actualizado));
    }
}
