package com.umg.sgau.academico.service;

import com.umg.sgau.academico.dto.AcademicoDTOs;
import com.umg.sgau.academico.exception.VinculacionAcademicaNoEncontradaException;
import com.umg.sgau.colegiatura.entity.Colegiatura;
import com.umg.sgau.colegiatura.repository.ColegiaturaRepository;
import com.umg.sgau.config.AccessScopeService;
import com.umg.sgau.curso.entity.Curso;
import com.umg.sgau.curso.repository.CursoRepository;
import com.umg.sgau.carrera.repository.CarreraRepository;
import com.umg.sgau.docente.entity.Docente;
import com.umg.sgau.docente.repository.DocenteRepository;
import com.umg.sgau.estudiante.entity.Estudiante;
import com.umg.sgau.estudiante.repository.EstudianteRepository;
import com.umg.sgau.inscripcion.entity.Inscripcion;
import com.umg.sgau.inscripcion.repository.InscripcionRepository;
import com.umg.sgau.nota.entity.Nota;
import com.umg.sgau.nota.repository.NotaRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AcademicoSelfService {
    private final AccessScopeService accessScope;
    private final DocenteRepository docentes;
    private final EstudianteRepository estudiantes;
    private final CursoRepository cursos;
    private final CarreraRepository carreras;
    private final InscripcionRepository inscripciones;
    private final NotaRepository notas;
    private final ColegiaturaRepository colegiaturas;

    public AcademicoSelfService(AccessScopeService accessScope, DocenteRepository docentes,
            EstudianteRepository estudiantes, CursoRepository cursos, CarreraRepository carreras,
            InscripcionRepository inscripciones, NotaRepository notas,
            ColegiaturaRepository colegiaturas) {
        this.accessScope = accessScope;
        this.docentes = docentes;
        this.estudiantes = estudiantes;
        this.cursos = cursos;
        this.carreras = carreras;
        this.inscripciones = inscripciones;
        this.notas = notas;
        this.colegiaturas = colegiaturas;
    }

    public AcademicoDTOs.DocentePerfil docente(Authentication auth) {
        Docente d = docenteActual(auth);
        return new AcademicoDTOs.DocentePerfil(d.getId(), d.getCodigoDocente(), d.getNombre(),
                d.getApellido(), d.getEmail(), d.getTelefono(), d.getEspecialidad(), d.getActivo(),
                d.getUsuario() == null ? "PERFIL_HISTORICO" : "USUARIO");
    }

    public List<AcademicoDTOs.Curso> cursosDocente(Authentication auth) {
        return cursos.findByDocente_Id(docenteActual(auth).getId()).stream().map(this::curso).toList();
    }

    public List<AcademicoDTOs.Curso> cursosDocente(Authentication auth, Integer cicloAnio) {
        Docente docente = docenteActual(auth);
        return (cicloAnio == null ? cursos.findByDocente_Id(docente.getId())
                : cursos.findByDocente_IdAndCicloAnio(docente.getId(), cicloAnio)).stream().map(this::curso).toList();
    }

    public Page<AcademicoDTOs.Inscripcion> alumnosCurso(Authentication auth, Long cursoId, Pageable pageable) {
        exigirCursoDocente(auth, cursoId);
        return inscripciones.findByCurso_IdAndActivoTrue(cursoId, pageable).map(this::inscripcion);
    }

    public Page<AcademicoDTOs.Inscripcion> alumnosCurso(Authentication auth, Long cursoId, Integer cicloAnio, Pageable pageable) {
        Curso c = exigirCursoDocente(auth, cursoId);
        if (cicloAnio != null && !cicloAnio.equals(c.getCicloAnio())) throw new org.springframework.security.access.AccessDeniedException("El ciclo no corresponde al curso asignado.");
        return cicloAnio == null ? inscripciones.findByCurso_IdAndActivoTrue(cursoId, pageable).map(this::inscripcion)
                : inscripciones.findByCurso_IdAndCicloAnioAndActivoTrue(cursoId, cicloAnio, pageable).map(this::inscripcion);
    }

    public Page<AcademicoDTOs.Nota> notasCurso(Authentication auth, Long cursoId, Pageable pageable) {
        exigirCursoDocente(auth, cursoId);
        return notas.findByCurso_Id(cursoId, pageable).map(this::nota);
    }

    public Page<AcademicoDTOs.Nota> notasCurso(Authentication auth, Long cursoId, Integer cicloAnio, Pageable pageable) {
        Curso c = exigirCursoDocente(auth, cursoId);
        if (cicloAnio != null && !cicloAnio.equals(c.getCicloAnio())) throw new org.springframework.security.access.AccessDeniedException("El ciclo no corresponde al curso asignado.");
        return notas.findValidasByCurso(cursoId, cicloAnio, pageable).map(this::nota);
    }

    public AcademicoDTOs.EstudiantePerfil estudiante(Authentication auth) {
        Estudiante e = estudianteActual(auth);
        return new AcademicoDTOs.EstudiantePerfil(e.getId(), e.getCodigoEstudiantil(), e.getNombres(),
                e.getApellidos(), e.getCorreo(), e.getTelefono(), e.getDireccion(), e.getActivo(),
                e.getUsuario() == null ? "PERFIL_HISTORICO" : "USUARIO");
    }

    public Page<AcademicoDTOs.Inscripcion> inscripcionesEstudiante(Authentication auth, Pageable pageable) {
        return inscripciones.findByEstudiante_Id(estudianteActual(auth).getId(), pageable).map(this::inscripcion);
    }

    public List<AcademicoDTOs.Curso> cursosEstudiante(Authentication auth) {
        Page<Inscripcion> pagina = inscripciones.findByEstudiante_IdAndActivoTrue(
                estudianteActual(auth).getId(), Pageable.unpaged());
        var unicos = new LinkedHashMap<Long, Curso>();
        pagina.getContent().stream().filter(i -> i.getCurso() != null)
                .forEach(i -> unicos.putIfAbsent(i.getCurso().getId(), i.getCurso()));
        return unicos.values().stream().map(this::curso).toList();
    }

    public AcademicoDTOs.Carrera carreraEstudiante(Authentication auth) {
        Inscripcion i = inscripcionActual(estudianteActual(auth));
        var c = i.getCarrera();
        return new AcademicoDTOs.Carrera(c.getId(), c.getCodigo(), c.getNombre(), c.getDescripcion(), c.getDuracionAnios(), i.getCicloAnio());
    }

    public AcademicoDTOs.PlanCarrera planCarrera(Authentication auth) {
        Estudiante e = estudianteActual(auth);
        Inscripcion actual = inscripcionActual(e);
        var carrera = carreras.findById(actual.getCarrera().getId())
                .orElseThrow(() -> new VinculacionAcademicaNoEncontradaException("carrera"));
        List<Inscripcion> activas = inscripciones.findByEstudiante_IdAndActivoTrueOrderByCicloAnioDescFechaInscripcionDesc(e.getId());
        var cursosInscritos = activas.stream().filter(i -> i.getCurso() != null)
                .collect(java.util.stream.Collectors.toMap(i -> i.getCurso().getId(), Inscripcion::getCurso, (a,b)->a, LinkedHashMap::new));
        List<Curso> plan = cursos.findByCarrera_IdAndActivoTrueOrderByCicloAnioDescNombreAsc(carrera.getId());
        List<AcademicoDTOs.CursoPlan> detalle = plan.stream().map(c -> {
            var d=c.getDocente();
            return new AcademicoDTOs.CursoPlan(c.getId(),c.getCodigo(),c.getNombre(),c.getDescripcion(),c.getCreditos(),
                    c.getHorasSemanales(),c.getCicloAnio(),cursosInscritos.containsKey(c.getId()),d==null?null:d.getId(),
                    d==null?null:d.getNombre()+" "+d.getApellido());
        }).toList();
        int totalPlan=plan.stream().mapToInt(Curso::getCreditos).sum();
        int totalInscritos=cursosInscritos.values().stream().mapToInt(Curso::getCreditos).sum();
        var resumen=new AcademicoDTOs.Carrera(carrera.getId(),carrera.getCodigo(),carrera.getNombre(),carrera.getDescripcion(),carrera.getDuracionAnios(),actual.getCicloAnio());
        return new AcademicoDTOs.PlanCarrera(resumen,detalle,cursosInscritos.values().stream().map(this::curso).toList(),totalPlan,totalInscritos);
    }

    public List<AcademicoDTOs.DocenteCurso> docentesEstudiante(Authentication auth) {
        Estudiante e=estudianteActual(auth);
        return inscripciones.findByEstudiante_IdAndActivoTrueOrderByCicloAnioDescFechaInscripcionDesc(e.getId()).stream()
                .filter(i->i.getCurso()!=null && i.getCurso().getDocente()!=null).map(i->{var c=i.getCurso();var d=c.getDocente();
                    return new AcademicoDTOs.DocenteCurso(d.getId(),d.getCodigoDocente(),d.getNombre(),d.getApellido(),d.getEmail(),d.getEspecialidad(),c.getId(),c.getCodigo(),c.getNombre(),i.getCicloAnio());
                }).toList();
    }

    public Page<AcademicoDTOs.Nota> notasEstudiante(Authentication auth, Pageable pageable) {
        return notas.findByEstudiante_Id(estudianteActual(auth).getId(), pageable).map(this::nota);
    }

    public Page<AcademicoDTOs.Nota> notasEstudiante(Authentication auth, Integer cicloAnio, Pageable pageable) {
        Long id = estudianteActual(auth).getId();
        return (cicloAnio == null ? notas.findByEstudiante_IdAndActivoTrue(id, pageable)
                : notas.findByEstudiante_IdAndCicloAnioAndActivoTrue(id, cicloAnio, pageable)).map(this::nota);
    }

    public AcademicoDTOs.Promedio promedio(Authentication auth) {
        Estudiante e = estudianteActual(auth);
        List<Nota> activas = notas.findByEstudiante_IdAndActivoTrue(e.getId());
        BigDecimal promedio = activas.stream().map(Nota::getCalificacion)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (!activas.isEmpty()) promedio = promedio.divide(BigDecimal.valueOf(activas.size()), 2, RoundingMode.HALF_UP);
        return new AcademicoDTOs.Promedio(e.getId(), promedio.setScale(2, RoundingMode.HALF_UP), activas.size());
    }

    public Page<AcademicoDTOs.Colegiatura> colegiaturas(Authentication auth, Pageable pageable) {
        return colegiaturas.findByEstudiante_Id(estudianteActual(auth).getId(), pageable).map(this::colegiatura);
    }

    public AcademicoDTOs.EstadoCuenta estadoCuenta(Authentication auth) {
        Estudiante e = estudianteActual(auth);
        List<Colegiatura> cargos = colegiaturas
                .findByEstudiante_IdAndActivoTrueOrderByCicloAnioDescFechaEmisionDesc(e.getId());
        BigDecimal total = sumar(cargos, 0); BigDecimal pagado = sumar(cargos, 1); BigDecimal saldo = sumar(cargos, 2);
        int pendientes = (int) cargos.stream().filter(c -> c.getSaldoPendiente().compareTo(BigDecimal.ZERO) > 0).count();
        return new AcademicoDTOs.EstadoCuenta(e.getId(), total, pagado, saldo, cargos.size(), pendientes,
                cargos.stream().map(this::colegiatura).toList());
    }

    private BigDecimal sumar(List<Colegiatura> cargos, int tipo) {
        return cargos.stream().map(c -> tipo == 0 ? c.getMontoTotal() : tipo == 1 ? c.getMontoPagado() : c.getSaldoPendiente())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Docente docenteActual(Authentication auth) {
        Long id = accessScope.idDocente(auth).orElseThrow(() -> new VinculacionAcademicaNoEncontradaException("docente"));
        return docentes.findById(id).orElseThrow(() -> new VinculacionAcademicaNoEncontradaException("docente"));
    }

    private Estudiante estudianteActual(Authentication auth) {
        Long id = accessScope.idEstudiante(auth).orElseThrow(() -> new VinculacionAcademicaNoEncontradaException("estudiante"));
        return estudiantes.findById(id).orElseThrow(() -> new VinculacionAcademicaNoEncontradaException("estudiante"));
    }

    private Curso exigirCursoDocente(Authentication auth, Long cursoId) {
        Long docenteId = docenteActual(auth).getId();
        Curso curso = cursos.findById(cursoId).orElseThrow(() -> new com.umg.sgau.curso.exception.CursoNoEncontradoException(cursoId));
        if (curso.getDocente() == null || !docenteId.equals(curso.getDocente().getId()))
            throw new org.springframework.security.access.AccessDeniedException("El curso no esta asignado al docente autenticado.");
        return curso;
    }

    private Inscripcion inscripcionActual(Estudiante e) {
        return inscripciones.findByEstudiante_IdAndActivoTrueOrderByCicloAnioDescFechaInscripcionDesc(e.getId()).stream()
                .findFirst().orElseThrow(() -> new VinculacionAcademicaNoEncontradaException("carrera"));
    }

    private AcademicoDTOs.Curso curso(Curso c) {
        var carrera = c.getCarrera();
        return new AcademicoDTOs.Curso(c.getId(), c.getCodigo(), c.getNombre(), c.getDescripcion(), c.getCreditos(),
                c.getHorasSemanales(), c.getCicloAnio(), c.getActivo(), carrera == null ? null : carrera.getId(),
                carrera == null ? null : carrera.getCodigo(), carrera == null ? null : carrera.getNombre());
    }

    private AcademicoDTOs.Inscripcion inscripcion(Inscripcion i) {
        var e=i.getEstudiante(); var c=i.getCurso(); var r=i.getCarrera();
        return new AcademicoDTOs.Inscripcion(i.getId(), e.getId(), e.getCodigoEstudiantil(),
                e.getNombres()+" "+e.getApellidos(), c==null?null:c.getId(), c==null?null:c.getCodigo(),
                c==null?null:c.getNombre(), r.getId(), r.getCodigo(), r.getNombre(), i.getGrado(), i.getSeccion(),
                i.getCicloAnio(), i.getFechaInscripcion(), i.getEstado(), i.getObservaciones(), i.getActivo());
    }

    private AcademicoDTOs.Nota nota(Nota n) {
        var e=n.getEstudiante(); var c=n.getCurso();
        return new AcademicoDTOs.Nota(n.getId(), e.getId(), e.getCodigoEstudiantil(), e.getNombres()+" "+e.getApellidos(),
                c.getId(), c.getCodigo(), c.getNombre(), n.getCicloAnio(), n.getTipoEvaluacion(), n.getCalificacion(),
                n.getObservaciones(), n.getActivo());
    }

    private AcademicoDTOs.Colegiatura colegiatura(Colegiatura c) {
        return new AcademicoDTOs.Colegiatura(c.getId(), c.getCicloAnio(), c.getConcepto(), c.getMontoTotal(),
                c.getMontoPagado(), c.getSaldoPendiente(), c.getFechaEmision(), c.getFechaVencimiento(), c.getEstado(), c.getActivo());
    }
}
