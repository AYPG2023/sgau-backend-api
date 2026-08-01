package com.umg.sgau.estudiante.controller;

import com.umg.sgau.estudiante.dto.EstudianteCreateRequestDTO;
import com.umg.sgau.estudiante.dto.EstudianteStatusRequestDTO;
import com.umg.sgau.estudiante.dto.EstudianteUpdateRequestDTO;
import com.umg.sgau.estudiante.entity.Estudiante;
import com.umg.sgau.estudiante.exception.EstudianteNoEncontradoException;
import com.umg.sgau.estudiante.mapper.EstudianteMapper;
import com.umg.sgau.estudiante.service.EstudianteService;
import com.umg.sgau.historialacademico.service.HistorialAcademicoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/estudiantes")
public class EstudianteController {

    private final EstudianteService estudianteService;
    private final HistorialAcademicoService historialAcademicoService;

    public EstudianteController(
            EstudianteService estudianteService,
            HistorialAcademicoService historialAcademicoService
    ) {
        this.estudianteService = estudianteService;
        this.historialAcademicoService = historialAcademicoService;
    }

    @PostMapping
    public ResponseEntity<?> crear(
            @Valid @RequestBody EstudianteCreateRequestDTO request
    ) {

        Estudiante estudianteCreado =
                estudianteService.crear(
                        EstudianteMapper.toEntity(request)
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        EstudianteMapper.toResponseDTO(
                                estudianteCreado
                        )
                );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(
            @PathVariable Long id
    ) {

        try {

            Estudiante estudiante =
                    estudianteService.obtenerPorId(id);

            return ResponseEntity.ok(
                    EstudianteMapper.toResponseDTO(estudiante)
            );

        } catch (EstudianteNoEncontradoException ex) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ex.getMessage());

        }

    }

    @GetMapping
    public ResponseEntity<?> listar(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(defaultValue = "0") Integer pagina,
            @RequestParam(defaultValue = "10") Integer tamanio
    ) {

        Pageable pageable =
                PageRequest.of(pagina, tamanio);

        Page<Estudiante> estudiantes =
                estudianteService.listar(
                        texto,
                        activo,
                        pageable
                );

        return ResponseEntity.ok(
                estudiantes.map(
                        EstudianteMapper::toResponseDTO
                )
        );

    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody EstudianteUpdateRequestDTO request
    ) {

        try {

            Estudiante estudianteActualizado =
                    estudianteService.actualizar(
                            id,
                            EstudianteMapper.toEntity(request)
                    );

            return ResponseEntity.ok(
                    EstudianteMapper.toResponseDTO(
                            estudianteActualizado
                    )
            );

        } catch (EstudianteNoEncontradoException ex) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ex.getMessage());

        }

    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody EstudianteStatusRequestDTO request
    ) {

        try {

            Estudiante estudiante =
                    estudianteService.cambiarEstado(
                            id,
                            request.getActivo()
                    );

            return ResponseEntity.ok(
                    EstudianteMapper.toResponseDTO(
                            estudiante
                    )
            );

        } catch (EstudianteNoEncontradoException ex) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ex.getMessage());

        }

    }

    @GetMapping("/{id}/resumen")
    public ResponseEntity<?> obtenerResumen(
            @PathVariable Long id
    ) {

        try {

            Estudiante estudiante =
                    estudianteService.obtenerResumenPorId(id);

            return ResponseEntity.ok(
                    EstudianteMapper.toSummaryDTO(
                            estudiante
                    )
            );

        } catch (EstudianteNoEncontradoException ex) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ex.getMessage());

        }

    }

    @GetMapping("/{id}/historial")
    public ResponseEntity<?> obtenerHistorial(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(historialAcademicoService.generarHistorial(id));

    }

    @GetMapping("/{id}/historial-academico")
    public ResponseEntity<?> obtenerHistorialAcademico(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(historialAcademicoService.generarHistorial(id));

    }

    @GetMapping("/{id}/historial-academico/ciclo/{cicloAnio}")
    public ResponseEntity<?> obtenerHistorialAcademicoPorCiclo(
            @PathVariable Long id,
            @PathVariable Integer cicloAnio
    ) {

        return ResponseEntity.ok(historialAcademicoService.generarHistorialPorCiclo(id, cicloAnio));

    }

    @GetMapping("/activos")
    public ResponseEntity<?> obtenerActivos() {
        return ResponseEntity.ok(
                EstudianteMapper.toResponseDTOList(
                        estudianteService.obtenerActivos()
                )
        );
    }

    @GetMapping("/activos/correos")
    public ResponseEntity<?> obtenerCorreosActivos() {
        return ResponseEntity.ok(estudianteService.obtenerCorreosActivos());
    }

}
