package com.umg.sgau.estadogeneral.serviceimpl;

import com.umg.sgau.colegiatura.entity.Colegiatura;
import com.umg.sgau.colegiatura.model.EstadoCuentaEstudiante;
import com.umg.sgau.colegiatura.service.ColegiaturaService;
import com.umg.sgau.estadogeneral.model.EstadoGeneralEstudiante;
import com.umg.sgau.estadogeneral.service.EstadoGeneralEstudianteService;
import com.umg.sgau.estudiante.entity.Estudiante;
import com.umg.sgau.estudiante.service.EstudianteService;
import com.umg.sgau.historialacademico.dto.HistorialAcademicoResponseDTO;
import com.umg.sgau.historialacademico.dto.HistorialCursoResponseDTO;
import com.umg.sgau.historialacademico.service.HistorialAcademicoService;
import com.umg.sgau.inscripcion.entity.Inscripcion;
import com.umg.sgau.inscripcion.service.InscripcionService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EstadoGeneralEstudianteServiceImpl implements EstadoGeneralEstudianteService {

    private static final BigDecimal CERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private static final String RESULTADO_REPROBADO = "REPROBADO";
    private static final String RESULTADO_EN_CURSO = "EN_CURSO";
    private static final String ESTADO_PAGADA = "PAGADA";

    private static final String ESTADO_ACADEMICO_SIN_INFORMACION = "SIN_INFORMACION";
    private static final String ESTADO_ACADEMICO_EN_CURSO = "EN_CURSO";
    private static final String ESTADO_ACADEMICO_APROBADO = "APROBADO";
    private static final String ESTADO_ACADEMICO_CON_REPROBADOS = "CON_CURSOS_REPROBADOS";
    private static final String ESTADO_ACADEMICO_INACTIVO = "INACTIVO";

    private static final String ESTADO_FINANCIERO_SIN_REGISTROS = "SIN_REGISTROS";
    private static final String ESTADO_FINANCIERO_AL_DIA = "AL_DIA";
    private static final String ESTADO_FINANCIERO_PENDIENTE = "PENDIENTE";

    private static final String ESTADO_GENERAL_SIN_INFORMACION = "SIN_INFORMACION";
    private static final String ESTADO_GENERAL_REGULAR = "REGULAR";
    private static final String ESTADO_GENERAL_PENDIENTES_ACADEMICOS = "CON_PENDIENTES_ACADEMICOS";
    private static final String ESTADO_GENERAL_SALDO_PENDIENTE = "CON_SALDO_PENDIENTE";
    private static final String ESTADO_GENERAL_PENDIENTES_ACADEMICOS_Y_FINANCIEROS =
            "CON_PENDIENTES_ACADEMICOS_Y_FINANCIEROS";
    private static final String ESTADO_GENERAL_INACTIVO = "INACTIVO";

    private final EstudianteService estudianteService;
    private final HistorialAcademicoService historialAcademicoService;
    private final InscripcionService inscripcionService;
    private final ColegiaturaService colegiaturaService;

    public EstadoGeneralEstudianteServiceImpl(
            EstudianteService estudianteService,
            HistorialAcademicoService historialAcademicoService,
            InscripcionService inscripcionService,
            ColegiaturaService colegiaturaService) {
        this.estudianteService = estudianteService;
        this.historialAcademicoService = historialAcademicoService;
        this.inscripcionService = inscripcionService;
        this.colegiaturaService = colegiaturaService;
    }

    @Override
    public EstadoGeneralEstudiante obtenerEstadoGeneral(Long estudianteId) {
        validarEstudianteId(estudianteId);

        Estudiante estudiante = estudianteService.obtenerPorId(estudianteId);
        HistorialAcademicoResponseDTO historialAcademico =
                historialAcademicoService.generarHistorial(estudianteId);
        EstadoCuentaEstudiante estadoCuenta = colegiaturaService.generarEstadoCuenta(estudianteId);
        List<Inscripcion> inscripciones = obtenerInscripciones(estudianteId);

        int totalInscripciones = inscripciones.size();
        int inscripcionesActivas = contarInscripcionesActivas(inscripciones);
        Integer cicloMasReciente = obtenerCicloMasReciente(inscripciones);
        int cursosEnCurso = contarCursosPorResultado(historialAcademico, RESULTADO_EN_CURSO);
        int cantidadPagadas = contarColegiaturasPagadas(estadoCuenta);
        String estadoAcademico = determinarEstadoAcademico(estudiante, historialAcademico, cursosEnCurso);
        String estadoFinanciero = determinarEstadoFinanciero(estadoCuenta);
        String estadoGeneral = determinarEstadoGeneral(estudiante, historialAcademico, estadoCuenta);

        return new EstadoGeneralEstudiante(
                estudiante,
                historialAcademico,
                estadoCuenta,
                totalInscripciones,
                inscripcionesActivas,
                cicloMasReciente,
                cursosEnCurso,
                cantidadPagadas,
                estadoAcademico,
                estadoFinanciero,
                estadoGeneral);
    }

    private List<Inscripcion> obtenerInscripciones(Long estudianteId) {
        return inscripcionService.historialPorEstudiante(estudianteId, Pageable.unpaged())
                .getContent()
                .stream()
                .collect(java.util.stream.Collectors.toList());
    }

    private int contarInscripcionesActivas(List<Inscripcion> inscripciones) {
        return Math.toIntExact(inscripciones.stream()
                .filter(inscripcion -> Boolean.TRUE.equals(inscripcion.getActivo()))
                .count());
    }

    private Integer obtenerCicloMasReciente(List<Inscripcion> inscripciones) {
        return inscripciones.stream()
                .map(Inscripcion::getCicloAnio)
                .filter(java.util.Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(null);
    }

    private int contarCursosPorResultado(
            HistorialAcademicoResponseDTO historialAcademico,
            String resultado) {
        return Math.toIntExact(historialAcademico.getDetalleCursos()
                .stream()
                .filter(curso -> resultado.equals(curso.getResultado()))
                .count());
    }

    private int contarColegiaturasPagadas(EstadoCuentaEstudiante estadoCuenta) {
        return Math.toIntExact(estadoCuenta.detalle()
                .stream()
                .filter(colegiatura -> ESTADO_PAGADA.equals(colegiatura.getEstado())
                        || normalizarMonto(colegiatura.getSaldoPendiente()).compareTo(CERO) == 0)
                .count());
    }

    private String determinarEstadoAcademico(
            Estudiante estudiante,
            HistorialAcademicoResponseDTO historialAcademico,
            int cursosEnCurso) {
        if (!Boolean.TRUE.equals(estudiante.getActivo())) {
            return ESTADO_ACADEMICO_INACTIVO;
        }

        if (historialAcademico.getTotalCursos() == 0) {
            return ESTADO_ACADEMICO_SIN_INFORMACION;
        }

        if (historialAcademico.getCursosReprobados() > 0) {
            return ESTADO_ACADEMICO_CON_REPROBADOS;
        }

        if (cursosEnCurso > 0 || historialAcademico.getCursosSinCalificacion() > 0) {
            return ESTADO_ACADEMICO_EN_CURSO;
        }

        return ESTADO_ACADEMICO_APROBADO;
    }

    private String determinarEstadoFinanciero(EstadoCuentaEstudiante estadoCuenta) {
        if (estadoCuenta.cantidadCargos() == 0) {
            return ESTADO_FINANCIERO_SIN_REGISTROS;
        }

        return normalizarMonto(estadoCuenta.saldoPendiente()).compareTo(CERO) == 0
                ? ESTADO_FINANCIERO_AL_DIA
                : ESTADO_FINANCIERO_PENDIENTE;
    }

    private String determinarEstadoGeneral(
            Estudiante estudiante,
            HistorialAcademicoResponseDTO historialAcademico,
            EstadoCuentaEstudiante estadoCuenta) {
        if (!Boolean.TRUE.equals(estudiante.getActivo())) {
            return ESTADO_GENERAL_INACTIVO;
        }

        boolean sinInformacionAcademica = historialAcademico.getTotalCursos() == 0;
        boolean sinInformacionFinanciera = estadoCuenta.cantidadCargos() == 0;
        if (sinInformacionAcademica && sinInformacionFinanciera) {
            return ESTADO_GENERAL_SIN_INFORMACION;
        }

        boolean tienePendientesAcademicos = historialAcademico.getDetalleCursos()
                .stream()
                .map(HistorialCursoResponseDTO::getResultado)
                .filter(java.util.Objects::nonNull)
                .anyMatch(RESULTADO_REPROBADO::equals);
        boolean tieneSaldoPendiente = normalizarMonto(estadoCuenta.saldoPendiente()).compareTo(CERO) > 0;

        if (tienePendientesAcademicos && tieneSaldoPendiente) {
            return ESTADO_GENERAL_PENDIENTES_ACADEMICOS_Y_FINANCIEROS;
        }
        if (tienePendientesAcademicos) {
            return ESTADO_GENERAL_PENDIENTES_ACADEMICOS;
        }
        if (tieneSaldoPendiente) {
            return ESTADO_GENERAL_SALDO_PENDIENTE;
        }

        return ESTADO_GENERAL_REGULAR;
    }

    private BigDecimal normalizarMonto(BigDecimal monto) {
        if (monto == null) {
            return CERO;
        }
        return monto.setScale(2, RoundingMode.HALF_UP);
    }

    private void validarEstudianteId(Long estudianteId) {
        if (estudianteId == null || estudianteId <= 0) {
            throw new IllegalArgumentException("El identificador del estudiante debe ser positivo.");
        }
    }
}
