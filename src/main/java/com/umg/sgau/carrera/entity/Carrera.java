package com.umg.sgau.carrera.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.umg.sgau.curso.entity.Curso;
import com.umg.sgau.inscripcion.entity.Inscripcion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.math.BigDecimal;
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
@Table(name = "carreras")
public class Carrera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(nullable = false, unique = true, length = 120)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false)
    private Integer duracionAnios;

    @Column(name = "mensualidad", precision = 10, scale = 2)
    private BigDecimal mensualidad;

    @Column(name = "cantidad_cuotas")
    private Integer cantidadCuotas;

    @Column(name = "dia_vencimiento")
    private Integer diaVencimiento;

    @Column(nullable = false)
    private Boolean activo;

    @OneToMany(mappedBy = "carrera", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<Curso> cursos = new ArrayList<>();

    @OneToMany(mappedBy = "carrera", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<Inscripcion> inscripciones = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = false)
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
}
