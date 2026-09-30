package com.umg.sgau.academico.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public final class AcademicoDTOs {
    private AcademicoDTOs() {}

    public record DocentePerfil(Long id, String codigo, String nombre, String apellido,
            String email, String telefono, String especialidad, Boolean activo, String identidadFuente) {}

    public record EstudiantePerfil(Long id, String codigo, String nombres, String apellidos,
            String correo, String telefono, String direccion, Boolean activo, String identidadFuente) {}

    public record Curso(Long id, String codigo, String nombre, String descripcion,
            Integer creditos, Integer horasSemanales, Integer cicloAnio, Boolean activo,
            Long carreraId, String carreraCodigo, String carreraNombre, Long cicloId, String cicloNombre) {}

    public record Carrera(Long id, String codigo, String nombre, String descripcion,
            Integer duracionAnios, Integer cicloActual) {}

    public record DocenteCurso(Long id, String codigo, String nombre, String apellido,
            String email, String especialidad, Long cursoId, String cursoCodigo,
            String cursoNombre, Integer cicloAnio) {}

    public record CursoPlan(Long id, String codigo, String nombre, String descripcion,
            Integer creditos, Integer horasSemanales, Integer cicloAnio, Long cicloId, String cicloNombre,
            Boolean inscrito, Long docenteId, String docenteNombre) {}

    public record PlanCarrera(Carrera carrera, List<CursoPlan> cursosDisponibles,
            List<Curso> cursosInscritos, Integer totalCreditosPlan,
            Integer totalCreditosInscritos) {}

    public record Inscripcion(Long id, Long estudianteId, String estudianteCodigo,
            String estudianteNombre, Long cursoId, String cursoCodigo, String cursoNombre,
            Long carreraId, String carreraCodigo, String carreraNombre, String grado,
            String seccion, Integer cicloAnio, LocalDate fechaInscripcion, String estado,
            String observaciones, Boolean activo) {}

    public record Nota(Long id, Long estudianteId, String estudianteCodigo,
            String estudianteNombre, Long cursoId, String cursoCodigo, String cursoNombre,
            Integer cicloAnio, String tipoEvaluacion, BigDecimal calificacion,
            String observaciones, Boolean activo) {}

    public record Colegiatura(Long id, Integer cicloAnio, String cicloNombre, String concepto,
            BigDecimal montoTotal, BigDecimal montoPagado, BigDecimal saldoPendiente,
            LocalDate fechaEmision, LocalDate fechaVencimiento, String estado, Boolean activo) {}

    public record Promedio(Long estudianteId, BigDecimal promedio, int cantidadNotas) {}

    public record EstadoCuenta(Long estudianteId, BigDecimal totalCargos,
            BigDecimal totalPagado, BigDecimal saldoPendiente, int cantidadCargos,
            int cantidadPendientes, List<Colegiatura> detalle) {}

    public record CarreraDisponible(Long id,String codigo,String nombre,String descripcion,Integer duracionAnios,BigDecimal mensualidad,Integer cantidadCuotas,Integer diaVencimiento) {}
    public record Ciclo(Long id,String nombre,Integer anio,LocalDate fechaInicio,LocalDate fechaFin,Boolean activo) {}
    public record Grado(Long id,String codigo,String nombre) {}
    public record Seccion(Long id,String codigo,String nombre,Long gradoId) {}
    public record SolicitudInscripcion(@NotNull @Positive Long carreraId,@NotNull @Positive Long cicloId,@NotNull @Positive Long gradoId,@NotNull @Positive Long seccionId) {}
    public record ResultadoInscripcion(Long inscripcionId,Long carreraId,String carreraNombre,Integer cicloAnio,String cicloNombre,BigDecimal mensualidad,Integer cantidadCuotas,Integer diaVencimiento) {}
    public record SolicitudAsignacion(Long cursoId) {}
}
