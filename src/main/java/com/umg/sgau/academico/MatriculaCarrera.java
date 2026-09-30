package com.umg.sgau.academico;

import com.umg.sgau.carrera.entity.Carrera;
import com.umg.sgau.estudiante.entity.Estudiante;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.*;

@Entity @Table(name="matriculas_carrera")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MatriculaCarrera {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="estudiante_id",nullable=false) private Estudiante estudiante;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="carrera_id",nullable=false) private Carrera carrera;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="ciclo_id",nullable=false) private CicloAcademico ciclo;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="grado_id",nullable=false) private GradoAcademico gradoAcademico;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="seccion_id",nullable=false) private SeccionAcademica seccionAcademica;
 @Column(nullable=false) private LocalDate fechaInscripcion;
 @Column(nullable=false,length=20) @Builder.Default private String estado="ACTIVA";
 @Column(nullable=false) @Builder.Default private Boolean activo=true;
 @Column(name="fecha_creacion",nullable=false,updatable=false) private LocalDateTime fechaCreacion;
 @Transient private Integer cicloAnioHistorico;
 @Transient private String gradoHistorico;
 @Transient private String seccionHistorica;
 public Integer getCicloAnio(){return ciclo==null?cicloAnioHistorico:ciclo.getAnio();}
 public String getGrado(){return gradoAcademico==null?gradoHistorico:gradoAcademico.getNombre();}
 public String getSeccion(){return seccionAcademica==null?seccionHistorica:seccionAcademica.getCodigo();}
 @PrePersist void prePersist(){if(fechaCreacion==null)fechaCreacion=LocalDateTime.now();if(activo==null)activo=true;if(estado==null)estado="ACTIVA";}
}
