package com.umg.sgau.estudiante.serviceimpl;

import com.umg.sgau.academico.service.AcademicAccountLinkService;
import com.umg.sgau.academico.service.AcademicIdentityPolicy;
import com.umg.sgau.estudiante.entity.Estudiante;
import com.umg.sgau.estudiante.exception.EstudianteNoEncontradoException;
import com.umg.sgau.estudiante.repository.EstudianteRepository;
import com.umg.sgau.estudiante.service.EstudianteService;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service @RequiredArgsConstructor @Transactional
public class EstudianteServiceImpl implements EstudianteService {
    private final EstudianteRepository estudianteRepository;
    private final AcademicAccountLinkService accountLinks;
    @Override public Estudiante crear(Estudiante e) { return crearOVincular(e,null,false,null,null); }
    @Override public Estudiante crearOVincular(Estudiante e, Long usuarioId, Boolean acceso, String username, String password) {
        Estudiante codigo=estudianteRepository.findByCodigoEstudiantil(e.getCodigoEstudiantil()).orElse(null);
        Estudiante doc=estudianteRepository.findByNumeroIdentificacion(e.getNumeroIdentificacion()).orElse(null);
        Estudiante correo=estudianteRepository.findByCorreoIgnoreCase(e.getCorreo())
                .or(() -> estudianteRepository.findByUsuario_EmailIgnoreCase(e.getCorreo())).orElse(null);
        if(codigo!=null||doc!=null||correo!=null) {
            if(codigo==null||doc==null||correo==null||!codigo.getId().equals(doc.getId())||!codigo.getId().equals(correo.getId()))
                throw new IllegalArgumentException("El codigo, identificacion o correo pertenece a otro estudiante");
            if(codigo.getUsuario()!=null) {
                if(usuarioId==null||codigo.getUsuario().getId().equals(usuarioId)) {
                    AcademicIdentityPolicy.validar(codigo.getUsuario(),e.getNombres(),e.getApellidos(),e.getCorreo());
                    return codigo;
                }
                throw new IllegalArgumentException("El estudiante ya esta vinculado a otro usuario");
            }
            var usuario = accountLinks.resolver(usuarioId,acceso,username,password,e.getCorreo(),e.getNombres(),e.getApellidos(),"ESTUDIANTE");
            if (usuario != null) AcademicIdentityPolicy.validar(usuario,codigo.getNombres(),codigo.getApellidos(),codigo.getCorreo());
            codigo.setUsuario(usuario);
            return estudianteRepository.save(codigo);
        }
        e.setUsuario(accountLinks.resolver(usuarioId,acceso,username,password,e.getCorreo(),e.getNombres(),e.getApellidos(),"ESTUDIANTE"));
        if (e.getUsuario() != null) { e.setNombres(null); e.setApellidos(null); e.setCorreo(null); }
        e.setActivo(true); return estudianteRepository.save(e);
    }
    @Override @Transactional(readOnly=true) public Estudiante obtenerPorId(Long id) { return estudianteRepository.findById(id).orElseThrow(()->new EstudianteNoEncontradoException(id)); }
    @Override @Transactional(readOnly=true) public Page<Estudiante> listar(String q, Boolean activo, Pageable p) { return estudianteRepository.buscar(q==null||q.isBlank()?null:q.trim(),activo,p); }
    @Override public Estudiante actualizar(Long id, Estudiante e) {
        Estudiante a=obtenerPorId(id);
        if(estudianteRepository.existsByCodigoEstudiantilAndIdNot(e.getCodigoEstudiantil(),id)) throw new IllegalArgumentException("Codigo estudiantil duplicado");
        if(estudianteRepository.existsByNumeroIdentificacionAndIdNot(e.getNumeroIdentificacion(),id)) throw new IllegalArgumentException("Identificacion duplicada");
        a.setCodigoEstudiantil(e.getCodigoEstudiantil()); a.setNumeroIdentificacion(e.getNumeroIdentificacion());
        a.setFechaNacimiento(e.getFechaNacimiento());
        a.setTelefono(e.getTelefono()); a.setDireccion(e.getDireccion()); return estudianteRepository.save(a);
    }
    @Override public Estudiante cambiarEstado(Long id,Boolean activo) { Estudiante e=obtenerPorId(id);e.setActivo(activo);return estudianteRepository.save(e); }
    @Override @Transactional(readOnly=true) public List<Estudiante> obtenerActivos(){return estudianteRepository.findSeleccionables();}
    @Override @Transactional(readOnly=true) public List<String> obtenerCorreosActivos(){return estudianteRepository.findSeleccionables().stream().map(Estudiante::getCorreo).toList();}
    @Override public Estudiante obtenerResumenPorId(Long id){return obtenerPorId(id);}
    @Override public Object obtenerHistorialAcademico(Long id){throw new UnsupportedOperationException("El historial academico aun no esta disponible");}
}
