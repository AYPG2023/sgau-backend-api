package com.umg.sgau.docente.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.umg.sgau.docente.dto.DocenteRequestDTO;
import com.umg.sgau.docente.dto.DocenteResponseDTO;
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
    public ResponseEntity<?> crear(
            @Valid @RequestBody DocenteRequestDTO request) {

        Docente docenteCreado =
                docenteService.crear(DocenteMapper.aEntidad(request));

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
    public ResponseEntity<?> obtenerTodos() {

        List<Docente> docentes =
                docenteService.obtenerTodos();

        List<DocenteResponseDTO> response =
                DocenteMapper.aResponseDTOList(docentes);

        return ResponseEntity.ok(response);
    }

    // ACTUALIZAR DOCENTE
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody DocenteRequestDTO request) {

        try {

            Docente docenteActualizado =
                    docenteService.actualizar(
                            id,
                            DocenteMapper.aEntidad(request)
                    );

            return ResponseEntity.ok(
                    DocenteMapper.aResponseDTO(docenteActualizado)
            );

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