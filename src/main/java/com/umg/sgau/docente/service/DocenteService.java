package com.umg.sgau.docente.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.umg.sgau.docente.entity.Docente;

public interface DocenteService {
	Docente crear(Docente docente);
	Docente obtenerPorId(Long id);
	
	List<Docente> obtenerTodos();
	Page<Docente> listar(String busqueda, Boolean activo, Pageable pageable);
	List<Docente> obtenerActivos();
	List<String> obtenerCorreosActivos();
	Docente actualizar(Long id, Docente docente);
	Docente cambiarEstado(Long id, Boolean activo);
	
	void eliminar(Long id);
}
