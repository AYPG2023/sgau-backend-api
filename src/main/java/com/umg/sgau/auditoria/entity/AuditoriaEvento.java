package com.umg.sgau.auditoria.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "auditoria_eventos", indexes = {
        @Index(name = "idx_auditoria_fecha", columnList = "fecha_hora"),
        @Index(name = "idx_auditoria_usuario", columnList = "usuario_id"),
        @Index(name = "idx_auditoria_modulo_accion", columnList = "modulo,accion"),
        @Index(name = "idx_auditoria_entidad", columnList = "tipo_entidad,entidad_id")})
@Getter @Setter @NoArgsConstructor
public class AuditoriaEvento {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "fecha_hora", nullable = false, updatable = false)
    private LocalDateTime fechaHora;
    @Column(name = "usuario_id")
    private Long usuarioId;
    @Column(nullable = false, length = 100)
    private String username;
    @Column(nullable = false, length = 60)
    private String accion;
    @Column(nullable = false, length = 60)
    private String modulo;
    @Column(name = "tipo_entidad", nullable = false, length = 100)
    private String tipoEntidad;
    @Column(name = "entidad_id", length = 80)
    private String entidadId;
    @Column(nullable = false, length = 20)
    private String resultado;
    @Column(nullable = false, length = 500)
    private String descripcion;
    @Column(name = "cambios_antes", columnDefinition = "TEXT")
    private String cambiosAntes;
    @Column(name = "cambios_despues", columnDefinition = "TEXT")
    private String cambiosDespues;

    @PrePersist void prePersist() { if (fechaHora == null) fechaHora = LocalDateTime.now(); }
}
