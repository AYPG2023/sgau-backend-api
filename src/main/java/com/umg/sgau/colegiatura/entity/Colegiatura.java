package com.umg.sgau.colegiatura.entity;

import com.umg.sgau.estudiante.entity.Estudiante;
import com.umg.sgau.academico.CicloAcademico;
import com.umg.sgau.academico.MatriculaCarrera;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "colegiaturas",
        indexes = {
                @Index(name = "idx_colegiatura_estudiante", columnList = "estudiante_id"),
                @Index(name = "idx_colegiatura_estado", columnList = "estado"),
                @Index(name = "idx_colegiatura_activo", columnList = "activo"),
                @Index(name = "idx_colegiatura_ciclo", columnList = "ciclo_anio")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Colegiatura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;

    @Column(name = "ciclo_anio", nullable = false)
    private Integer cicloAnio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ciclo_id")
    private CicloAcademico ciclo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inscripcion_carrera_id")
    private MatriculaCarrera inscripcionCarrera;

    @Column(name = "numero_cuota")
    private Integer numeroCuota;

    @Column(nullable = false, length = 120)
    private String concepto;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal montoTotal;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal montoPagado;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal saldoPendiente;

    @Column(nullable = false)
    private LocalDate fechaEmision;

    @Column(nullable = false)
    private LocalDate fechaVencimiento;

    @Column(nullable = false, length = 20)
    private String estado;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void alPersistir() {

        LocalDateTime ahora = LocalDateTime.now();

        this.fechaCreacion = ahora;
        this.fechaActualizacion = ahora;

        if (this.montoPagado == null) {
            this.montoPagado = BigDecimal.ZERO;
        }

        if (this.saldoPendiente == null) {
            this.saldoPendiente = this.montoTotal;
        }

        if (this.estado == null) {
            this.estado = "PENDIENTE";
        }

        if (this.activo == null) {
            this.activo = true;
        }
    }

    @PreUpdate
    protected void alActualizar() {
        this.fechaActualizacion = LocalDateTime.now();
    }

    public Long getEstudianteId() {
        return estudiante == null ? null : estudiante.getId();
    }

    public void setEstudianteId(Long estudianteId) {
        this.estudiante = estudianteId == null ? null : Estudiante.builder().id(estudianteId).build();
    }

    public static class ColegiaturaBuilder {
        public ColegiaturaBuilder estudianteId(Long estudianteId) {
            this.estudiante = estudianteId == null ? null : Estudiante.builder().id(estudianteId).build();
            return this;
        }
    }
}
