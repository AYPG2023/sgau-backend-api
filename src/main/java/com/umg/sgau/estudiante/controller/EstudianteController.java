package com.umg.sgau.estudiante.controller;

import com.umg.sgau.estudiante.entity.Estudiante;
import com.umg.sgau.estudiante.exception.EstudianteNoEncontradoException;
import com.umg.sgau.estudiante.mapper.EstudianteMapper;
import com.umg.sgau.estudiante.service.EstudianteService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/estudiantes")
public class EstudianteController {

    private final EstudianteService estudianteService;

    public EstudianteController(
            EstudianteService estudianteService
    ) {
        this.estudianteService = estudianteService;
    }

    @PostMapping
    public ResponseEntity<?> crear(
            @RequestBody Estudiante estudiante
    ) {

        Estudiante estudianteCreado =
                estudianteService.crear(estudiante);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        EstudianteMapper.aResponseDTO(
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
                    EstudianteMapper.aResponseDTO(estudiante)
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
                        EstudianteMapper::aResponseDTO
                )
        );

    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(
            @PathVariable Long id,
            @RequestBody Estudiante estudiante
    ) {

        try {

            Estudiante estudianteActualizado =
                    estudianteService.actualizar(
                            id,
                            estudiante
                    );

            return ResponseEntity.ok(
                    EstudianteMapper.aResponseDTO(
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
            @RequestParam Boolean activo
    ) {

        try {

            Estudiante estudiante =
                    estudianteService.cambiarEstado(
                            id,
                            activo
                    );

            return ResponseEntity.ok(
                    EstudianteMapper.aResponseDTO(
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
                    EstudianteMapper.aSummaryDTO(
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

        try {

            return ResponseEntity.ok(
                    estudianteService.obtenerHistorialAcademico(id)
            );

        } catch (RuntimeException ex) {

            return ResponseEntity
                    .status(HttpStatus.NOT_IMPLEMENTED)
                    .body(ex.getMessage());

        }

    }

}