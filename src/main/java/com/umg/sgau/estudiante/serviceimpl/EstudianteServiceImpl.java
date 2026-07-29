package com.umg.sgau.estudiante.serviceimpl;

import com.umg.sgau.estudiante.entity.Estudiante;
import com.umg.sgau.estudiante.exception.EstudianteNoEncontradoException;
import com.umg.sgau.estudiante.repository.EstudianteRepository;
import com.umg.sgau.estudiante.service.EstudianteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class EstudianteServiceImpl implements EstudianteService {

    private final EstudianteRepository estudianteRepository;

    @Override
    public Estudiante crear(Estudiante estudiante) {

        if (estudianteRepository.existsByCodigoEstudiantil(
                estudiante.getCodigoEstudiantil())) {
            throw new IllegalArgumentException(
                    "El código estudiantil ya existe.");
        }

        if (estudianteRepository.existsByNumeroIdentificacion(
                estudiante.getNumeroIdentificacion())) {
            throw new IllegalArgumentException(
                    "El número de identificación ya existe.");
        }

        if (estudianteRepository.existsByCorreo(
                estudiante.getCorreo())) {
            throw new IllegalArgumentException(
                    "El correo electrónico ya existe.");
        }

        estudiante.setActivo(true);

        return estudianteRepository.save(estudiante);
    }

    @Override
    @Transactional(readOnly = true)
    public Estudiante obtenerPorId(Long id) {

        Optional<Estudiante> estudianteEncontrado =
                estudianteRepository.findById(id);

        if (estudianteEncontrado.isEmpty()) {
            throw new EstudianteNoEncontradoException(id);
        }

        return estudianteEncontrado.get();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Estudiante> listar(
            String texto,
            Boolean activo,
            Pageable pageable) {

        throw new UnsupportedOperationException(
                "Pendiente de implementación.");
    }

    @Override
    public Estudiante actualizar(
            Long id,
            Estudiante estudiante) {

        Optional<Estudiante> estudianteExistente =
                estudianteRepository.findById(id);

        if (estudianteExistente.isEmpty()) {
            throw new EstudianteNoEncontradoException(id);
        }

        Estudiante estudianteActual = estudianteExistente.get();

        if (estudianteRepository.existsByCodigoEstudiantilAndIdNot(
                estudiante.getCodigoEstudiantil(), id)) {
            throw new IllegalArgumentException(
                    "El código estudiantil ya existe.");
        }

        if (estudianteRepository.existsByNumeroIdentificacionAndIdNot(
                estudiante.getNumeroIdentificacion(), id)) {
            throw new IllegalArgumentException(
                    "El número de identificación ya existe.");
        }

        if (estudianteRepository.existsByCorreoAndIdNot(
                estudiante.getCorreo(), id)) {
            throw new IllegalArgumentException(
                    "El correo electrónico ya existe.");
        }

        estudianteActual.setCodigoEstudiantil(
                estudiante.getCodigoEstudiantil());

        estudianteActual.setNumeroIdentificacion(
                estudiante.getNumeroIdentificacion());

        estudianteActual.setNombres(
                estudiante.getNombres());

        estudianteActual.setApellidos(
                estudiante.getApellidos());

        estudianteActual.setFechaNacimiento(
                estudiante.getFechaNacimiento());

        estudianteActual.setCorreo(
                estudiante.getCorreo());

        estudianteActual.setTelefono(
                estudiante.getTelefono());

        estudianteActual.setDireccion(
                estudiante.getDireccion());

        return estudianteRepository.save(estudianteActual);
    }

    @Override
    public Estudiante cambiarEstado(
            Long id,
            Boolean activo) {

        Optional<Estudiante> estudianteExistente =
                estudianteRepository.findById(id);

        if (estudianteExistente.isEmpty()) {
            throw new EstudianteNoEncontradoException(id);
        }

        Estudiante estudiante = estudianteExistente.get();

        estudiante.setActivo(activo);

        return estudianteRepository.save(estudiante);
    }

    @Override
    public Estudiante obtenerResumenPorId(Long id) {

        return obtenerPorId(id);
    }

    @Override
    public Object obtenerHistorialAcademico(Long id) {

        throw new UnsupportedOperationException(
                "El historial académico estará disponible cuando el módulo de notas sea integrado."
        );
    }

}