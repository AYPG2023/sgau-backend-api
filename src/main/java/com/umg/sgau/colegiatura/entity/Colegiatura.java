package com.umg.sgau.colegiatura.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "colegiaturas",
        indexes = {
                @Index(name = "idx_colegiatura_estudiante", columnList = "estudianteId"),
                @Index(name = "idx_colegiatura_estado", columnList = "estado"),
                @Index(name = "idx_colegiatura_activo", columnList = "activo"),
                @Index(name = "idx_colegiatura_ciclo", columnList = "cicloAnio")
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

    @Column(nullable = false)
    private Long estudianteId;

    @Column(nullable = false)
    private Integer cicloAnio;

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

}