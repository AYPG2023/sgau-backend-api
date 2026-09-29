package com.umg.sgau.auditoria.service;

import com.umg.sgau.auditoria.dto.AuditoriaResponseDTO;
import com.umg.sgau.auditoria.entity.AuditoriaEvento;
import com.umg.sgau.auditoria.repository.AuditoriaRepository;
import com.umg.sgau.usuario.entity.Usuario;
import com.umg.sgau.usuario.repository.UsuarioRepository;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditoriaService {
    private final AuditoriaRepository repository;
    private final UsuarioRepository usuarios;
    public AuditoriaService(AuditoriaRepository repository, UsuarioRepository usuarios) {
        this.repository = repository; this.usuarios = usuarios;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(Authentication auth, String accion, String modulo, String tipoEntidad,
            String entidadId, String descripcion, String antes, String despues) {
        AuditoriaEvento e = new AuditoriaEvento();
        Usuario actor = auth == null ? null : usuarios.findByUsernameIgnoreCaseOrEmailIgnoreCase(auth.getName(), auth.getName()).orElse(null);
        e.setUsuarioId(actor == null ? null : actor.getId());
        e.setUsername(actor == null ? "SISTEMA" : actor.getUsername());
        e.setAccion(accion); e.setModulo(modulo); e.setTipoEntidad(tipoEntidad); e.setEntidadId(entidadId);
        e.setResultado("EXITOSO"); e.setDescripcion(descripcion); e.setCambiosAntes(antes); e.setCambiosDespues(despues);
        repository.save(e);
    }

    @Transactional(readOnly = true)
    public Page<AuditoriaResponseDTO> buscar(LocalDateTime desde, LocalDateTime hasta, Long usuarioId,
            String username, String modulo, String accion, String tipoEntidad, String entidadId, Pageable pageable) {
        return repository.findAll((root, query, cb) -> {
            var p = new ArrayList<Predicate>();
            if (desde != null) p.add(cb.greaterThanOrEqualTo(root.get("fechaHora"), desde));
            if (hasta != null) p.add(cb.lessThanOrEqualTo(root.get("fechaHora"), hasta));
            if (usuarioId != null) p.add(cb.equal(root.get("usuarioId"), usuarioId));
            if (username != null && !username.isBlank()) p.add(cb.equal(cb.lower(root.get("username")), username.toLowerCase()));
            if (modulo != null && !modulo.isBlank()) p.add(cb.equal(cb.lower(root.get("modulo")), modulo.toLowerCase()));
            if (accion != null && !accion.isBlank()) p.add(cb.equal(cb.lower(root.get("accion")), accion.toLowerCase()));
            if (tipoEntidad != null && !tipoEntidad.isBlank()) p.add(cb.equal(cb.lower(root.get("tipoEntidad")), tipoEntidad.toLowerCase()));
            if (entidadId != null && !entidadId.isBlank()) p.add(cb.equal(root.get("entidadId"), entidadId));
            return cb.and(p.toArray(Predicate[]::new));
        }, pageable).map(this::dto);
    }

    private AuditoriaResponseDTO dto(AuditoriaEvento e) {
        return new AuditoriaResponseDTO(e.getId(), e.getFechaHora(), e.getUsuarioId(), e.getUsername(), e.getAccion(),
                e.getModulo(), e.getTipoEntidad(), e.getEntidadId(), e.getResultado(), e.getDescripcion(),
                e.getCambiosAntes(), e.getCambiosDespues());
    }
}
