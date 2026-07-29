package com.umg.sgau.estudiante.serviceimpl;

import com.umg.sgau.estudiante.entity.Estudiante;
import com.umg.sgau.estudiante.repository.EstudianteRepository;
import com.umg.sgau.estudiante.service.EstudianteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class EstudianteServiceImpl implements EstudianteService {

    private final EstudianteRepository estudianteRepository;

    @Override
    public Estudiante crear(Estudiante estudiante) {

        if (estudianteRepository.existsByCodigoEstudiantil(estudiante.getCodigoEstudiantil())) {
            throw new IllegalArgumentException("El código estudiantil ya existe.");
        }

        if (estudianteRepository.existsByNumeroIdentificacion(estudiante.getNumeroIdentificacion())) {
            throw new IllegalArgumentException("El número de identificación ya existe.");
        }

        if (estudianteRepository.existsByCorreo(estudiante.getCorreo())) {
            throw new IllegalArgumentException("El correo electrónico ya existe.");
        }

        estudiante.setActivo(true);

        return estudianteRepository.save(estudiante);
    }

    @Override
    @Transactional(readOnly = true)
    public Estudiante obtenerPorId(Long id) {

        return estudianteRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Estudiante no encontrado."));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Estudiante> listar(String texto,
                                   Boolean activo,
                                   Pageable pageable) {

        throw new UnsupportedOperationException("Pendiente de implementación.");
    }

    @Override
    public Estudiante actualizar(Long id,
                                 Estudiante estudiante) {

        throw new UnsupportedOperationException("Pendiente de implementación.");
    }

    @Override
    public Estudiante cambiarEstado(Long id,
                                    Boolean activo) {

        throw new UnsupportedOperationException("Pendiente de implementación.");
    }

    @Override
    @Transactional(readOnly = true)
    public Estudiante obtenerResumenPorId(Long id) {

        return obtenerPorId(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Object obtenerHistorialAcademico(Long id) {

        throw new UnsupportedOperationException(
                "El historial académico estará disponible cuando el módulo de notas sea integrado."
        );
    }

}