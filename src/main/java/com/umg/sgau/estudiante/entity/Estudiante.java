package com.umg.sgau.estudiante.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.umg.sgau.colegiatura.entity.Colegiatura;
import com.umg.sgau.inscripcion.entity.Inscripcion;
import com.umg.sgau.nota.entity.Nota;
import com.umg.sgau.usuario.entity.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.time.*;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(
        name = "estudiantes",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "codigo_estudiantil"),
                @UniqueConstraint(columnNames = "numero_identificacion"),
                @UniqueConstraint(columnNames = "correo")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Estudiante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_estudiantil", nullable = false, length = 20)
    private String codigoEstudiantil;

    @Column(name = "numero_identificacion", nullable = false, length = 20)
    private String numeroIdentificacion;

    @Column(nullable = false, length = 100)
    private String nombres;

    @Column(nullable = false, length = 100)
    private String apellidos;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(nullable = false, length = 150)
    private String correo;

    @Column(length = 20)
    private String telefono;

    @Column(length = 250)
    private String direccion;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", unique = true)
    @JsonIgnore
    private Usuario usuario;

    @OneToMany(mappedBy = "estudiante", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<Inscripcion> inscripciones = new ArrayList<>();

    @OneToMany(mappedBy = "estudiante", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<Colegiatura> colegiaturas = new ArrayList<>();

    @OneToMany(mappedBy = "estudiante", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<Nota> notas = new ArrayList<>();

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void alPersistir() {
        LocalDateTime ahora = LocalDateTime.now();

        this.fechaCreacion = ahora;
        this.fechaActualizacion = ahora;

        if (this.activo == null) {
            this.activo = true;
        }
    }

    @PreUpdate
    protected void alActualizar() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}
