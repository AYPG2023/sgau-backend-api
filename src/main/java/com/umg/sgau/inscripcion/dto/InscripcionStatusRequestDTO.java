package com.umg.sgau.inscripcion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InscripcionStatusRequestDTO {

    @NotBlank(message = "El motivo de anulación es obligatorio")
    @Size(max = 250)
    private String motivo;
}