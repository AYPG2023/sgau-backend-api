package com.umg.sgau.curso.serviceimpl;

import com.umg.sgau.carrera.entity.Carrera;
import com.umg.sgau.carrera.exception.CarreraNoEncontradaException;
import com.umg.sgau.carrera.service.CarreraService;
import com.umg.sgau.curso.exception.CarreraInactivaParaCursoException;
import com.umg.sgau.curso.entity.Curso;
import com.umg.sgau.curso.exception.CarreraCursoInvalidaException;
import com.umg.sgau.curso.exception.CodigoCursoDuplicadoException;
import com.umg.sgau.curso.exception.CursoAcademicoDuplicadoException;
import com.umg.sgau.curso.exception.CursoInactivoException;
import com.umg.sgau.curso.exception.CursoNoEncontradoException;
import com.umg.sgau.curso.exception.CursoSinDocenteException;
import com.umg.sgau.curso.exception.DocenteInactivoParaCursoException;
import com.umg.sgau.curso.exception.DocenteCursoInvalidoException;
import com.umg.sgau.curso.repository.CursoRepository;
import com.umg.sgau.curso.service.CursoService;
import com.umg.sgau.docente.entity.Docente;
import com.umg.sgau.docente.exception.DocenteNoEncontradoException;
import com.umg.sgau.docente.service.DocenteService;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import com.umg.sgau.notificacion.service.EventoNotificacion;

@Service
public class CursoServiceImpl implements CursoService {
    @Autowired private ApplicationEventPublisher events;

    private final CursoRepository cursoRepository;
    private final CarreraService carreraService;
    private final DocenteService docenteService;

    public CursoServiceImpl(
            CursoRepository cursoRepository,
            CarreraService carreraService,
            DocenteService docenteService) {
        this.cursoRepository = cursoRepository;
        this.carreraService = carreraService;
        this.docenteService = docenteService;
    }

    @Override
    public Curso crear(Curso curso, Long carreraId) {
        String codigoNormalizado = normalizarCodigo(curso.getCodigo());
        String nombreNormalizado = normalizarNombre(curso.getNombre());
        String descripcionNormalizada = normalizarDescripcion(curso.getDescripcion());

        Carrera carrera = validarCarreraActiva(carreraId);
        validarCodigoDisponible(codigoNormalizado);
        validarCursoAcademicoDisponible(
                nombreNormalizado,
                carreraId,
                curso.getCicloAnio());

        curso.setCodigo(codigoNormalizado);
        curso.setNombre(nombreNormalizado);
        curso.setDescripcion(descripcionNormalizada);
        curso.setCarrera(carrera);
        curso.setDocente(null);

        if (curso.getActivo() == null) {
            curso.setActivo(true);
        }

        return cursoRepository.save(curso);
    }

    @Override
    public Curso obtenerPorId(Long id) {
        return cursoRepository.findById(id)
                .orElseThrow(() -> new CursoNoEncontradoException(id));
    }

    @Override
    public Page<Curso> listar(
            String texto,
            Long carreraId,
            Long docenteId,
            Integer cicloAnio,
            Boolean activo,
            Pageable pageable) {
        String textoNormalizado = texto == null ? null : texto.trim();

        return cursoRepository.buscarConFiltros(
                textoNormalizado,
                carreraId,
                docenteId,
                cicloAnio,
                activo,
                pageable);
    }

    @Override
    public Curso actualizar(Long id, Curso curso, Long carreraId) {
        Curso existente = obtenerPorId(id);
        String codigoNormalizado = normalizarCodigo(curso.getCodigo());
        String nombreNormalizado = normalizarNombre(curso.getNombre());
        String descripcionNormalizada = normalizarDescripcion(curso.getDescripcion());

        Carrera carrera = validarCarreraActiva(carreraId);
        validarCodigoDisponibleParaActualizar(codigoNormalizado, id);
        validarCursoAcademicoDisponibleParaActualizar(
                nombreNormalizado,
                carreraId,
                curso.getCicloAnio(),
                id);

        existente.setCodigo(codigoNormalizado);
        existente.setNombre(nombreNormalizado);
        existente.setDescripcion(descripcionNormalizada);
        existente.setCreditos(curso.getCreditos());
        existente.setHorasSemanales(curso.getHorasSemanales());
        existente.setCarrera(carrera);
        existente.setCicloAnio(curso.getCicloAnio());

        return cursoRepository.save(existente);
    }

    @Override
    public Curso cambiarEstado(Long id, Boolean activo) {
        Curso curso = obtenerPorId(id);
        curso.setActivo(activo);
        return cursoRepository.save(curso);
    }

    @Override
    @Transactional
    public Curso asignarDocente(Long id, Long docenteId) {
        Curso curso = obtenerPorId(id);

        validarCursoActivo(curso);
        validarDocenteIdRequerido(docenteId);
        Docente docente = validarDocenteActivo(docenteId);

        curso.setDocente(docente);

        Curso guardado=cursoRepository.save(curso);
        if(events!=null&&docente.getUsuario()!=null)events.publishEvent(new EventoNotificacion(docente.getUsuario().getId(),"ASIGNACION_CURSO:"+id+":"+System.nanoTime(),"CURSO","Curso asignado","Se actualizó tu asignación docente.","CURSO",id,false));
        return guardado;
    }

