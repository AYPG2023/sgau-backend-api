package com.umg.sgau.academico;
import jakarta.persistence.*; import lombok.*;
@Entity @Table(name="secciones_academicas",uniqueConstraints=@UniqueConstraint(name="uq_seccion_grado_codigo",columnNames={"grado_id","codigo"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SeccionAcademica { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @Column(nullable=false,length=20) private String codigo; @Column(nullable=false,length=80) private String nombre; @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="grado_id",nullable=false) private GradoAcademico grado; @Column(nullable=false) @Builder.Default private Boolean activo=true; }
