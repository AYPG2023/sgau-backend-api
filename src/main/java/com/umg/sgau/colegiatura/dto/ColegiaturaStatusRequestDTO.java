package com.umg.sgau.colegiatura.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ColegiaturaStatusRequestDTO {

    @NotNull(message = "El estado activo es obligatorio")
    private Boolean activo;

}