package com.umg.sgau.colegiatura.controller;

import com.umg.sgau.colegiatura.dto.*;
import com.umg.sgau.colegiatura.entity.Colegiatura;
import com.umg.sgau.colegiatura.mapper.ColegiaturaMapper;
import com.umg.sgau.colegiatura.service.ColegiaturaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/colegiaturas")
public class ColegiaturaController {

    private final ColegiaturaService colegiaturaService;

    public ColegiaturaController(
            ColegiaturaService colegiaturaService
    ) {
        this.colegiaturaService = colegiaturaService;
    }

    @PostMapping
    public ResponseEntity<?> crear(
            @Valid
            @RequestBody
            ColegiaturaCreateRequestDTO request
    ) {

        try {

            Colegiatura colegiatura =
                    ColegiaturaMapper.toEntity(request);

            Colegiatura creada =
                    colegiaturaService.crear(colegiatura);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(
                            ColegiaturaMapper.toResponseDTO(
                                    creada
                            )
                    );

        } catch (IllegalArgumentException ex) {

            return ResponseEntity
                    .badRequest()
                    .body(ex.getMessage());

        }

    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(
            @PathVariable Long id
    ) {

        try {

            Colegiatura colegiatura =
                    colegiaturaService.obtenerPorId(id);

            return ResponseEntity.ok(
                    ColegiaturaMapper.toResponseDTO(
                            colegiatura
                    )
            );

        } catch (RuntimeException ex) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ex.getMessage());

        }

    }

    @GetMapping
    public ResponseEntity<?> listar(

            @RequestParam(required = false)
            Long estudianteId,

            @RequestParam(required = false)
            Integer cicloAnio,

            @RequestParam(required = false)
            String estado,

            @RequestParam(required = false)
            Boolean activo,

            @RequestParam(required = false)
            String concepto,

            @RequestParam(defaultValue = "0")
            Integer pagina,

            @RequestParam(defaultValue = "10")
            Integer tamanio

    ) {

        Pageable pageable =
                PageRequest.of(
                        pagina,
                        tamanio
                );

        Page<Colegiatura> colegiaturas =
                colegiaturaService.listar(
                        estudianteId,
                        cicloAnio,
                        estado,
                        activo,
                        concepto,
                        pageable
                );

        return ResponseEntity.ok(
                colegiaturas.map(
                        ColegiaturaMapper::toResponseDTO
                )
        );

    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(

            @PathVariable
            Long id,

            @Valid
            @RequestBody
            ColegiaturaUpdateRequestDTO request

    ) {

        try {

            Colegiatura colegiatura =
                    new Colegiatura();

            ColegiaturaMapper.updateEntity(
                    request,
                    colegiatura
            );

            Colegiatura actualizada =
                    colegiaturaService.actualizar(
                            id,
                            colegiatura
                    );

            return ResponseEntity.ok(
                    ColegiaturaMapper.toResponseDTO(
                            actualizada
                    )
            );

        } catch (RuntimeException ex) {

            return ResponseEntity
                    .badRequest()
                    .body(ex.getMessage());

        }

    }

    @PatchMapping("/{id}/pago")
    public ResponseEntity<?> registrarPago(

            @PathVariable
            Long id,

            @Valid
            @RequestBody
            ColegiaturaPagoRequestDTO request

    ) {

        try {

            Colegiatura actualizada =
                    colegiaturaService.registrarPago(
                            id,
                            request.getMontoPago()
                    );

            return ResponseEntity.ok(
                    ColegiaturaMapper.toResponseDTO(
                            actualizada
                    )
            );

        } catch (RuntimeException ex) {

            return ResponseEntity
                    .badRequest()
                    .body(ex.getMessage());

        }

    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(

            @PathVariable
            Long id,

            @Valid
            @RequestBody
            ColegiaturaStatusRequestDTO request

    ) {

        try {

            Colegiatura colegiatura =
                    colegiaturaService.cambiarEstado(
                            id,
                            request.getActivo()
                    );

            return ResponseEntity.ok(
                    ColegiaturaMapper.toResponseDTO(
                            colegiatura
                    )
            );

        } catch (RuntimeException ex) {

            return ResponseEntity
                    .badRequest()
                    .body(ex.getMessage());

        }

    }

    @GetMapping("/pendientes")
    public ResponseEntity<?> obtenerPendientes() {

        List<Colegiatura> colegiaturas =
                colegiaturaService.obtenerPendientes();

        return ResponseEntity.ok(
                ColegiaturaMapper.toResponseDTOList(
                        colegiaturas
                )
        );

    }

    @GetMapping("/saldos")
    public ResponseEntity<List<BigDecimal>> obtenerSaldosPendientes() {

        return ResponseEntity.ok(
                colegiaturaService.obtenerSaldosPendientes()
        );

    }

    @GetMapping("/estudiante/{estudianteId}")
    public ResponseEntity<?> obtenerHistorialPorEstudiante(
            @PathVariable Long estudianteId
    ) {

        List<Colegiatura> colegiaturas =
                colegiaturaService
                        .obtenerHistorialPorEstudiante(
                                estudianteId
                        );

        return ResponseEntity.ok(
                ColegiaturaMapper.toResponseDTOList(
                        colegiaturas
                )
        );

    }

}