package com.umg.sgau.historialacademico.serviceimpl;

import com.umg.sgau.curso.entity.Curso;
import com.umg.sgau.curso.exception.CursoNoEncontradoException;
import com.umg.sgau.curso.service.CursoService;
import com.umg.sgau.estudiante.entity.Estudiante;
import com.umg.sgau.estudiante.service.EstudianteService;
import com.umg.sgau.historialacademico.dto.HistorialAcademicoResponseDTO;
import com.umg.sgau.historialacademico.dto.HistorialCursoResponseDTO;
import com.umg.sgau.historialacademico.dto.HistorialNotaResponseDTO;
import com.umg.sgau.historialacademico.exception.DatosHistorialInconsistentesException;
import com.umg.sgau.historialacademico.service.HistorialAcademicoService;
import com.umg.sgau.inscripcion.entity.Inscripcion;
import com.umg.sgau.inscripcion.service.InscripcionService;
import com.umg.sgau.nota.entity.Nota;
import com.umg.sgau.nota.service.NotaService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class HistorialAcademicoServiceImpl implements HistorialAcademicoService {

    private static final BigDecimal CERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private static final BigDecimal NOTA_MINIMA_APROBACION = new BigDecimal("61.00");
    private static final String RESULTADO_APROBADO = "APROBADO";
    private static final String RESULTADO_REPROBADO = "REPROBADO";
    private static final String RESULTADO_SIN_CALIFICACION = "SIN_CALIFICACION";
    private static final String RESULTADO_EN_CURSO = "EN_CURSO";

    private final EstudianteService estudianteService;
    private final InscripcionService inscripcionService;
    private final CursoService cursoService;
    private final NotaService notaService;

    public HistorialAcademicoServiceImpl(
            EstudianteService estudianteService,
            InscripcionService inscripcionService,
            CursoService cursoService,
            NotaService notaService) {
        this.estudianteService = estudianteService;
        this.inscripcionService = inscripcionService;
        this.cursoService = cursoService;
        this.notaService = notaService;
    }

    @Override
    public HistorialAcademicoResponseDTO generarHistorial(Long estudianteId) {
        return generarHistorial(estudianteId, null);
    }

    @Override
    public HistorialAcademicoResponseDTO generarHistorialPorCiclo(Long estudianteId, Integer cicloAnio) {
        if (cicloAnio == null || cicloAnio < 2020 || cicloAnio > 2100) {
            throw new IllegalArgumentException("El ciclo academico debe estar entre 2020 y 2100.");
        }
        return generarHistorial(estudianteId, cicloAnio);
    }

    private HistorialAcademicoResponseDTO generarHistorial(Long estudianteId, Integer cicloAnio) {
        validarEstudianteId(estudianteId);
        Estudiante estudiante = estudianteService.obtenerPorId(estudianteId);

        List<Inscripcion> inscripciones = obtenerInscripciones(estudianteId, cicloAnio);
        List<Nota> notasActivas = obtenerNotasActivas(estudianteId, cicloAnio);
        Map<CursoCicloKey, List<Nota>> notasPorCursoYCiclo = agruparNotasPorCursoYCiclo(notasActivas);
        Map<Long, Curso> cursosPorId = cargarCursos(inscripciones);

        List<HistorialCursoResponseDTO> detalleCursos = inscripciones.stream()
                .filter(inscripcion -> inscripcion.getCursoId() != null)
                .collect(Collectors.toMap(
                        inscripcion -> new CursoCicloKey(inscripcion.getCursoId(), inscripcion.getCicloAnio()),
                        Function.identity(),
                        this::seleccionarInscripcionRepresentativa,
                        LinkedHashMap::new))
                .values()
                .stream()
                .map(inscripcion -> construirDetalleCurso(
                        inscripcion,
                        cursosPorId.get(inscripcion.getCursoId()),
                        notasPorCursoYCiclo.getOrDefault(
                                new CursoCicloKey(inscripcion.getCursoId(), inscripcion.getCicloAnio()),
                                List.of())))
                .sorted(Comparator
                        .comparing(HistorialCursoResponseDTO::getCicloAnio, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(HistorialCursoResponseDTO::getNombreCurso, Comparator.nullsLast(String::compareTo)))
                .collect(Collectors.toList());

        return construirRespuesta(estudiante, detalleCursos);
    }

    private List<Inscripcion> obtenerInscripciones(Long estudianteId, Integer cicloAnio) {
        return inscripcionService.historialPorEstudiante(estudianteId, Pageable.unpaged())
                .getContent()
                .stream()
                .filter(inscripcion -> cicloAnio == null || cicloAnio.equals(inscripcion.getCicloAnio()))
                .collect(Collectors.toList());
    }

    private List<Nota> obtenerNotasActivas(Long estudianteId, Integer cicloAnio) {
        return notaService.obtenerNotasActivasPorEstudiante(estudianteId)
                .stream()
                .filter(nota -> Boolean.TRUE.equals(nota.getActivo()))
                .filter(nota -> cicloAnio == null || cicloAnio.equals(nota.getCicloAnio()))
                .collect(Collectors.toList());
    }

    private Map<CursoCicloKey, List<Nota>> agruparNotasPorCursoYCiclo(List<Nota> notasActivas) {
        return notasActivas.stream()
                .filter(nota -> nota.getCursoId() != null)
                .filter(nota -> nota.getCicloAnio() != null)
                .collect(Collectors.groupingBy(nota -> new CursoCicloKey(nota.getCursoId(), nota.getCicloAnio())));
    }

    private Map<Long, Curso> cargarCursos(List<Inscripcion> inscripciones) {
        Map<Long, Curso> cursos = new HashMap<>();

        inscripciones.stream()
                .map(Inscripcion::getCursoId)
                .filter(Objects::nonNull)
                .distinct()
                .forEach(cursoId -> cursos.put(cursoId, obtenerCurso(cursoId)));

        return cursos;
    }

    private Curso obtenerCurso(Long cursoId) {
        try {
            return cursoService.obtenerPorId(cursoId);
        } catch (CursoNoEncontradoException exception) {
            throw new DatosHistorialInconsistentesException(
                    "La inscripcion referencia un curso inexistente: " + cursoId);
        }
    }

    private HistorialCursoResponseDTO construirDetalleCurso(
            Inscripcion inscripcion,
            Curso curso,
            List<Nota> notasActivas) {
        BigDecimal promedioCurso = calcularPromedio(notasActivas);
        String resultado = determinarResultado(inscripcion, notasActivas, promedioCurso);

        return HistorialCursoResponseDTO.builder()
                .inscripcionId(inscripcion.getId())
                .cursoId(inscripcion.getCursoId())
                .codigoCurso(curso.getCodigo())
                .nombreCurso(curso.getNombre())
                .carreraId(curso.getCarreraId())
                .cicloAnio(inscripcion.getCicloAnio())
                .grado(inscripcion.getGrado())
                .seccion(inscripcion.getSeccion())
                .estadoInscripcion(inscripcion.getEstado())
                .inscripcionActiva(inscripcion.getActivo())
                .cursoActivo(curso.getActivo())
                .promedioCurso(promedioCurso)
                .resultado(resultado)
                .notas(toNotasDTO(notasActivas))
                .build();
    }

    private List<HistorialNotaResponseDTO> toNotasDTO(List<Nota> notas) {
        return notas.stream()
                .sorted(Comparator.comparing(Nota::getTipoEvaluacion, Comparator.nullsLast(String::compareTo)))
                .map(nota -> HistorialNotaResponseDTO.builder()
                        .notaId(nota.getId())
                        .tipoEvaluacion(nota.getTipoEvaluacion())
                        .calificacion(nota.getCalificacion())
                        .observaciones(nota.getObservaciones())
                        .activo(nota.getActivo())
                        .build())
                .collect(Collectors.toList());
    }

    private BigDecimal calcularPromedio(List<Nota> notasActivas) {
        double promedio = notasActivas.stream()
                .filter(nota -> nota.getCalificacion() != null)
                .map(Nota::getCalificacion)
                .map(BigDecimal::doubleValue)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.00);

        return BigDecimal.valueOf(promedio).setScale(2, RoundingMode.HALF_UP);
    }

    private String determinarResultado(Inscripcion inscripcion, List<Nota> notasActivas, BigDecimal promedioCurso) {
        if (notasActivas.isEmpty()) {
            return Boolean.TRUE.equals(inscripcion.getActivo()) ? RESULTADO_EN_CURSO : RESULTADO_SIN_CALIFICACION;
        }

        return promedioCurso.compareTo(NOTA_MINIMA_APROBACION) >= 0
                ? RESULTADO_APROBADO
                : RESULTADO_REPROBADO;
    }

    private HistorialAcademicoResponseDTO construirRespuesta(
            Estudiante estudiante,
            List<HistorialCursoResponseDTO> detalleCursos) {
        BigDecimal promedioGeneral = calcularPromedioGeneral(detalleCursos);

        long aprobados = detalleCursos.stream()
                .filter(curso -> RESULTADO_APROBADO.equals(curso.getResultado()))
                .count();
        long reprobados = detalleCursos.stream()
                .filter(curso -> RESULTADO_REPROBADO.equals(curso.getResultado()))
                .count();
        long sinCalificacion = detalleCursos.stream()
                .filter(curso -> RESULTADO_SIN_CALIFICACION.equals(curso.getResultado()))
                .count();

        return HistorialAcademicoResponseDTO.builder()
                .estudianteId(estudiante.getId())
                .nombreCompleto(estudiante.getNombres() + " " + estudiante.getApellidos())
                .estudianteActivo(estudiante.getActivo())
                .promedioGeneral(promedioGeneral)
                .totalCursos(detalleCursos.size())
                .cursosAprobados(Math.toIntExact(aprobados))
                .cursosReprobados(Math.toIntExact(reprobados))
                .cursosSinCalificacion(Math.toIntExact(sinCalificacion))
                .detalleCursos(detalleCursos)
                .build();
    }

    private BigDecimal calcularPromedioGeneral(List<HistorialCursoResponseDTO> detalleCursos) {
        double promedio = detalleCursos.stream()
                .filter(curso -> !curso.getNotas().isEmpty())
                .map(HistorialCursoResponseDTO::getPromedioCurso)
                .map(BigDecimal::doubleValue)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.00);

        return BigDecimal.valueOf(promedio).setScale(2, RoundingMode.HALF_UP);
    }

    private Inscripcion seleccionarInscripcionRepresentativa(Inscripcion actual, Inscripcion repetida) {
        if (Boolean.TRUE.equals(actual.getActivo()) && !Boolean.TRUE.equals(repetida.getActivo())) {
            return actual;
        }
        if (Boolean.TRUE.equals(repetida.getActivo()) && !Boolean.TRUE.equals(actual.getActivo())) {
            return repetida;
        }
        return actual.getId() != null && repetida.getId() != null && repetida.getId() > actual.getId()
                ? repetida
                : actual;
    }

    private void validarEstudianteId(Long estudianteId) {
        if (estudianteId == null || estudianteId <= 0) {
            throw new IllegalArgumentException("El identificador del estudiante debe ser positivo.");
        }
    }

    private record CursoCicloKey(Long cursoId, Integer cicloAnio) {
    }
}
