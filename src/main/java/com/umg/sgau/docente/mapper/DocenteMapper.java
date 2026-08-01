package com.umg.sgau.docente.mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.umg.sgau.docente.dto.DocenteRequestDTO;
import com.umg.sgau.docente.dto.DocenteResponseDTO;
import com.umg.sgau.docente.entity.Docente;

public class DocenteMapper {

    public DocenteMapper() {
    }

    public static Docente aEntidad(DocenteRequestDTO dto) {

        Docente docente = new Docente();

        docente.setCodigoDocente(dto.getCodigoDocente());
        docente.setNombre(dto.getNombre());
        docente.setApellido(dto.getApellido());
        docente.setEmail(dto.getEmail());
        docente.setTelefono(dto.getTelefono());
        docente.setEspecialidad(dto.getEspecialidad());
        docente.setActivo(dto.getActivo());

        return docente;
    }
        public static DocenteResponseDTO aResponseDTO(Docente docente) {

            DocenteResponseDTO dto = new DocenteResponseDTO();

            dto.setId(docente.getId());
            dto.setCodigoDocente(docente.getCodigoDocente());
            dto.setNombre(docente.getNombre());
            dto.setApellido(docente.getApellido());
            dto.setEmail(docente.getEmail());
            dto.setTelefono(docente.getTelefono());
            dto.setEspecialidad(docente.getEspecialidad());
            dto.setActivo(docente.getActivo());
            dto.setFechaCreacion(docente.getFechaCreacion());

            return dto;
        }
        public static List<DocenteResponseDTO> aResponseDTOList(List<Docente> docentes) {
            return docentes.stream()
                    .map(DocenteMapper::aResponseDTO)
                    .collect(Collectors.toList());
        }
    }