package com.umg.sgau.inscripcion.entity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

    @Column(name= "estudiante_id", nullable = false)
    private Long estudianteId;

    @Column(name = "carrera_id", nullable = false)
    private Long carreraId;

    @Column(name = "curso_id")
    private Long cursoId;

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


}
