package com.umg.sgau.academico.service;

import com.umg.sgau.docente.repository.DocenteRepository;
import com.umg.sgau.estudiante.repository.EstudianteRepository;
import com.umg.sgau.rol.entity.Rol;
import com.umg.sgau.rol.repository.RolRepository;
import com.umg.sgau.usuario.entity.Usuario;
import com.umg.sgau.usuario.repository.UsuarioRepository;
import com.umg.sgau.usuario.service.UsuarioService;
import java.util.HashSet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Reglas comunes para crear o vincular una cuenta a un perfil academico. */
@Service
public class AcademicAccountLinkService {
    private final UsuarioRepository usuarios;
    private final UsuarioService usuarioService;
    private final RolRepository roles;
    private final DocenteRepository docentes;
    private final EstudianteRepository estudiantes;

    public AcademicAccountLinkService(UsuarioRepository usuarios, UsuarioService usuarioService,
            RolRepository roles, DocenteRepository docentes, EstudianteRepository estudiantes) {
        this.usuarios = usuarios;
        this.usuarioService = usuarioService;
        this.roles = roles;
        this.docentes = docentes;
        this.estudiantes = estudiantes;
    }

    @Transactional
    public Usuario resolver(Long usuarioId, Boolean accesoApp, String username, String password,
            String email, String nombre, String apellido, String codigoRol) {
        if (usuarioId == null && !Boolean.TRUE.equals(accesoApp)) return null;

        Usuario usuario = usuarioId == null
                ? usuarios.findByEmailIgnoreCase(email).map(u -> cargar(u.getId())).orElse(null)
                : cargar(usuarioId);

        if (usuario == null) {
            exigirTexto(username, "El username es obligatorio para crear el acceso a la app");
            exigirTexto(password, "La contrasena es obligatoria para crear el acceso a la app");
            Rol rol = rolActivo(codigoRol);
            usuario = new Usuario();
            usuario.setUsername(username.trim());
            usuario.setPassword(password);
            usuario.setEmail(email.trim());
            usuario.setNombre(nombre.trim());
            usuario.setApellido(apellido.trim());
            usuario.setActivo(true);
            usuario.setRoles(new HashSet<>());
            usuario.getRoles().add(rol);
            usuario = usuarioService.crear(usuario);
        }

        boolean ocupado = "DOCENTE".equals(codigoRol)
                ? docentes.existsByUsuarioId(usuario.getId())
                : estudiantes.existsByUsuarioId(usuario.getId());
        if (ocupado) throw new IllegalArgumentException("El usuario ya tiene un perfil " + codigoRol.toLowerCase());
        AcademicIdentityPolicy.validar(usuario, nombre, apellido, email);
        validarRol(usuario, codigoRol);
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new IllegalArgumentException("El usuario vinculado esta inactivo");
        }
        return usuario;
    }

    private Usuario cargar(Long id) {
        return usuarios.findWithRolesById(id)
                .orElseThrow(() -> new IllegalArgumentException("El usuario indicado no existe"));
    }

    private Rol rolActivo(String codigo) {
        Rol rol = roles.findByCodigoIgnoreCase(codigo)
                .orElseThrow(() -> new IllegalStateException("No existe el rol requerido: " + codigo));
        if (!Boolean.TRUE.equals(rol.getActivo()))
            throw new IllegalStateException("El rol requerido esta inactivo: " + codigo);
        return rol;
    }

    private void validarRol(Usuario usuario, String codigo) {
        boolean valido = usuario.getRoles().stream().anyMatch(r -> Boolean.TRUE.equals(r.getActivo())
                && codigo.equalsIgnoreCase(r.getCodigo()));
        if (!valido) throw new IllegalArgumentException("El usuario debe tener el rol " + codigo);
    }

    private void exigirTexto(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) throw new IllegalArgumentException(mensaje);
    }
}
