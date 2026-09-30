package com.umg.sgau.inscripcion.entity;
import com.umg.sgau.carrera.entity.Carrera;
import com.umg.sgau.curso.entity.Curso;
import com.umg.sgau.estudiante.entity.Estudiante;
import com.umg.sgau.academico.CicloAcademico;
import com.umg.sgau.academico.GradoAcademico;
import com.umg.sgau.academico.SeccionAcademica;
import com.umg.sgau.academico.MatriculaCarrera;
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

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name= "inscripciones")
public class Inscripcion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name= "estudiante_id", nullable = false)
    private Estudiante estudiante;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "carrera_id", nullable = false)
    private Carrera carrera;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curso_id")
    private Curso curso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ciclo_id")
    private CicloAcademico ciclo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grado_id")
    private GradoAcademico gradoCatalogo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seccion_id")
    private SeccionAcademica seccionCatalogo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matricula_carrera_id")
    private MatriculaCarrera matriculaCarrera;

    @Column(nullable = false, length = 50)
    private String grado;

    @Column(nullable = false, length = 20)
    private String seccion;

    @Column(name = "ciclo_anio", nullable = false)
    private Integer cicloAnio;

    @Column(name = "fecha_inscripcion", nullable = false)
    private LocalDate fechaInscripcion;

    @Column(nullable = false , length = 20)
    private String estado;

    @Column(length = 500)
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
        if (estado == null) {
            estado = "ACTIVA";
        }
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

    public Long getCarreraId() {
        return carrera == null ? null : carrera.getId();
    }

    public void setCarreraId(Long carreraId) {
        this.carrera = carreraId == null ? null : Carrera.builder().id(carreraId).activo(true).build();
    }

    public Long getCursoId() {
        return curso == null ? null : curso.getId();
    }

    public void setCursoId(Long cursoId) {
        this.curso = cursoId == null ? null : Curso.builder().id(cursoId).activo(true).build();
    }

    public static class InscripcionBuilder {
        public InscripcionBuilder estudianteId(Long estudianteId) {
            this.estudiante = estudianteId == null ? null : Estudiante.builder().id(estudianteId).activo(true).build();
            return this;
        }

        public InscripcionBuilder carreraId(Long carreraId) {
            this.carrera = carreraId == null ? null : Carrera.builder().id(carreraId).activo(true).build();
            return this;
        }

        public InscripcionBuilder cursoId(Long cursoId) {
            this.curso = cursoId == null ? null : Curso.builder().id(cursoId).activo(true).build();
            return this;
        }
    }
}
