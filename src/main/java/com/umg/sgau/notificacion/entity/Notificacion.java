package com.umg.sgau.notificacion.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
@Entity @Table(name="notificaciones", uniqueConstraints=@UniqueConstraint(name="uk_notificacion_evento_usuario",columnNames={"usuario_id","event_key"}), indexes={@Index(name="idx_notificacion_usuario_fecha",columnList="usuario_id,fecha_creacion"),@Index(name="idx_notificacion_entrega",columnList="entrega_estado,next_attempt_at")})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Notificacion {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(name="usuario_id",nullable=false) private Long usuarioId;
 @Column(name="event_key",nullable=false,length=160) private String eventKey;
 @Column(nullable=false,length=40) private String tipo;
 @Column(nullable=false,length=120) private String titulo;
 @Column(nullable=false,length=500) private String mensaje;
 @Column(name="destino_tipo",length=30) private String destinoTipo;
 @Column(name="destino_id") private Long destinoId;
 @Column(name="fecha_creacion",nullable=false,updatable=false) private LocalDateTime fechaCreacion;
 @Column(name="leida",nullable=false) @Builder.Default private boolean leida=false;
 @Column(name="entrega_estado",nullable=false,length=20) @Builder.Default private String entregaEstado="PENDIENTE";
 @Column(name="intentos",nullable=false) @Builder.Default private int intentos=0;
 @Column(name="next_attempt_at") private LocalDateTime nextAttemptAt;
 @Column(name="entrega_error",length=300) private String entregaError;
 @PrePersist void persist(){if(fechaCreacion==null)fechaCreacion=LocalDateTime.now();if(nextAttemptAt==null)nextAttemptAt=fechaCreacion;}
}
