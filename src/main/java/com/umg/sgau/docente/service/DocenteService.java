

package com.umg.sgau.docente.service;

import java.util.List;

import com.umg.sgau.docente.entity.Docente;

public interface DocenteService {
	Docente crear(Docente docente);
	Docente obtenerPorId(Long id);
	
	List<Docente> obtenerTodos();
	Docente actualizar(Long id, Docente docente);
	
	void eliminar(Long id);
}
