package com.umg.sgau.rol.dto;

import jakarta.validation.constraints.NotNull;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RolPermisosRequestDTO {

    @NotNull(message = "La lista de permisos es obligatoria")
    private Set<Long> permisoIds;
}
