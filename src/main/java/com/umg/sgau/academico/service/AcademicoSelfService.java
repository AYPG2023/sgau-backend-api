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
import org.springframework.beans.factory.annotation.Autowired;
import com.umg.sgau.academico.CicloAcademicoRepository;
import com.umg.sgau.academico.GradoAcademicoRepository;
import com.umg.sgau.academico.SeccionAcademicaRepository;
import com.umg.sgau.academico.MatriculaCarrera;
import com.umg.sgau.academico.MatriculaCarreraRepository;
import org.springframework.context.ApplicationEventPublisher;
import com.umg.sgau.notificacion.service.EventoNotificacion;
import java.time.LocalDate;
import java.time.YearMonth;

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
    private final CicloAcademicoRepository ciclos;
    private final GradoAcademicoRepository grados;
    private final SeccionAcademicaRepository secciones;
    private final ApplicationEventPublisher events;
    private final MatriculaCarreraRepository matriculas;

    @Autowired
    public AcademicoSelfService(AccessScopeService accessScope, DocenteRepository docentes,
            EstudianteRepository estudiantes, CursoRepository cursos, CarreraRepository carreras,
            InscripcionRepository inscripciones, NotaRepository notas,
            ColegiaturaRepository colegiaturas, CicloAcademicoRepository ciclos,
            GradoAcademicoRepository grados, SeccionAcademicaRepository secciones, ApplicationEventPublisher events,
            MatriculaCarreraRepository matriculas) {
        this.accessScope = accessScope;
        this.docentes = docentes;
        this.estudiantes = estudiantes;
        this.cursos = cursos;
        this.carreras = carreras;
        this.inscripciones = inscripciones;
        this.notas = notas;
        this.colegiaturas = colegiaturas;
        this.ciclos=ciclos; this.grados=grados; this.secciones=secciones; this.events=events;
        this.matriculas=matriculas;
    }

    public AcademicoSelfService(AccessScopeService accessScope, DocenteRepository docentes,
            EstudianteRepository estudiantes, CursoRepository cursos, CarreraRepository carreras,
            InscripcionRepository inscripciones, NotaRepository notas, ColegiaturaRepository colegiaturas) {
        this(accessScope,docentes,estudiantes,cursos,carreras,inscripciones,notas,colegiaturas,null,null,null,null,null);
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
        MatriculaCarrera i = matriculaActual(estudianteActual(auth));
        var c = i.getCarrera();
        return new AcademicoDTOs.Carrera(c.getId(), c.getCodigo(), c.getNombre(), c.getDescripcion(), c.getDuracionAnios(), i.getCicloAnio());
    }

    public AcademicoDTOs.PlanCarrera planCarrera(Authentication auth) {
        Estudiante e = estudianteActual(auth);
        MatriculaCarrera actual = matriculaActual(e);
        var carrera = actual.getCarrera();
        List<Inscripcion> activas = inscripciones.findByEstudiante_IdAndActivoTrueOrderByCicloAnioDescFechaInscripcionDesc(e.getId());
        var cursosInscritos = activas.stream().filter(i -> i.getCurso() != null)
                .filter(i -> actual.getId()==null || (i.getMatriculaCarrera()!=null && actual.getId().equals(i.getMatriculaCarrera().getId())))
                .collect(java.util.stream.Collectors.toMap(i -> i.getCurso().getId(), Inscripcion::getCurso, (a,b)->a, LinkedHashMap::new));
        List<Curso> plan = cursos.findByCarrera_IdAndActivoTrueOrderByCicloAnioDescNombreAsc(carrera.getId()).stream()
                .filter(c -> actual.getCiclo()!=null
                        ? c.getCiclo()!=null && c.getCiclo().getId().equals(actual.getCiclo().getId())
                        : c.getCiclo()==null && java.util.Objects.equals(c.getCicloAnio(),actual.getCicloAnio()))
                .toList();
        List<AcademicoDTOs.CursoPlan> detalle = plan.stream().map(c -> {
            var d=c.getDocente();
            return new AcademicoDTOs.CursoPlan(c.getId(),c.getCodigo(),c.getNombre(),c.getDescripcion(),c.getCreditos(),
                    c.getHorasSemanales(),c.getCicloAnio(),c.getCiclo()==null?null:c.getCiclo().getId(),c.getCiclo()==null?null:c.getCiclo().getNombre(),cursosInscritos.containsKey(c.getId()),d==null?null:d.getId(),
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

    public List<AcademicoDTOs.CarreraDisponible> carrerasDisponibles(Authentication auth) {
        Long id=estudianteActual(auth).getId();
        if (matriculas.existsByEstudiante_IdAndActivoTrue(id)||inscripciones.existsByEstudiante_IdAndActivoTrue(id)) return List.of();
        return carreras.findAll().stream().filter(c->Boolean.TRUE.equals(c.getActivo())).filter(c->configuracionCompleta(c))
                .map(c->new AcademicoDTOs.CarreraDisponible(c.getId(),c.getCodigo(),c.getNombre(),c.getDescripcion(),c.getDuracionAnios(),c.getMensualidad(),c.getCantidadCuotas(),c.getDiaVencimiento())).toList();
    }

    @Transactional
    public AcademicoDTOs.ResultadoInscripcion inscribirse(Authentication auth, AcademicoDTOs.SolicitudInscripcion req) {
        Estudiante e=estudianteActual(auth);
        if(!Boolean.TRUE.equals(e.getActivo())) throw new IllegalArgumentException("El perfil del estudiante está inactivo.");
        if(matriculas.existsByEstudiante_IdAndActivoTrue(e.getId())||inscripciones.existsByEstudiante_IdAndActivoTrue(e.getId())) throw new IllegalStateException("Ya tienes una inscripción académica vigente. No se reemplaza automáticamente.");
        var carrera=carreras.findById(req.carreraId()).orElseThrow(()->new IllegalArgumentException("Carrera no encontrada."));
        var ciclo=ciclos.findById(req.cicloId()).orElseThrow(()->new IllegalArgumentException("Ciclo académico no encontrado."));
        var grado=grados.findById(req.gradoId()).orElseThrow(()->new IllegalArgumentException("Grado no encontrado."));
        var seccion=secciones.findById(req.seccionId()).orElseThrow(()->new IllegalArgumentException("Sección no encontrada."));
        if(!Boolean.TRUE.equals(carrera.getActivo())||!configuracionCompleta(carrera)) throw new IllegalArgumentException("Carrera inactiva o con mensualidad sin configurar.");
        if(!Boolean.TRUE.equals(ciclo.getActivo())||LocalDate.now().isBefore(ciclo.getFechaInicio())||LocalDate.now().isAfter(ciclo.getFechaFin())) throw new IllegalArgumentException("El ciclo no está activo o fuera de fechas de inscripción.");
        if(!Boolean.TRUE.equals(grado.getActivo())||!Boolean.TRUE.equals(seccion.getActivo())||!seccion.getGrado().getId().equals(grado.getId())) throw new IllegalArgumentException("Combinación de grado y sección inválida.");
        MatriculaCarrera i=matriculas.saveAndFlush(MatriculaCarrera.builder().estudiante(e).carrera(carrera).ciclo(ciclo).gradoAcademico(grado).seccionAcademica(seccion)
                .fechaInscripcion(LocalDate.now()).estado("ACTIVA").activo(true).build());
        BigDecimal amount=carrera.getMensualidad().setScale(2,RoundingMode.HALF_UP);
        for(int n=1;n<=carrera.getCantidadCuotas();n++) {
            LocalDate issue=ciclo.getFechaInicio().plusMonths(n-1); YearMonth ym=YearMonth.from(issue);
            LocalDate due=ym.atDay(Math.min(carrera.getDiaVencimiento(),ym.lengthOfMonth()));
            if(issue.isAfter(ciclo.getFechaFin())) throw new IllegalArgumentException("La cantidad de cuotas excede la duración del ciclo académico.");
            if(due.isBefore(issue)) due=issue;
            Colegiatura c=Colegiatura.builder().estudiante(e).inscripcionCarrera(i).ciclo(ciclo).numeroCuota(n).cicloAnio(ciclo.getAnio())
                    .concepto("COLEGIATURA " + carrera.getCodigo()+" "+ciclo.getNombre()+" - CUOTA "+n).montoTotal(amount).montoPagado(BigDecimal.ZERO.setScale(2))
                    .saldoPendiente(amount).fechaEmision(issue).fechaVencimiento(due).estado("PENDIENTE").activo(true).build();
            colegiaturas.save(c);
            if(e.getUsuario()!=null) events.publishEvent(new EventoNotificacion(e.getUsuario().getId(),"COLEGIATURA:"+i.getId()+":"+n,"COLEGIATURA","Nueva colegiatura","Se generó una colegiatura en tu cuenta.","COLEGIATURA",c.getId(),false));
        }
        if(e.getUsuario()!=null) events.publishEvent(new EventoNotificacion(e.getUsuario().getId(),"INSCRIPCION_CARRERA:"+i.getId(),"INSCRIPCION","Inscripción a carrera","Se confirmó tu inscripción a la carrera.","CARRERA",carrera.getId(),false));
        return new AcademicoDTOs.ResultadoInscripcion(i.getId(),carrera.getId(),carrera.getNombre(),ciclo.getAnio(),ciclo.getNombre(),amount,carrera.getCantidadCuotas(),carrera.getDiaVencimiento());
    }

    public List<AcademicoDTOs.Ciclo> ciclosDisponibles(Authentication auth) { LocalDate hoy=LocalDate.now(); return ciclos.findByActivoTrueOrderByAnioDescFechaInicioDesc().stream().filter(c->!hoy.isBefore(c.getFechaInicio())&&!hoy.isAfter(c.getFechaFin())).map(c->new AcademicoDTOs.Ciclo(c.getId(),c.getNombre(),c.getAnio(),c.getFechaInicio(),c.getFechaFin(),c.getActivo())).toList(); }
    public List<AcademicoDTOs.Grado> gradosDisponibles(Authentication auth) { return grados.findByActivoTrueOrderByNombreAsc().stream().map(g->new AcademicoDTOs.Grado(g.getId(),g.getCodigo(),g.getNombre())).toList(); }
    public List<AcademicoDTOs.Seccion> seccionesDisponibles(Authentication auth,Long gradoId) { return secciones.findByGrado_IdAndActivoTrueOrderByNombreAsc(gradoId).stream().map(s->new AcademicoDTOs.Seccion(s.getId(),s.getCodigo(),s.getNombre(),s.getGrado().getId())).toList(); }

    @Transactional
    public AcademicoDTOs.Inscripcion asignarCurso(Authentication auth,Long cursoId) {
        Estudiante e=estudianteActual(auth); MatriculaCarrera carrera=matriculaActual(e);
        if(!Boolean.TRUE.equals(e.getActivo())) throw new IllegalArgumentException("El perfil del estudiante está inactivo.");
        if(carrera.getId()==null||carrera.getCiclo()==null||carrera.getGradoAcademico()==null||carrera.getSeccionAcademica()==null) throw new IllegalArgumentException("La matrícula histórica debe asociarse a un ciclo, grado y sección antes de asignar cursos.");
        LocalDate today=LocalDate.now();
        if(!Boolean.TRUE.equals(carrera.getActivo())||!Boolean.TRUE.equals(carrera.getCiclo().getActivo())||today.isBefore(carrera.getCiclo().getFechaInicio())||today.isAfter(carrera.getCiclo().getFechaFin())) throw new IllegalArgumentException("La matrícula/ciclo no está vigente para asignar cursos.");
        Curso curso=cursos.findById(cursoId).orElseThrow(()->new IllegalArgumentException("Curso no encontrado."));
        if(!Boolean.TRUE.equals(curso.getActivo())||!Boolean.TRUE.equals(carrera.getCarrera().getActivo())||!curso.getCarrera().getId().equals(carrera.getCarrera().getId())) throw new IllegalArgumentException("Curso inactivo o de otra carrera.");
        if(curso.getCiclo()==null||carrera.getCiclo()==null||!curso.getCiclo().getId().equals(carrera.getCiclo().getId())) throw new IllegalArgumentException("El curso debe estar vinculado al mismo ciclo académico de la inscripción.");
        boolean duplicada=curso.getCiclo()!=null
                ? inscripciones.existsByEstudiante_IdAndCurso_IdAndCiclo_IdAndActivoTrue(e.getId(),cursoId,curso.getCiclo().getId())
                : inscripciones.existsByEstudiante_IdAndCurso_IdAndCicloAnioAndActivoTrue(e.getId(),cursoId,carrera.getCicloAnio());
        if(duplicada) throw new IllegalStateException("Ya estás asignado a este curso en el período.");
        Inscripcion i=Inscripcion.builder().estudiante(e).carrera(carrera.getCarrera()).curso(curso).matriculaCarrera(carrera).ciclo(carrera.getCiclo()).grado(carrera.getGrado()).seccion(carrera.getSeccion()).gradoCatalogo(carrera.getGradoAcademico()).seccionCatalogo(carrera.getSeccionAcademica()).cicloAnio(carrera.getCicloAnio()).fechaInscripcion(LocalDate.now()).estado("ACTIVA").activo(true).build();
        i=inscripciones.save(i);
        if(e.getUsuario()!=null) events.publishEvent(new EventoNotificacion(e.getUsuario().getId(),"ASIGNACION_CURSO_ESTUDIANTE:"+i.getId(),"INSCRIPCION","Asignación a curso","Te asignaste a un curso.","CURSO",curso.getId(),false));
        return inscripcion(i);
    }

    private boolean configuracionCompleta(com.umg.sgau.carrera.entity.Carrera c) { return c.getMensualidad()!=null&&c.getMensualidad().signum()>0&&c.getCantidadCuotas()!=null&&c.getCantidadCuotas()>0&&c.getDiaVencimiento()!=null&&c.getDiaVencimiento()>=1&&c.getDiaVencimiento()<=31; }

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

    private MatriculaCarrera matriculaActual(Estudiante e) {
        if(matriculas!=null){var actual=matriculas.findFirstByEstudiante_IdAndActivoTrueOrderByFechaInscripcionDesc(e.getId());if(actual.isPresent())return actual.get();}
        Inscripcion legacy=inscripciones.findByEstudiante_IdAndActivoTrueOrderByCicloAnioDescFechaInscripcionDesc(e.getId()).stream().filter(i->i.getCurso()!=null).findFirst().orElseThrow(()->new VinculacionAcademicaNoEncontradaException("carrera"));
        MatriculaCarrera vista=MatriculaCarrera.builder().estudiante(e).carrera(legacy.getCarrera()).ciclo(legacy.getCiclo()).gradoAcademico(legacy.getGradoCatalogo()).seccionAcademica(legacy.getSeccionCatalogo()).build();
        vista.setCicloAnioHistorico(legacy.getCicloAnio());vista.setGradoHistorico(legacy.getGrado());vista.setSeccionHistorica(legacy.getSeccion());return vista;
    }

    private AcademicoDTOs.Curso curso(Curso c) {
        var carrera = c.getCarrera();
        return new AcademicoDTOs.Curso(c.getId(), c.getCodigo(), c.getNombre(), c.getDescripcion(), c.getCreditos(),
                c.getHorasSemanales(), c.getCicloAnio(), c.getActivo(), carrera == null ? null : carrera.getId(),
                carrera == null ? null : carrera.getCodigo(), carrera == null ? null : carrera.getNombre(),c.getCiclo()==null?null:c.getCiclo().getId(),c.getCiclo()==null?null:c.getCiclo().getNombre());
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
        return new AcademicoDTOs.Colegiatura(c.getId(), c.getCicloAnio(), c.getCiclo()==null?null:c.getCiclo().getNombre(), c.getConcepto(), c.getMontoTotal(),
                c.getMontoPagado(), c.getSaldoPendiente(), c.getFechaEmision(), c.getFechaVencimiento(), c.getEstado(), c.getActivo());
    }
}
