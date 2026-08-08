package com.umg.sgau.usuario.dto;

import jakarta.validation.constraints.NotNull;
import java.util.Set;
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
public class UsuarioRolesRequestDTO {

    @NotNull(message = "La lista de roles es obligatoria")
    private Set<Long> rolIds;
}
