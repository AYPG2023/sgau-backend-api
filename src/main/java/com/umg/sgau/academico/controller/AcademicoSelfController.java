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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;

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
    public List<AcademicoDTOs.Curso> cursosDocente(Authentication auth,
            @RequestParam(required = false) Integer cicloAnio) { return service.cursosDocente(auth, cicloAnio); }

    @GetMapping("/docente/me/cursos/{cursoId}/estudiantes")
    @PreAuthorize("hasAuthority('INSCRIPCIONES_LEER')")
    public Page<AcademicoDTOs.Inscripcion> alumnos(Authentication auth, @PathVariable Long cursoId,
            @RequestParam(required = false) Integer cicloAnio, Pageable pageable) {
        return service.alumnosCurso(auth, cursoId, cicloAnio, pageable);
    }

    @GetMapping("/docente/me/cursos/{cursoId}/notas")
    @PreAuthorize("hasAuthority('NOTAS_LEER')")
    public Page<AcademicoDTOs.Nota> notasCurso(Authentication auth, @PathVariable Long cursoId,
            @RequestParam(required = false) Integer cicloAnio, Pageable pageable) {
        return service.notasCurso(auth, cursoId, cicloAnio, pageable);
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

    @GetMapping("/estudiante/me/cursos-inscritos")
    @PreAuthorize("hasAuthority('CURSOS_LEER')")
    public List<AcademicoDTOs.Curso> cursosInscritos(Authentication auth) { return service.cursosEstudiante(auth); }

    @GetMapping("/estudiante/me/carrera")
    @PreAuthorize("hasAuthority('ESTUDIANTES_LEER')")
    public AcademicoDTOs.Carrera carrera(Authentication auth) { return service.carreraEstudiante(auth); }

    @GetMapping("/estudiante/me/plan-carrera")
    @PreAuthorize("hasAuthority('CURSOS_LEER')")
    public AcademicoDTOs.PlanCarrera planCarrera(Authentication auth) { return service.planCarrera(auth); }

    @GetMapping("/estudiante/me/docentes")
    @PreAuthorize("hasAuthority('CURSOS_LEER')")
    public List<AcademicoDTOs.DocenteCurso> docentes(Authentication auth) { return service.docentesEstudiante(auth); }

    @GetMapping("/estudiante/me/notas")
    @PreAuthorize("hasAuthority('NOTAS_LEER')")
    public Page<AcademicoDTOs.Nota> notas(Authentication auth, @RequestParam(required=false) Integer cicloAnio, Pageable pageable) { return service.notasEstudiante(auth, cicloAnio, pageable); }

    @GetMapping("/estudiante/me/promedio")
    @PreAuthorize("hasAuthority('NOTAS_LEER')")
    public AcademicoDTOs.Promedio promedio(Authentication auth) { return service.promedio(auth); }

    @GetMapping("/estudiante/me/colegiaturas")
    @PreAuthorize("hasAuthority('COLEGIATURAS_LEER')")
    public Page<AcademicoDTOs.Colegiatura> colegiaturas(Authentication auth, Pageable pageable) { return service.colegiaturas(auth, pageable); }

    @GetMapping("/estudiante/me/estado-cuenta")
    @PreAuthorize("hasAuthority('COLEGIATURAS_LEER')")
    public AcademicoDTOs.EstadoCuenta estadoCuenta(Authentication auth) { return service.estadoCuenta(auth); }

    @GetMapping("/estudiante/me/carreras-disponibles")
    @PreAuthorize("hasAuthority('CARRERAS_LEER')")
    public List<AcademicoDTOs.CarreraDisponible> carrerasDisponibles(Authentication auth){return service.carrerasDisponibles(auth);}
    @GetMapping("/estudiante/me/ciclos-disponibles")
    @PreAuthorize("hasAuthority('INSCRIPCIONES_LEER')")
    public List<AcademicoDTOs.Ciclo> ciclosDisponibles(Authentication auth){return service.ciclosDisponibles(auth);}
    @GetMapping("/estudiante/me/grados-disponibles")
    @PreAuthorize("hasAuthority('INSCRIPCIONES_LEER')")
    public List<AcademicoDTOs.Grado> gradosDisponibles(Authentication auth){return service.gradosDisponibles(auth);}
    @GetMapping("/estudiante/me/grados/{gradoId}/secciones")
    @PreAuthorize("hasAuthority('INSCRIPCIONES_LEER')")
    public List<AcademicoDTOs.Seccion> seccionesDisponibles(Authentication auth,@PathVariable Long gradoId){return service.seccionesDisponibles(auth,gradoId);}
    @PostMapping("/estudiante/me/inscripciones")
    @PreAuthorize("hasRole('ESTUDIANTE') and hasAuthority('INSCRIPCIONES_CREAR')")
    public AcademicoDTOs.ResultadoInscripcion inscribirse(Authentication auth,@Valid @RequestBody AcademicoDTOs.SolicitudInscripcion request){return service.inscribirse(auth,request);}
    @PostMapping("/estudiante/me/cursos/{cursoId}/asignacion")
    @PreAuthorize("hasRole('ESTUDIANTE') and hasAuthority('INSCRIPCIONES_CREAR')")
    public AcademicoDTOs.Inscripcion asignarCurso(Authentication auth,@PathVariable Long cursoId){return service.asignarCurso(auth,cursoId);}
}
