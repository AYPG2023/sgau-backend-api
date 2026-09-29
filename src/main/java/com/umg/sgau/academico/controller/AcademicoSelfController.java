package com.umg.sgau.academico.controller;

import com.umg.sgau.academico.dto.AcademicoDTOs;
import com.umg.sgau.academico.service.AcademicoSelfService;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/academico")
public class AcademicoSelfController {
    private final AcademicoSelfService service;
    public AcademicoSelfController(AcademicoSelfService service) { this.service = service; }

    @GetMapping("/docente/me")
    @PreAuthorize("hasAuthority('CURSOS_LEER')")
    public AcademicoDTOs.DocentePerfil docente(Authentication auth) { return service.docente(auth); }

    @GetMapping("/docente/me/cursos")
    @PreAuthorize("hasAuthority('CURSOS_LEER')")
    public List<AcademicoDTOs.Curso> cursosDocente(Authentication auth) { return service.cursosDocente(auth); }

    @GetMapping("/docente/me/cursos/{cursoId}/estudiantes")
    @PreAuthorize("hasAuthority('INSCRIPCIONES_LEER')")
    public Page<AcademicoDTOs.Inscripcion> alumnos(Authentication auth, @PathVariable Long cursoId, Pageable pageable) {
        return service.alumnosCurso(auth, cursoId, pageable);
    }

    @GetMapping("/docente/me/cursos/{cursoId}/notas")
    @PreAuthorize("hasAuthority('NOTAS_LEER')")
    public Page<AcademicoDTOs.Nota> notasCurso(Authentication auth, @PathVariable Long cursoId, Pageable pageable) {
        return service.notasCurso(auth, cursoId, pageable);
    }

    @GetMapping("/estudiante/me")
    @PreAuthorize("hasAuthority('ESTUDIANTES_LEER')")
    public AcademicoDTOs.EstudiantePerfil estudiante(Authentication auth) { return service.estudiante(auth); }

    @GetMapping("/estudiante/me/inscripciones")
    @PreAuthorize("hasAuthority('INSCRIPCIONES_LEER')")
    public Page<AcademicoDTOs.Inscripcion> inscripciones(Authentication auth, Pageable pageable) { return service.inscripcionesEstudiante(auth, pageable); }

    @GetMapping("/estudiante/me/cursos")
    @PreAuthorize("hasAuthority('CURSOS_LEER')")
    public List<AcademicoDTOs.Curso> cursos(Authentication auth) { return service.cursosEstudiante(auth); }

    @GetMapping("/estudiante/me/notas")
    @PreAuthorize("hasAuthority('NOTAS_LEER')")
    public Page<AcademicoDTOs.Nota> notas(Authentication auth, Pageable pageable) { return service.notasEstudiante(auth, pageable); }

    @GetMapping("/estudiante/me/promedio")
    @PreAuthorize("hasAuthority('NOTAS_LEER')")
    public AcademicoDTOs.Promedio promedio(Authentication auth) { return service.promedio(auth); }

    @GetMapping("/estudiante/me/colegiaturas")
    @PreAuthorize("hasAuthority('COLEGIATURAS_LEER')")
    public Page<AcademicoDTOs.Colegiatura> colegiaturas(Authentication auth, Pageable pageable) { return service.colegiaturas(auth, pageable); }

    @GetMapping("/estudiante/me/estado-cuenta")
    @PreAuthorize("hasAuthority('COLEGIATURAS_LEER')")
    public AcademicoDTOs.EstadoCuenta estadoCuenta(Authentication auth) { return service.estadoCuenta(auth); }
}
