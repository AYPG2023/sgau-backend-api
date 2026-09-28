package com.umg.sgau.estadogeneral.controller;

import com.umg.sgau.estadogeneral.dto.EstadoGeneralEstudianteResponseDTO;
import com.umg.sgau.estadogeneral.mapper.EstadoGeneralEstudianteMapper;
import com.umg.sgau.estadogeneral.service.EstadoGeneralEstudianteService;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/estudiantes")
public class EstadoGeneralEstudianteController {

    private final EstadoGeneralEstudianteService estadoGeneralEstudianteService;

    public EstadoGeneralEstudianteController(EstadoGeneralEstudianteService estadoGeneralEstudianteService) {
        this.estadoGeneralEstudianteService = estadoGeneralEstudianteService;
    }

    @GetMapping("/{estudianteId}/estado-general")
    @PreAuthorize("@accessScope.esEstudiantePropietario(authentication, #estudianteId)")
    public ResponseEntity<EstadoGeneralEstudianteResponseDTO> obtenerEstadoGeneral(
            @PathVariable @Positive(message = "El identificador del estudiante debe ser positivo") Long estudianteId) {
        return ResponseEntity.ok(
                EstadoGeneralEstudianteMapper.toResponseDTO(
                        estadoGeneralEstudianteService.obtenerEstadoGeneral(estudianteId)));
    }
}
