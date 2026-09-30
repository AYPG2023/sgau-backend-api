package com.umg.sgau.colegiatura.entity;

import com.umg.sgau.estudiante.entity.Estudiante;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "solicitudes_pago", uniqueConstraints = @UniqueConstraint(name = "uk_solicitud_pago_idempotencia", columnNames = {"estudiante_id", "idempotency_key"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SolicitudPago {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "colegiatura_id", nullable = false) private Colegiatura colegiatura;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "estudiante_id", nullable = false) private Estudiante estudiante;
    @Column(nullable = false, precision = 10, scale = 2) private BigDecimal monto;
    @Column(name = "fecha_pago", nullable = false) private LocalDate fechaPago;
    @Column(nullable = false, length = 100) private String referencia;
    @Column(name = "metodo_pago", length = 40) private String metodoPago;
    @Column(name = "comprobante_url", length = 500) private String comprobanteUrl;
    @Column(nullable = false, length = 20) private String estado;
    @Column(name = "motivo_rechazo", length = 250) private String motivoRechazo;
    @Column(name = "idempotency_key", nullable = false, length = 80) private String idempotencyKey;
    @Column(name = "referencia_unica", length = 150, unique = true) private String referenciaUnica;
    @Column(name = "fecha_creacion", nullable = false, updatable = false) private LocalDateTime fechaCreacion;
    @Column(name = "fecha_revision") private LocalDateTime fechaRevision;
    @Column(name = "revisado_por_usuario_id") private Long revisadoPorUsuarioId;
    @PrePersist void prePersist() { if (estado == null) estado = "PENDIENTE"; if (fechaCreacion == null) fechaCreacion = LocalDateTime.now(); }
}
