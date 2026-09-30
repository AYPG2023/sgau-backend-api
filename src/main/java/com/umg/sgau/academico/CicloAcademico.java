package com.umg.sgau.academico;
import jakarta.persistence.*; import lombok.*; import java.time.LocalDate;
@Entity @Table(name="ciclos_academicos",uniqueConstraints=@UniqueConstraint(name="uq_ciclo_nombre_anio",columnNames={"nombre","anio"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CicloAcademico { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @Column(nullable=false,length=40) private String nombre; @Column(nullable=false) private Integer anio; @Column(name="fecha_inicio",nullable=false) private LocalDate fechaInicio; @Column(name="fecha_fin",nullable=false) private LocalDate fechaFin; @Column(nullable=false) @Builder.Default private Boolean activo=true; }
