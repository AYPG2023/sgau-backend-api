package com.umg.sgau.colegiatura.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
public final class SolicitudPagoDTOs {
    private SolicitudPagoDTOs() {}
    public record Registro(@NotNull @DecimalMin("0.01") @Digits(integer=8,fraction=2) BigDecimal monto,
        @NotNull @PastOrPresent LocalDate fechaPago, @NotBlank @Size(max=100) String referencia,
        @Size(max=40) String metodoPago, @Size(max=500) String comprobanteUrl,
        @NotBlank @Size(max=80) String idempotencyKey) {}
    public record Revision(@NotBlank @Pattern(regexp="APROBADO|RECHAZADO") String estado, @Size(max=250) String motivo) {}
    public record Respuesta(Long id, Long colegiaturaId, BigDecimal monto, LocalDate fechaPago, String referencia,
        String metodoPago, String comprobanteUrl, String estado, String motivoRechazo,
        LocalDateTime fechaCreacion, LocalDateTime fechaRevision) {}
}
