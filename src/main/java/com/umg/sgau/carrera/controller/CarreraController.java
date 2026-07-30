package com.umg.sgau.carrera.controller;

import com.umg.sgau.carrera.dto.CarreraCreateRequestDTO;
import com.umg.sgau.carrera.dto.CarreraResponseDTO;
import com.umg.sgau.carrera.dto.CarreraStatusRequestDTO;
import com.umg.sgau.carrera.dto.CarreraSummaryDTO;
import com.umg.sgau.carrera.dto.CarreraUpdateRequestDTO;
import com.umg.sgau.carrera.entity.Carrera;
import com.umg.sgau.carrera.mapper.CarreraMapper;
import com.umg.sgau.carrera.service.CarreraService;
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
@RequestMapping("/api/carreras")
public class CarreraController {

    private final CarreraService carreraService;

    public CarreraController(CarreraService carreraService) {
        this.carreraService = carreraService;
    }

    @PostMapping
    public ResponseEntity<CarreraResponseDTO> crear(
            @Valid @RequestBody CarreraCreateRequestDTO request) {
        Carrera carrera = CarreraMapper.aEntidad(request);
        Carrera creada = carreraService.crear(carrera);
        CarreraResponseDTO response = CarreraMapper.aResponseDTO(creada);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<Page<CarreraResponseDTO>> listar(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) Boolean activo,
            Pageable pageable) {
        Page<Carrera> pagina = carreraService.listar(texto, activo, pageable);
        Page<CarreraResponseDTO> respuesta = pagina.map(CarreraMapper::aResponseDTO);

        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/activas")
    public ResponseEntity<List<CarreraSummaryDTO>> obtenerCarrerasActivas() {
        List<Carrera> carreras = carreraService.obtenerCarrerasActivas();
        List<CarreraSummaryDTO> respuesta = carreras.stream()
                .map(CarreraMapper::aSummaryDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/nombres-activos")
    public ResponseEntity<List<String>> obtenerNombresDeCarrerasActivas() {
        return ResponseEntity.ok(carreraService.obtenerNombresDeCarrerasActivas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarreraResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(
                CarreraMapper.aResponseDTO(carreraService.obtenerPorId(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CarreraResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody CarreraUpdateRequestDTO request) {
        Carrera datos = new Carrera();
        CarreraMapper.actualizarEntidad(request, datos);
        Carrera actualizada = carreraService.actualizar(id, datos);

        return ResponseEntity.ok(CarreraMapper.aResponseDTO(actualizada));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<CarreraResponseDTO> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CarreraStatusRequestDTO request) {
        Carrera carrera = carreraService.cambiarEstado(id, request.getActivo());

        return ResponseEntity.ok(CarreraMapper.aResponseDTO(carrera));
    }
}
