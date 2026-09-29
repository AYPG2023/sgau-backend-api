package com.umg.sgau.usuario.dto;

import com.umg.sgau.rol.dto.RolSummaryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resultado del alta atomica")
public class AltaUsuarioResponseDTO {
    private Long usuarioId;
    private Set<RolSummaryDTO> roles;
    private Long docenteId;
    private Long estudianteId;
}
