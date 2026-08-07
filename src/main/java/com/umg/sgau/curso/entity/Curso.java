package com.umg.sgau.curso.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.umg.sgau.carrera.entity.Carrera;
import com.umg.sgau.docente.entity.Docente;
import com.umg.sgau.inscripcion.entity.Inscripcion;
import com.umg.sgau.nota.entity.Nota;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "cursos")
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false)
    private Integer creditos;

    @Column(nullable = false)
    private Integer horasSemanales;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "carrera_id", nullable = false)
    private Carrera carrera;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "docente_id")
    private Docente docente;

    @OneToMany(mappedBy = "curso", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<Inscripcion> inscripciones = new ArrayList<>();

    @OneToMany(mappedBy = "curso", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<Nota> notas = new ArrayList<>();

    @Column(name = "ciclo_anio", nullable = false)
    private Integer cicloAnio;

    @Column(nullable = false)
    private Boolean activo;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void alPersistir() {
        LocalDateTime fechaActual = LocalDateTime.now();
        fechaCreacion = fechaActual;
        fechaActualizacion = fechaActual;

        if (activo == null) {
            activo = true;
        }
    }

    @PreUpdate
    protected void alActualizar() {
        fechaActualizacion = LocalDateTime.now();
    }

    public Long getCarreraId() {
        return carrera == null ? null : carrera.getId();
    }

    public void setCarreraId(Long carreraId) {
        this.carrera = carreraId == null ? null : Carrera.builder().id(carreraId).activo(true).build();
    }

    public Long getDocenteId() {
        return docente == null ? null : docente.getId();
    }

    public void setDocenteId(Long docenteId) {
        this.docente = docenteId == null ? null : Docente.builder().id(docenteId).activo(true).build();
    }

    public static class CursoBuilder {
        public CursoBuilder carreraId(Long carreraId) {
            this.carrera = carreraId == null ? null : Carrera.builder().id(carreraId).activo(true).build();
            return this;
        }

        public CursoBuilder docenteId(Long docenteId) {
            this.docente = docenteId == null ? null : Docente.builder().id(docenteId).activo(true).build();
            return this;
        }
    }
}
