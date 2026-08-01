package com.umg.sgau.docente.serviceimpl;

import java.util.List;
import java.util.Optional;

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
		
		// TODO Auto-generated method stub
		return docenteRepository.save(docente);
	}

	@Override
	public Docente obtenerPorId(Long id) {
		Optional<Docente> docenteEncontrado = docenteRepository.findById(id);
		
		if (docenteEncontrado.isEmpty()) {
			throw new DocenteNoEncontradoException(id);
		}
		
		return docenteEncontrado.get();
	}

	@Override
	public List<Docente> obtenerTodos() {
		// TODO Auto-generated method stub
		return docenteRepository.findAll();
	}
	
	// necesita hacer dos cosas , tenemos que validar que exista ante de intentar actualizar
	@Override
	public Docente actualizar(Long id, Docente docente) {
		
		Optional<Docente> docenteExistente = docenteRepository.findById(id);
		
		if (docenteExistente.isEmpty()) {
			// si esta vacio este throw, sale del metodo termina la ejecucion y regresa la exception
			throw new DocenteNoEncontradoException(id);
		}
		
		Docente docenteActual = docenteExistente.get();
			
		docenteActual.setCodigoDocente(docente.getCodigoDocente());
		docenteActual.setEmail(docente.getEmail());
		docenteActual.setNombre(docente.getNombre());
		docenteActual.setApellido(docente.getApellido());
		docenteActual.setTelefono(docente.getTelefono());
		docenteActual.setEspecialidad(docente.getEspecialidad());
		docenteActual.setActivo(docente.getActivo());

		//no se usa update solo save
		return docenteRepository.save(docenteActual);
	}
		
	//softdelete no elimina solo inactiva 
	@Override
	public void eliminar(Long id) {
		
		Optional<Docente> docenteExistente = docenteRepository.findById(id);
		
		if (docenteExistente.isEmpty()) {
			// si esta vacio este throw, sale del metodo termina la ejecucion y regresa la exception
			throw new DocenteNoEncontradoException(id);
		}
		
		docenteRepository.deleteById(id);
	}
}