    @Override
    public Curso retirarDocente(Long id) {
        Curso curso = obtenerPorId(id);
        curso.setDocente(null);
        return cursoRepository.save(curso);
    }

    @Override
    public Docente obtenerDocenteAsignado(Long id) {
        Curso curso = obtenerPorId(id);

        if (curso.getDocente() == null) {
            throw new CursoSinDocenteException(id);
        }

        return obtenerDocenteExistente(curso.getDocenteId());
    }

    @Override
    public Page<Curso> obtenerCursosActivosPorCarrera(Long carreraId, Pageable pageable) {
        validarCarreraExistente(carreraId);

        List<Curso> cursosActivos = cursoRepository.findByCarreraId(carreraId, pageable)
                .getContent()
                .stream()
                .filter(curso -> Boolean.TRUE.equals(curso.getActivo()))
                .collect(Collectors.toList());

        return new PageImpl<>(cursosActivos, pageable, cursosActivos.size());
    }

    @Override
    public List<Curso> obtenerCursosActivos() {
        return cursoRepository.findAll()
                .stream()
                .filter(curso -> Boolean.TRUE.equals(curso.getActivo()))
                .collect(Collectors.toList());
    }

    @Override
    public List<String> obtenerNombresDeCursosActivos() {
        return cursoRepository.findAll()
                .stream()
                .filter(curso -> Boolean.TRUE.equals(curso.getActivo()))
                .map(Curso::getNombre)
                .collect(Collectors.toList());
    }

    @Override
    public List<Curso> obtenerCursosPorDocente(Long docenteId) {
        validarDocenteIdRequerido(docenteId);
        obtenerDocenteExistente(docenteId);

        return cursoRepository.findByDocenteId(docenteId)
                .stream()
                .filter(curso -> Boolean.TRUE.equals(curso.getActivo()))
                .filter(curso -> curso.getDocente() != null && docenteId.equals(curso.getDocente().getId()))
                .collect(Collectors.toList());
    }

    private String normalizarCodigo(String codigo) {
        return codigo.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizarNombre(String nombre) {
        return nombre.trim();
    }

    private String normalizarDescripcion(String descripcion) {
        return descripcion == null ? "" : descripcion.trim();
    }

    private void validarCarreraId(Long carreraId) {
        if (carreraId == null || carreraId <= 0) {
            throw new CarreraCursoInvalidaException(carreraId);
        }
    }

    private Carrera validarCarreraExistente(Long carreraId) {
        validarCarreraId(carreraId);
        try {
            return carreraService.obtenerPorId(carreraId);
        } catch (CarreraNoEncontradaException exception) {
            throw new CarreraCursoInvalidaException(carreraId);
        }
    }

    private Carrera validarCarreraActiva(Long carreraId) {
        Carrera carrera = validarCarreraExistente(carreraId);
        if (!Boolean.TRUE.equals(carrera.getActivo())) {
            throw new CarreraInactivaParaCursoException(carreraId);
        }
        return carrera;
    }

    private void validarDocenteIdRequerido(Long docenteId) {
        if (docenteId == null || docenteId <= 0) {
            throw new DocenteCursoInvalidoException(docenteId);
        }
    }

    private Docente obtenerDocenteExistente(Long docenteId) {
        validarDocenteIdRequerido(docenteId);
        try {
            return docenteService.obtenerPorId(docenteId);
        } catch (DocenteNoEncontradoException exception) {
            throw new DocenteCursoInvalidoException(docenteId);
        }
    }

    private Docente validarDocenteActivo(Long docenteId) {
        Docente docente = obtenerDocenteExistente(docenteId);
        if (!Boolean.TRUE.equals(docente.getActivo())) {
            throw new DocenteInactivoParaCursoException(docenteId);
        }
        return docente;
    }

    private void validarCursoActivo(Curso curso) {
        if (!Boolean.TRUE.equals(curso.getActivo())) {
            throw new CursoInactivoException(curso.getId());
        }
    }

    private void validarCodigoDisponible(String codigo) {
        if (cursoRepository.existsByCodigo(codigo)) {
            throw new CodigoCursoDuplicadoException(codigo);
        }
    }

    private void validarCodigoDisponibleParaActualizar(String codigo, Long id) {
        if (cursoRepository.existsByCodigoAndIdNot(codigo, id)) {
            throw new CodigoCursoDuplicadoException(codigo);
        }
    }

    private void validarCursoAcademicoDisponible(
            String nombre,
            Long carreraId,
            Integer cicloAnio) {
        if (cursoRepository.existsByNombreIgnoreCaseAndCarreraIdAndCicloAnioAndActivoTrue(
                nombre,
                carreraId,
                cicloAnio)) {
            throw new CursoAcademicoDuplicadoException(nombre, carreraId, cicloAnio);
        }
    }

    private void validarCursoAcademicoDisponibleParaActualizar(
            String nombre,
            Long carreraId,
            Integer cicloAnio,
            Long id) {
        if (cursoRepository.existsByNombreIgnoreCaseAndCarreraIdAndCicloAnioAndActivoTrueAndIdNot(
                nombre,
                carreraId,
                cicloAnio,
                id)) {
            throw new CursoAcademicoDuplicadoException(nombre, carreraId, cicloAnio);
        }
    }
}
