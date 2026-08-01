package com.umg.sgau.docente.serviceimpl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.umg.sgau.docente.entity.Docente;
import com.umg.sgau.docente.exception.DocenteDuplicadoException;
import com.umg.sgau.docente.exception.DocenteNoEncontradoException;
import com.umg.sgau.docente.repository.DocenteRepository;
import com.umg.sgau.docente.service.DocenteService;

//es un servicio
@Service
public class DocenteServiceImpl implements DocenteService {
	
	private final DocenteRepository docenteRepository;
	
	//inyeccion de dependencia por constructor
	public DocenteServiceImpl(DocenteRepository docenteRepository) {
		this.docenteRepository = docenteRepository;
	}

	@Override
	public Docente crear(Docente docente) {
		
		// regla de negocio: no permitir codigo de docente duplicado
		if (docenteRepository.existsByCodigoDocente(docente.getCodigoDocente())) {
			throw new DocenteDuplicadoException(
					"Ya existe un docente con el código: " + docente.getCodigoDocente()
			);
		}
		
		// regla de negocio: no permitir email duplicado
		if (docenteRepository.existsByEmail(docente.getEmail())) {
			throw new DocenteDuplicadoException(
					"Ya existe un docente con el email: " + docente.getEmail()
			);
		}
		
		docente.setActivo(true);
		return docenteRepository.save(docente);
	}

	@Override
	public Docente obtenerPorId(Long id) {
		return docenteRepository.findById(id)
				.orElseThrow(() -> new DocenteNoEncontradoException(id));
	}

	@Override
	public List<Docente> obtenerTodos() {
		return docenteRepository.findAll();
	}

	@Override
	public Page<Docente> listar(String busqueda, Boolean activo, Pageable pageable) {
		return docenteRepository.buscarConFiltros(normalizarBusqueda(busqueda), activo, pageable);
	}

	@Override
	public List<Docente> obtenerActivos() {
		return docenteRepository.findAll()
				.stream()
				.filter(docente -> Boolean.TRUE.equals(docente.getActivo()))
				.collect(Collectors.toList());
	}

	@Override
	public List<String> obtenerCorreosActivos() {
		return docenteRepository.findAll()
				.stream()
				.filter(docente -> Boolean.TRUE.equals(docente.getActivo()))
				.map(Docente::getEmail)
				.collect(Collectors.toList());
	}
	
	@Override
	public Docente actualizar(Long id, Docente docente) {
		
		Docente docenteActual = obtenerPorId(id);

		if (docenteRepository.existsByCodigoDocenteAndIdNot(docente.getCodigoDocente(), id)) {
			throw new DocenteDuplicadoException(
					"Ya existe un docente con el codigo: " + docente.getCodigoDocente()
			);
		}

		if (docenteRepository.existsByEmailAndIdNot(docente.getEmail(), id)) {
			throw new DocenteDuplicadoException(
					"Ya existe un docente con el email: " + docente.getEmail()
			);
		}
			
		docenteActual.setCodigoDocente(docente.getCodigoDocente());
		docenteActual.setEmail(docente.getEmail());
		docenteActual.setNombre(docente.getNombre());
		docenteActual.setApellido(docente.getApellido());
		docenteActual.setTelefono(docente.getTelefono());
		docenteActual.setEspecialidad(docente.getEspecialidad());

		return docenteRepository.save(docenteActual);
	}

	@Override
	public Docente cambiarEstado(Long id, Boolean activo) {
		Docente docente = obtenerPorId(id);
		docente.setActivo(activo);
		return docenteRepository.save(docente);
	}
		
	@Override
	public void eliminar(Long id) {
		cambiarEstado(id, false);
	}

	private String normalizarBusqueda(String busqueda) {
		if (busqueda == null || busqueda.isBlank()) {
			return "";
		}
		return busqueda.trim();
	}
}
