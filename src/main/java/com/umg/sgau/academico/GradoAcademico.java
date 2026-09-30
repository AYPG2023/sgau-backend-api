package com.umg.sgau.academico;
import jakarta.persistence.*; import lombok.*;
@Entity @Table(name="grados_academicos",uniqueConstraints=@UniqueConstraint(name="uq_grado_codigo",columnNames="codigo"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GradoAcademico { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @Column(nullable=false,length=20) private String codigo; @Column(nullable=false,length=80) private String nombre; @Column(nullable=false) @Builder.Default private Boolean activo=true; }
