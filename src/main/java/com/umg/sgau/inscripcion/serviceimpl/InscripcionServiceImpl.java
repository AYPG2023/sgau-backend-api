package com.umg.sgau.inscripcion.serviceimpl;

import com.umg.sgau.carrera.entity.Carrera;
import com.umg.sgau.carrera.exception.CarreraNoEncontradaException;
import com.umg.sgau.carrera.service.CarreraService;
import com.umg.sgau.curso.entity.Curso;
import com.umg.sgau.curso.exception.CursoNoEncontradoException;
import com.umg.sgau.curso.service.CursoService;
import com.umg.sgau.estudiante.entity.Estudiante;
import com.umg.sgau.estudiante.exception.EstudianteNoEncontradoException;
import com.umg.sgau.estudiante.service.EstudianteService;
import com.umg.sgau.inscripcion.entity.Inscripcion;
import com.umg.sgau.inscripcion.exception.CursoInactivoParaInscripcionException;
import com.umg.sgau.inscripcion.exception.CursoInvalidoParaInscripcionException;
import com.umg.sgau.inscripcion.exception.CursoNoPerteneceCarreraException;
import com.umg.sgau.inscripcion.exception.EstudianteInactivoParaInscripcionException;
import com.umg.sgau.inscripcion.exception.EstudianteInvalidoParaInscripcionException;
import com.umg.sgau.inscripcion.exception.InscripcionDuplicadaException;
import com.umg.sgau.inscripcion.exception.InscripcionNoEncontradaException;
import com.umg.sgau.inscripcion.repository.InscripcionRepository;
import com.umg.sgau.inscripcion.service.InscripcionService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import com.umg.sgau.notificacion.service.EventoNotificacion;
import java.time.LocalDateTime;

@Service
@Transactional
public class InscripcionServiceImpl implements InscripcionService {

    private final InscripcionRepository inscripcionRepository;
    private final EstudianteService estudianteService;
    private final CarreraService carreraService;
    private final CursoService cursoService;
    @Autowired private ApplicationEventPublisher events;

    @Autowired
    public InscripcionServiceImpl(
            InscripcionRepository inscripcionRepository,
            EstudianteService estudianteService,
            CarreraService carreraService,
            CursoService cursoService) {
        this.inscripcionRepository = inscripcionRepository;
        this.estudianteService = estudianteService;
        this.carreraService = carreraService;
        this.cursoService = cursoService;
    }

    public InscripcionServiceImpl(
            InscripcionRepository inscripcionRepository,
            EstudianteService estudianteService,
            CursoService cursoService) {
        this(inscripcionRepository, estudianteService, null, cursoService);
    }

    @Override
    public Inscripcion registrar(Inscripcion inscripcion, Long estudianteId, Long carreraId, Long cursoId) {
        asignarReferencias(inscripcion, estudianteId, carreraId, cursoId);
        validarReferenciasActivas(inscripcion);
        validarDuplicadoActivo(inscripcion, null);

        // El @PrePersist pone estado=ACTIVA y activo=true.
        Inscripcion guardada = inscripcionRepository.save(inscripcion);
        var student = guardada.getEstudiante(); var course = guardada.getCurso();
        if (events != null && student.getUsuario() != null) events.publishEvent(new EventoNotificacion(student.getUsuario().getId(),
                "INSCRIPCION:"+guardada.getId()+":"+LocalDateTime.now(), "INSCRIPCION", "Curso asignado",
                "Se confirmó tu inscripción a un curso.", "CURSO", course == null ? null : course.getId(), false));
        return guardada;
    }

