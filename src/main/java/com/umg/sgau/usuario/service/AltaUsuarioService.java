package com.umg.sgau.usuario.service;

import com.umg.sgau.docente.entity.Docente;
import com.umg.sgau.docente.repository.DocenteRepository;
import com.umg.sgau.estudiante.entity.Estudiante;
import com.umg.sgau.estudiante.repository.EstudianteRepository;
import com.umg.sgau.rol.entity.Rol;
import com.umg.sgau.rol.mapper.RolMapper;
import com.umg.sgau.rol.repository.RolRepository;
import com.umg.sgau.usuario.dto.AltaUsuarioRequestDTO;
import com.umg.sgau.usuario.dto.AltaUsuarioResponseDTO;
import com.umg.sgau.usuario.entity.Usuario;
import com.umg.sgau.usuario.exception.AltaUsuarioValidationException;
import com.umg.sgau.usuario.repository.UsuarioRepository;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AltaUsuarioService {
    private final UsuarioRepository usuarios;
    private final RolRepository roles;
    private final DocenteRepository docentes;
    private final EstudianteRepository estudiantes;
    private final PasswordEncoder passwordEncoder;

    public AltaUsuarioService(UsuarioRepository usuarios, RolRepository roles,
            DocenteRepository docentes, EstudianteRepository estudiantes, PasswordEncoder passwordEncoder) {
        this.usuarios = usuarios;
        this.roles = roles;
        this.docentes = docentes;
        this.estudiantes = estudiantes;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AltaUsuarioResponseDTO crear(AltaUsuarioRequestDTO request) {
        Map<String, String> errores = new LinkedHashMap<>();
        List<Rol> rolesSeleccionados = cargarYValidarRoles(request.getRolIds(), errores);
        Set<String> codigos = rolesSeleccionados.stream()
                .map(Rol::getCodigo).map(c -> c.toUpperCase(Locale.ROOT)).collect(Collectors.toSet());

        validarPerfilesRequeridos(request, codigos, errores);
        validarUnicidades(request, errores);
        if (!errores.isEmpty()) throw new AltaUsuarioValidationException(errores);

        Usuario usuario = new Usuario();
        usuario.setUsername(request.getUsername().trim());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setNombre(request.getNombre().trim());
        usuario.setApellido(request.getApellido().trim());
        usuario.setEmail(request.getCorreo().trim().toLowerCase(Locale.ROOT));
        usuario.setActivo(true);
        usuario.setRoles(new HashSet<>(rolesSeleccionados));
        usuario = usuarios.save(usuario);

        Long docenteId = null;
        if (codigos.contains("DOCENTE")) {
            var datos = request.getDocente();
            Docente docente = Docente.builder()
                    .codigoDocente(datos.getCodigoDocente().trim())
                    .telefono(limpiar(datos.getTelefono()))
                    .especialidad(limpiar(datos.getEspecialidad()))
                    .activo(true).usuario(usuario).build();
            docenteId = docentes.save(docente).getId();
        }

        Long estudianteId = null;
        if (codigos.contains("ESTUDIANTE")) {
            var datos = request.getEstudiante();
            Estudiante estudiante = Estudiante.builder()
                    .codigoEstudiantil(datos.getCodigoEstudiantil().trim())
                    .numeroIdentificacion(datos.getNumeroIdentificacion().trim())
                    .fechaNacimiento(datos.getFechaNacimiento())
                    .telefono(limpiar(datos.getTelefono()))
                    .direccion(limpiar(datos.getDireccion()))
                    .activo(true).usuario(usuario).build();
            estudianteId = estudiantes.save(estudiante).getId();
        }

        return AltaUsuarioResponseDTO.builder()
                .usuarioId(usuario.getId())
                .roles(rolesSeleccionados.stream().map(RolMapper::aSummaryDTO).collect(Collectors.toSet()))
                .docenteId(docenteId).estudianteId(estudianteId).build();
    }

    private List<Rol> cargarYValidarRoles(Set<Long> ids, Map<String, String> errores) {
        if (ids == null || ids.isEmpty()) return List.of();
        if (ids.stream().anyMatch(Objects::isNull)) {
            errores.put("rolIds", "Los IDs de roles no pueden ser nulos");
            return List.of();
        }
        List<Rol> encontrados = roles.findAllById(ids);
        if (encontrados.size() != ids.size()) errores.put("rolIds", "Uno o mas roles no existen");
        if (encontrados.stream().anyMatch(r -> !Boolean.TRUE.equals(r.getActivo())))
            errores.put("rolIds", "Todos los roles seleccionados deben estar activos");
        return encontrados;
    }

    private void validarPerfilesRequeridos(AltaUsuarioRequestDTO r, Set<String> codigos, Map<String, String> errores) {
        if (codigos.contains("DOCENTE") && r.getDocente() == null)
            errores.put("docente", "Los datos de docente son obligatorios para el rol DOCENTE");
        if (!codigos.contains("DOCENTE") && r.getDocente() != null)
            errores.put("docente", "No envie datos de docente sin seleccionar el rol DOCENTE");
        if (codigos.contains("ESTUDIANTE") && r.getEstudiante() == null)
            errores.put("estudiante", "Los datos de estudiante son obligatorios para el rol ESTUDIANTE");
        if (!codigos.contains("ESTUDIANTE") && r.getEstudiante() != null)
            errores.put("estudiante", "No envie datos de estudiante sin seleccionar el rol ESTUDIANTE");
    }

    private void validarUnicidades(AltaUsuarioRequestDTO r, Map<String, String> errores) {
        if (r.getUsername() != null && usuarios.existsByUsernameIgnoreCase(r.getUsername().trim()))
            errores.put("username", "El username ya esta registrado");
        if (r.getCorreo() != null && usuarios.existsByEmailIgnoreCase(r.getCorreo().trim()))
            errores.put("correo", "El correo ya esta registrado; use la operacion de vinculacion para una cuenta existente");
        if (r.getDocente() != null && r.getDocente().getCodigoDocente() != null
                && docentes.existsByCodigoDocente(r.getDocente().getCodigoDocente().trim()))
            errores.put("docente.codigoDocente", "El codigo docente ya esta registrado");
        if (r.getEstudiante() != null) {
            if (r.getEstudiante().getCodigoEstudiantil() != null
                    && estudiantes.existsByCodigoEstudiantil(r.getEstudiante().getCodigoEstudiantil().trim()))
                errores.put("estudiante.codigoEstudiantil", "El codigo estudiantil ya esta registrado");
            if (r.getEstudiante().getNumeroIdentificacion() != null
                    && estudiantes.existsByNumeroIdentificacion(r.getEstudiante().getNumeroIdentificacion().trim()))
                errores.put("estudiante.numeroIdentificacion", "El numero de identificacion ya esta registrado");
        }
    }

    private String limpiar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
