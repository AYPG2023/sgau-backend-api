package com.umg.sgau.docente.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.umg.sgau.docente.dto.DocenteCreateRequestDTO;
import com.umg.sgau.docente.dto.DocenteResponseDTO;
import com.umg.sgau.docente.dto.DocenteStatusRequestDTO;
import com.umg.sgau.docente.dto.DocenteUpdateRequestDTO;
import com.umg.sgau.docente.entity.Docente;
import com.umg.sgau.docente.exception.DocenteNoEncontradoException;
import com.umg.sgau.docente.mapper.DocenteMapper;
import com.umg.sgau.docente.service.DocenteService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/docentes")
public class DocenteController {

    private final DocenteService docenteService;

    public DocenteController(DocenteService docenteService) {
        this.docenteService = docenteService;
    }

    // CREAR DOCENTE
    @PostMapping
    public ResponseEntity<DocenteResponseDTO> crear(
            @Valid @RequestBody DocenteCreateRequestDTO request) {

        Docente docenteCreado =
                docenteService.crearOVincular(DocenteMapper.aEntidad(request), request.getUsuarioId(),
                        request.getAccesoApp(), request.getUsername(), request.getPassword());

        DocenteResponseDTO response =
                DocenteMapper.aResponseDTO(docenteCreado);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // OBTENER DOCENTE POR ID
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {

        try {

            Docente docente = docenteService.obtenerPorId(id);

            return ResponseEntity.ok(
                    DocenteMapper.aResponseDTO(docente)
            );

        } catch (DocenteNoEncontradoException ex) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ex.getMessage());
        }
    }

    // OBTENER TODOS LOS DOCENTES
    @GetMapping
    public ResponseEntity<Page<DocenteResponseDTO>> listar(
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) Boolean activo,
            Pageable pageable) {

        Page<Docente> docentes = docenteService.listar(busqueda, activo, pageable);
        return ResponseEntity.ok(docentes.map(DocenteMapper::aResponseDTO));
    }

    @GetMapping("/activos")
    public ResponseEntity<List<DocenteResponseDTO>> obtenerActivos() {
        return ResponseEntity.ok(
                DocenteMapper.aResponseDTOList(docenteService.obtenerActivos()));
    }

    @GetMapping("/activos/correos")
    public ResponseEntity<List<String>> obtenerCorreosActivos() {
        return ResponseEntity.ok(docenteService.obtenerCorreosActivos());
    }

    // ACTUALIZAR DOCENTE
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody DocenteUpdateRequestDTO request) {

        try {
            Docente datos = new Docente();
            DocenteMapper.actualizarEntidad(request, datos);

            Docente docenteActualizado =
                    docenteService.actualizar(id, datos);

            return ResponseEntity.ok(
                    DocenteMapper.aResponseDTO(docenteActualizado)
            );

        } catch (DocenteNoEncontradoException ex) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ex.getMessage());
        }
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody DocenteStatusRequestDTO request) {

        try {
            Docente docente = docenteService.cambiarEstado(id, request.getActivo());
            return ResponseEntity.ok(DocenteMapper.aResponseDTO(docente));
        } catch (DocenteNoEncontradoException ex) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ex.getMessage());
        }
    }

    // ELIMINAR DOCENTE
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {

        try {

            docenteService.eliminar(id);

            return ResponseEntity
                    .noContent()
                    .build();

        } catch (DocenteNoEncontradoException ex) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ex.getMessage());
        }
    }
}
