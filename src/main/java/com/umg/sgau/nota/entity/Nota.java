package com.umg.sgau.nota.entity;

import com.umg.sgau.curso.entity.Curso;
import com.umg.sgau.estudiante.entity.Estudiante;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "notas")
public class Nota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;

    @Column(name = "ciclo_anio", nullable = false)
    private Integer cicloAnio;

    @Column(name = "tipo_evaluacion", nullable = false, length = 50)
    private String tipoEvaluacion;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal calificacion;

    @Column(length = 250)
    private String observaciones;

    @Column(nullable = false)
    private Boolean activo;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void alPersistir() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
        if (activo == null) {
            activo = true;
        }
    }

    @PreUpdate
    protected void alActualizar() {
        fechaActualizacion = LocalDateTime.now();
    }

    public Long getEstudianteId() {
        return estudiante == null ? null : estudiante.getId();
    }

    public void setEstudianteId(Long estudianteId) {
        this.estudiante = estudianteId == null ? null : Estudiante.builder().id(estudianteId).activo(true).build();
    }

    public Long getCursoId() {
        return curso == null ? null : curso.getId();
    }

    public void setCursoId(Long cursoId) {
        this.curso = cursoId == null ? null : Curso.builder().id(cursoId).activo(true).build();
    }

    public static class NotaBuilder {
        public NotaBuilder estudianteId(Long estudianteId) {
            this.estudiante = estudianteId == null ? null : Estudiante.builder().id(estudianteId).activo(true).build();
            return this;
        }

        public NotaBuilder cursoId(Long cursoId) {
            this.curso = cursoId == null ? null : Curso.builder().id(cursoId).activo(true).build();
            return this;
        }
    }
}
