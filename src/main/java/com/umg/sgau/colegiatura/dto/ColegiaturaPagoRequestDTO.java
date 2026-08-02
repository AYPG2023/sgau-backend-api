package com.umg.sgau.colegiatura.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ColegiaturaPagoRequestDTO {

    @NotNull(message = "El monto del pago es obligatorio")
    @DecimalMin(
            value = "0.01",
            message = "El pago debe ser mayor que cero"
    )
    private BigDecimal montoPago;

    @NotNull(message = "La fecha del pago es obligatoria")
    @PastOrPresent
    private LocalDate fechaPago;

    @Size(max = 250)
    private String observaciones;

}