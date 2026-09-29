package com.umg.sgau.config;

import com.umg.sgau.colegiatura.repository.ColegiaturaRepository;
import com.umg.sgau.curso.repository.CursoRepository;
import com.umg.sgau.docente.repository.DocenteRepository;
import com.umg.sgau.estudiante.repository.EstudianteRepository;
import com.umg.sgau.nota.repository.NotaRepository;
import com.umg.sgau.inscripcion.repository.InscripcionRepository;
import com.umg.sgau.usuario.repository.UsuarioRepository;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("accessScope")
public class AccessScopeService {

    private final UsuarioRepository usuarioRepository;
    private final EstudianteRepository estudianteRepository;
    private final DocenteRepository docenteRepository;
    private final CursoRepository cursoRepository;
    private final NotaRepository notaRepository;
    private final ColegiaturaRepository colegiaturaRepository;
    private final InscripcionRepository inscripcionRepository;

    public AccessScopeService(UsuarioRepository usuarioRepository, EstudianteRepository estudianteRepository,
            DocenteRepository docenteRepository, CursoRepository cursoRepository,
            NotaRepository notaRepository, ColegiaturaRepository colegiaturaRepository,
            InscripcionRepository inscripcionRepository) {
        this.usuarioRepository = usuarioRepository;
        this.estudianteRepository = estudianteRepository;
        this.docenteRepository = docenteRepository;
        this.cursoRepository = cursoRepository;
        this.notaRepository = notaRepository;
        this.colegiaturaRepository = colegiaturaRepository;
        this.inscripcionRepository = inscripcionRepository;
    }

    public boolean esAdmin(Authentication authentication) {
        return tieneRol(authentication, "ROLE_ADMIN");
    }

    public boolean esEstudiantePropietario(Authentication authentication, Long estudianteId) {
        return esAdmin(authentication) || idEstudiante(authentication).filter(estudianteId::equals).isPresent();
    }

    public boolean puedeLeerNota(Authentication authentication, Long notaId) {
        if (esAdmin(authentication)) return true;
        Optional<Long> estudianteId = idEstudiante(authentication);
        if (estudianteId.isPresent()) return notaRepository.existsByIdAndEstudiante_Id(notaId, estudianteId.get());
        Optional<Long> docenteId = idDocente(authentication);
        return docenteId.isPresent() && notaRepository.existsGestionablePorDocente(notaId, docenteId.get());
    }

    public boolean puedeGestionarNota(Authentication authentication, Long notaId) {
        if (esAdmin(authentication)) return true;
        Optional<Long> docenteId = idDocente(authentication);
        return docenteId.isPresent() && notaRepository.existsGestionablePorDocente(notaId, docenteId.get());
    }

    public boolean puedeGestionarCurso(Authentication authentication, Long cursoId) {
        if (esAdmin(authentication)) return true;
        Optional<Long> docenteId = idDocente(authentication);
        return docenteId.isPresent() && cursoRepository.existsByIdAndDocente_Id(cursoId, docenteId.get());
    }

    public boolean puedeLeerCurso(Authentication authentication, Long cursoId) {
        if (puedeGestionarCurso(authentication, cursoId)) return true;
        Optional<Long> estudianteId = idEstudiante(authentication);
        return estudianteId.isPresent()
                && inscripcionRepository.existsByEstudiante_IdAndCurso_IdAndActivoTrue(estudianteId.get(), cursoId);
    }

    public boolean puedeLeerInscripcion(Authentication authentication, Long inscripcionId) {
        if (esAdmin(authentication)) return true;
        Optional<Long> estudianteId = idEstudiante(authentication);
        if (estudianteId.isPresent())
            return inscripcionRepository.existsByIdAndEstudiante_Id(inscripcionId, estudianteId.get());
        Optional<Long> docenteId = idDocente(authentication);
        return docenteId.isPresent()
                && inscripcionRepository.existsByIdAndCurso_Docente_Id(inscripcionId, docenteId.get());
    }

    public boolean puedeLeerNotasEstudianteCurso(Authentication authentication, Long estudianteId, Long cursoId) {
        if (esEstudiantePropietario(authentication, estudianteId)) return true;
        return puedeGestionarCurso(authentication, cursoId)
                && inscripcionRepository.existsByEstudiante_IdAndCurso_IdAndActivoTrue(estudianteId, cursoId);
    }

    public boolean puedeLeerColegiatura(Authentication authentication, Long colegiaturaId) {
        if (esAdmin(authentication)) return true;
        Optional<Long> estudianteId = idEstudiante(authentication);
        return estudianteId.isPresent()
                && colegiaturaRepository.existsByIdAndEstudiante_Id(colegiaturaId, estudianteId.get());
    }

    public boolean esDocentePropietario(Authentication authentication, Long docenteId) {
        return esAdmin(authentication) || idDocente(authentication).filter(docenteId::equals).isPresent();
    }

    public Optional<Long> idEstudiante(Authentication authentication) {
        return usuario(authentication).flatMap(u -> estudianteRepository.findByUsuarioId(u.getId()))
                .map(e -> e.getId());
    }

    public Optional<Long> idDocente(Authentication authentication) {
        return usuario(authentication).flatMap(u -> docenteRepository.findByUsuarioId(u.getId()))
                .map(d -> d.getId());
    }

    private Optional<com.umg.sgau.usuario.entity.Usuario> usuario(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return Optional.empty();
        return usuarioRepository.findWithRolesAndPermisosByUsernameIgnoreCase(authentication.getName());
    }

    private boolean tieneRol(Authentication authentication, String rol) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> rol.equals(a.getAuthority()));
    }
}
