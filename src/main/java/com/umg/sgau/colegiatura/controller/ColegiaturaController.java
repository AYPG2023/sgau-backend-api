package com.umg.sgau.colegiatura.controller;

import com.umg.sgau.colegiatura.dto.ColegiaturaCreateRequestDTO;
import com.umg.sgau.colegiatura.dto.ColegiaturaPagoRequestDTO;
import com.umg.sgau.colegiatura.dto.ColegiaturaResponseDTO;
import com.umg.sgau.colegiatura.dto.ColegiaturaStatusRequestDTO;
import com.umg.sgau.colegiatura.dto.ColegiaturaUpdateRequestDTO;
import com.umg.sgau.colegiatura.dto.EstadoCuentaResponseDTO;
import com.umg.sgau.colegiatura.entity.Colegiatura;
import com.umg.sgau.colegiatura.mapper.ColegiaturaMapper;
import com.umg.sgau.colegiatura.service.ColegiaturaService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
@RequestMapping("/api/colegiaturas")
public class ColegiaturaController {

    private final ColegiaturaService colegiaturaService;

    public ColegiaturaController(ColegiaturaService colegiaturaService) {
        this.colegiaturaService = colegiaturaService;
    }

    @PostMapping
    public ResponseEntity<ColegiaturaResponseDTO> crear(
            @Valid @RequestBody ColegiaturaCreateRequestDTO request) {
        Colegiatura colegiatura = ColegiaturaMapper.toEntity(request);
        Colegiatura creada = colegiaturaService.crear(colegiatura);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ColegiaturaMapper.toResponseDTO(creada));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ColegiaturaResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ColegiaturaMapper.toResponseDTO(colegiaturaService.obtenerPorId(id)));
    }

    @GetMapping
    public ResponseEntity<Page<ColegiaturaResponseDTO>> listar(
            @RequestParam(required = false) Long estudianteId,
            @RequestParam(required = false) Integer cicloAnio,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) String concepto,
            @RequestParam(defaultValue = "0") Integer pagina,
            @RequestParam(defaultValue = "10") Integer tamanio) {
        Pageable pageable = PageRequest.of(pagina, tamanio);
        Page<Colegiatura> colegiaturas = colegiaturaService.listar(
                estudianteId,
                cicloAnio,
                estado,
                activo,
                concepto,
                pageable);
        return ResponseEntity.ok(colegiaturas.map(ColegiaturaMapper::toResponseDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ColegiaturaResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ColegiaturaUpdateRequestDTO request) {
        Colegiatura colegiatura = new Colegiatura();
        ColegiaturaMapper.updateEntity(request, colegiatura);
        Colegiatura actualizada = colegiaturaService.actualizar(id, colegiatura);
        return ResponseEntity.ok(ColegiaturaMapper.toResponseDTO(actualizada));
    }

    @PatchMapping("/{id}/pago")
    public ResponseEntity<ColegiaturaResponseDTO> registrarPago(
            @PathVariable Long id,
            @Valid @RequestBody ColegiaturaPagoRequestDTO request) {
        Colegiatura actualizada = colegiaturaService.registrarPago(id, request.getMontoPago());
        return ResponseEntity.ok(ColegiaturaMapper.toResponseDTO(actualizada));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ColegiaturaResponseDTO> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody ColegiaturaStatusRequestDTO request) {
        Colegiatura colegiatura = colegiaturaService.cambiarEstado(id, request.getActivo());
        return ResponseEntity.ok(ColegiaturaMapper.toResponseDTO(colegiatura));
    }

    @GetMapping("/pendientes")
    public ResponseEntity<List<ColegiaturaResponseDTO>> obtenerPendientes() {
        return ResponseEntity.ok(
                ColegiaturaMapper.toResponseDTOList(colegiaturaService.obtenerPendientes()));
    }

    @GetMapping("/saldos")
    public ResponseEntity<List<BigDecimal>> obtenerSaldosPendientes() {
        return ResponseEntity.ok(colegiaturaService.obtenerSaldosPendientes());
    }

    @GetMapping("/estudiante/{estudianteId}")
    public ResponseEntity<List<ColegiaturaResponseDTO>> obtenerHistorialPorEstudiante(
            @PathVariable Long estudianteId) {
        return ResponseEntity.ok(
                ColegiaturaMapper.toResponseDTOList(
                        colegiaturaService.obtenerHistorialPorEstudiante(estudianteId)));
    }

    @GetMapping("/estudiante/{estudianteId}/activas")
    public ResponseEntity<Page<ColegiaturaResponseDTO>> obtenerActivasPorEstudiante(
            @PathVariable Long estudianteId,
            Pageable pageable) {
        return ResponseEntity.ok(
                colegiaturaService.obtenerActivasPorEstudiante(estudianteId, pageable)
                        .map(ColegiaturaMapper::toResponseDTO));
    }

    @GetMapping("/estudiante/{estudianteId}/pendientes")
    public ResponseEntity<Page<ColegiaturaResponseDTO>> obtenerPendientesPorEstudiante(
            @PathVariable Long estudianteId,
            Pageable pageable) {
        return ResponseEntity.ok(
                colegiaturaService.obtenerPendientesPorEstudiante(estudianteId, pageable)
                        .map(ColegiaturaMapper::toResponseDTO));
    }

    @GetMapping("/estudiante/{estudianteId}/estado-cuenta")
    public ResponseEntity<EstadoCuentaResponseDTO> obtenerEstadoCuenta(
            @PathVariable Long estudianteId) {
        return ResponseEntity.ok(
                ColegiaturaMapper.toEstadoCuentaDTO(
                        colegiaturaService.generarEstadoCuenta(estudianteId)));
    }
}
