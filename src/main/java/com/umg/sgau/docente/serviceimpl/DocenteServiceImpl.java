package com.umg.sgau.docente.serviceimpl;

import com.umg.sgau.academico.service.AcademicAccountLinkService;
import com.umg.sgau.docente.entity.Docente;
import com.umg.sgau.docente.exception.DocenteDuplicadoException;
import com.umg.sgau.docente.exception.DocenteNoEncontradoException;
import com.umg.sgau.docente.repository.DocenteRepository;
import com.umg.sgau.docente.service.DocenteService;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @Transactional
public class DocenteServiceImpl implements DocenteService {
    private final DocenteRepository docentes;
    private final AcademicAccountLinkService accountLinks;
    public DocenteServiceImpl(DocenteRepository docentes, AcademicAccountLinkService accountLinks) {
        this.docentes = docentes; this.accountLinks = accountLinks;
    }
    @Override public Docente crear(Docente d) { return crearOVincular(d, null, false, null, null); }
    @Override public Docente crearOVincular(Docente d, Long usuarioId, Boolean acceso, String username, String password) {
        Docente codigo=docentes.findByCodigoDocente(d.getCodigoDocente()).orElse(null);
        Docente email=docentes.findByEmailIgnoreCase(d.getEmail()).orElse(null);
        if (codigo != null || email != null) {
            if (codigo == null || email == null || !codigo.getId().equals(email.getId()))
                throw new DocenteDuplicadoException("El codigo o email pertenece a otro docente");
            if (codigo.getUsuario() != null) {
                if (usuarioId == null || codigo.getUsuario().getId().equals(usuarioId)) return codigo;
                throw new DocenteDuplicadoException("El docente ya esta vinculado a otro usuario");
            }
            codigo.setUsuario(accountLinks.resolver(usuarioId, acceso, username, password,
                    d.getEmail(), d.getNombre(), d.getApellido(), "DOCENTE"));
            return docentes.save(codigo);
        }
        d.setUsuario(accountLinks.resolver(usuarioId, acceso, username, password,
                d.getEmail(), d.getNombre(), d.getApellido(), "DOCENTE"));
        d.setActivo(true); return docentes.save(d);
    }
    @Override @Transactional(readOnly=true) public Docente obtenerPorId(Long id) { return docentes.findById(id).orElseThrow(() -> new DocenteNoEncontradoException(id)); }
    @Override @Transactional(readOnly=true) public List<Docente> obtenerTodos() { return docentes.findAll(); }
    @Override @Transactional(readOnly=true) public Page<Docente> listar(String q, Boolean activo, Pageable p) { return docentes.buscarConFiltros(q==null||q.isBlank()?"":q.trim(), activo, p); }
    @Override @Transactional(readOnly=true) public List<Docente> obtenerActivos() { return docentes.findSeleccionables(); }
    @Override @Transactional(readOnly=true) public List<String> obtenerCorreosActivos() { return docentes.findSeleccionables().stream().map(Docente::getEmail).toList(); }
    @Override public Docente actualizar(Long id, Docente d) {
        Docente a=obtenerPorId(id);
        if(docentes.existsByCodigoDocenteAndIdNot(d.getCodigoDocente(),id)) throw new DocenteDuplicadoException("Codigo duplicado");
        if(docentes.existsByEmailAndIdNot(d.getEmail(),id)) throw new DocenteDuplicadoException("Email duplicado");
        a.setCodigoDocente(d.getCodigoDocente()); a.setNombre(d.getNombre()); a.setApellido(d.getApellido());
        a.setEmail(d.getEmail()); a.setTelefono(d.getTelefono()); a.setEspecialidad(d.getEspecialidad()); return docentes.save(a);
    }
    @Override public Docente cambiarEstado(Long id, Boolean activo) { Docente d=obtenerPorId(id); d.setActivo(activo); return docentes.save(d); }
    @Override public void eliminar(Long id) { cambiarEstado(id,false); }
}
