package com.umg.sgau.nota.dto;

import jakarta.validation.constraints.NotNull;
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
public class NotaStatusRequestDTO {

    @NotNull(message = "El estado de la nota es obligatorio")
    private Boolean activo;
}
