package com.umg.sgau.notificacion.entity;
import jakarta.persistence.*; import java.time.LocalDateTime; import lombok.*;
@Entity @Table(name="dispositivos_push",uniqueConstraints=@UniqueConstraint(name="uk_dispositivo_token",columnNames="token"),indexes=@Index(name="idx_dispositivo_usuario",columnList="usuario_id")) @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DispositivoPush { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @Column(name="usuario_id",nullable=false) private Long usuarioId; @Column(nullable=false,length=500) private String token; @Column(name="actualizado_en",nullable=false) private LocalDateTime actualizadoEn; @PrePersist @PreUpdate void update(){actualizadoEn=LocalDateTime.now();} }