    @Override
    @Transactional(readOnly = true)
    public Inscripcion obtenerPorId(Long id) {
        return inscripcionRepository.findById(id)
                .orElseThrow(() -> new InscripcionNoEncontradaException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Inscripcion> listarConFiltros(
            Long estudianteId, Long carreraId, Long cursoId,
            Integer cicloAnio, String grado, String seccion,
            String estado, Boolean activo, Pageable pageable) {

        return inscripcionRepository.buscarConFiltros(
                        estudianteId, carreraId, cursoId, cicloAnio,
                        grado, seccion, estado, activo, pageable);
    }

    @Override
    public Inscripcion actualizar(Long id, Inscripcion inscripcion, Long carreraId, Long cursoId) {

        Inscripcion existente = obtenerPorId(id);

        Long carreraIdActualizada = carreraId == null ? getCarreraId(existente) : carreraId;
        Long cursoIdActualizado = cursoId == null ? getCursoId(existente) : cursoId;
        asignarReferencias(existente, getEstudianteId(existente), carreraIdActualizada, cursoIdActualizado);
        existente.setGrado(inscripcion.getGrado());
        existente.setSeccion(inscripcion.getSeccion());
        existente.setCicloAnio(inscripcion.getCicloAnio());
        existente.setObservaciones(inscripcion.getObservaciones());

        validarReferenciasActivas(existente);
        validarDuplicadoActivo(existente, id);

        return inscripcionRepository.save(existente);
    }

    @Override
    public Inscripcion anular(Long id, String motivo) {

        Inscripcion inscripcion = obtenerPorId(id);

        // Regla de negocio: no se puede anular una inscripcion que ya esta anulada
        if ("ANULADA".equals(inscripcion.getEstado())) {
            throw new IllegalStateException(
                    "La inscripcion con ID " + id + " ya esta anulada.");
        }

        // Soft-delete: cambiar estado a ANULADA, activo a false
        inscripcion.setEstado("ANULADA");
        inscripcion.setActivo(false);
        if (motivo != null && !motivo.isBlank()) {
            inscripcion.setObservaciones(motivo);
        }

        return inscripcionRepository.save(inscripcion);
    }

    @Override
    public Inscripcion reactivar(Long id) {
        Inscripcion inscripcion = obtenerPorId(id);

        if (Boolean.TRUE.equals(inscripcion.getActivo())) {
            throw new IllegalStateException(
                    "La inscripcion con ID " + id + " ya esta activa.");
        }

        validarReferenciasActivas(inscripcion);
        validarDuplicadoActivo(inscripcion, id);

        inscripcion.setEstado("ACTIVA");
        inscripcion.setActivo(true);

        return inscripcionRepository.save(inscripcion);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Inscripcion> historialPorEstudiante(Long estudianteId, Pageable pageable) {
        validarEstudianteExistente(estudianteId);
        return inscripcionRepository.findByEstudianteId(estudianteId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Inscripcion> inscripcionesPorCurso(Long cursoId, Pageable pageable) {
        validarCursoExistente(cursoId);
        return inscripcionRepository.findByCursoId(cursoId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Inscripcion> inscripcionesActivasPorEstudiante(Long estudianteId, Pageable pageable) {
        validarEstudianteExistente(estudianteId);
        return inscripcionRepository.findByEstudianteIdAndActivoTrue(estudianteId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Inscripcion> inscripcionesActivasPorCurso(Long cursoId, Pageable pageable) {
        validarCursoExistente(cursoId);
        return inscripcionRepository.findByCursoIdAndActivoTrue(cursoId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Inscripcion> obtenerActivas() {
        return inscripcionRepository.findByActivoTrue()
                .stream()
                .filter(inscripcion -> Boolean.TRUE.equals(inscripcion.getActivo()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> obtenerEstudiantesConInscripcionActiva() {
        return obtenerActivas()
                .stream()
                .filter(inscripcion -> Boolean.TRUE.equals(inscripcion.getActivo()))
                .filter(inscripcion -> inscripcion.getEstudiante() != null)
                .map(inscripcion -> inscripcion.getEstudiante().getId())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeInscripcionActiva(Long estudianteId, Long cursoId, Integer cicloAnio) {
        if (estudianteId == null || cursoId == null || cicloAnio == null) {
            return false;
        }

        return inscripcionRepository.existsByEstudianteIdAndCursoIdAndCicloAnioAndActivoTrue(
                estudianteId, cursoId, cicloAnio);
    }

    private void asignarReferencias(Inscripcion inscripcion, Long estudianteId, Long carreraId, Long cursoId) {
        Estudiante estudiante = validarEstudianteExistente(estudianteId);
        if (!Boolean.TRUE.equals(estudiante.getActivo())) {
            throw new EstudianteInactivoParaInscripcionException(estudianteId);
        }
        inscripcion.setEstudiante(estudiante);
        inscripcion.setCarrera(validarCarreraExistente(carreraId));
        inscripcion.setCurso(cursoId == null ? null : validarCursoExistente(cursoId));
    }

    private void validarReferenciasActivas(Inscripcion inscripcion) {
        Long estudianteId = getEstudianteId(inscripcion);
        Estudiante estudiante = validarEstudianteExistente(estudianteId);
        if (!Boolean.TRUE.equals(estudiante.getActivo())) {
            throw new EstudianteInactivoParaInscripcionException(estudianteId);
        }
        inscripcion.setEstudiante(estudiante);

        Long cursoId = getCursoId(inscripcion);
        Curso curso = null;
        if (cursoId != null) {
            curso = validarCursoExistente(cursoId);
            inscripcion.setCurso(curso);
        }
        if (curso != null && !Boolean.TRUE.equals(curso.getActivo())) {
            throw new CursoInactivoParaInscripcionException(cursoId);
        }

        validarCursoPerteneceCarrera(inscripcion, curso);
    }

    private Estudiante validarEstudianteExistente(Long estudianteId) {
        if (estudianteId == null || estudianteId <= 0) {
            throw new EstudianteInvalidoParaInscripcionException(estudianteId);
        }

        try {
            return estudianteService.obtenerPorId(estudianteId);
        } catch (EstudianteNoEncontradoException exception) {
            throw new EstudianteInvalidoParaInscripcionException(estudianteId);
        }
    }

    private Curso validarCursoExistente(Long cursoId) {
        if (cursoId == null || cursoId <= 0) {
            throw new CursoInvalidoParaInscripcionException(cursoId);
        }

        try {
            return cursoService.obtenerPorId(cursoId);
        } catch (CursoNoEncontradoException exception) {
            throw new CursoInvalidoParaInscripcionException(cursoId);
        }
    }

    private Carrera validarCarreraExistente(Long carreraId) {
        if (carreraId == null || carreraId <= 0) {
            throw new CursoNoPerteneceCarreraException(null, carreraId);
        }

        if (carreraService == null) {
            return Carrera.builder().id(carreraId).activo(true).build();
        }

        try {
            return carreraService.obtenerPorId(carreraId);
        } catch (CarreraNoEncontradaException exception) {
            throw new CursoNoPerteneceCarreraException(null, carreraId);
        }
    }

    private void validarCursoPerteneceCarrera(Inscripcion inscripcion, Curso curso) {
        Long carreraId = getCarreraId(inscripcion);
        if (curso != null && curso.getCarrera() != null && !curso.getCarrera().getId().equals(carreraId)) {
            throw new CursoNoPerteneceCarreraException(getCursoId(inscripcion), carreraId);
        }
        if (curso != null && !curso.getCicloAnio().equals(inscripcion.getCicloAnio())) {
            throw new IllegalArgumentException("El ciclo de la inscripcion debe coincidir con el ciclo academico del curso.");
        }
    }

    private void validarDuplicadoActivo(Inscripcion inscripcion, Long idExcluir) {
        boolean duplicada = getCursoId(inscripcion) == null
                ? existeDuplicadoActivoSinCurso(inscripcion, idExcluir)
                : existeDuplicadoActivoConCurso(inscripcion, idExcluir);

        if (!duplicada) {
            return;
        }

        if (getCursoId(inscripcion) == null) {
            throw new InscripcionDuplicadaException(
                    getEstudianteId(inscripcion), getCarreraId(inscripcion), inscripcion.getGrado(),
                    inscripcion.getSeccion(), inscripcion.getCicloAnio());
        }

        throw new InscripcionDuplicadaException(
                getEstudianteId(inscripcion), getCursoId(inscripcion), inscripcion.getCicloAnio());
    }

    private boolean existeDuplicadoActivoConCurso(Inscripcion inscripcion, Long idExcluir) {
        if (idExcluir == null) {
            return inscripcionRepository.existsByEstudianteIdAndCursoIdAndCicloAnioAndActivoTrue(
                    getEstudianteId(inscripcion), getCursoId(inscripcion), inscripcion.getCicloAnio());
        }

        return inscripcionRepository.existsByEstudianteIdAndCursoIdAndCicloAnioAndActivoTrueAndIdNot(
                getEstudianteId(inscripcion), getCursoId(inscripcion), inscripcion.getCicloAnio(), idExcluir);
    }

    private boolean existeDuplicadoActivoSinCurso(Inscripcion inscripcion, Long idExcluir) {
        if (idExcluir == null) {
            return inscripcionRepository.existsByEstudianteIdAndCarreraIdAndGradoAndSeccionAndCicloAnioAndActivoTrue(
                    getEstudianteId(inscripcion), getCarreraId(inscripcion), inscripcion.getGrado(),
                    inscripcion.getSeccion(), inscripcion.getCicloAnio());
        }

        return inscripcionRepository.existsByEstudianteIdAndCarreraIdAndGradoAndSeccionAndCicloAnioAndActivoTrueAndIdNot(
                getEstudianteId(inscripcion), getCarreraId(inscripcion), inscripcion.getGrado(),
                inscripcion.getSeccion(), inscripcion.getCicloAnio(), idExcluir);
    }

    private Long getEstudianteId(Inscripcion inscripcion) {
        return inscripcion.getEstudiante() == null ? null : inscripcion.getEstudiante().getId();
    }

    private Long getCarreraId(Inscripcion inscripcion) {
        return inscripcion.getCarrera() == null ? null : inscripcion.getCarrera().getId();
    }

    private Long getCursoId(Inscripcion inscripcion) {
        return inscripcion.getCurso() == null ? null : inscripcion.getCurso().getId();
    }
}
