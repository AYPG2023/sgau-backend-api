package com.umg.sgau.auditoria.dto;

import java.time.LocalDateTime;

public record AuditoriaResponseDTO(Long id, LocalDateTime fechaHora, Long usuarioId, String username,
        String accion, String modulo, String tipoEntidad, String entidadId, String resultado,
        String descripcion, String cambiosAntes, String cambiosDespues) {}
