package com.umg.sgau.usuario.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.umg.sgau.auth.dto.LoginRequestDTO;
import com.umg.sgau.auth.dto.LoginResponseDTO;
import com.umg.sgau.auth.service.AuthService;
import com.umg.sgau.docente.repository.DocenteRepository;
import com.umg.sgau.estudiante.repository.EstudianteRepository;
import com.umg.sgau.rol.entity.Rol;
import com.umg.sgau.rol.repository.RolRepository;
import com.umg.sgau.usuario.dto.AltaUsuarioDocenteDTO;
import com.umg.sgau.usuario.dto.AltaUsuarioEstudianteDTO;
import com.umg.sgau.usuario.dto.AltaUsuarioRequestDTO;
import com.umg.sgau.usuario.exception.AltaUsuarioValidationException;
import com.umg.sgau.usuario.repository.UsuarioRepository;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class AltaUsuarioServiceIntegrationTest {
    @Autowired AltaUsuarioService altas;
    @Autowired UsuarioRepository usuarios;
    @Autowired RolRepository roles;
    @Autowired DocenteRepository docentes;
    @Autowired EstudianteRepository estudiantes;
    @Autowired AuthService auth;

    private Rol docenteRol;
    private Rol estudianteRol;
    private Rol adminRol;

    @BeforeEach
    void prepararRoles() {
        docenteRol = rol("DOCENTE");
        estudianteRol = rol("ESTUDIANTE");
        adminRol = rol("ADMIN_TEST_ALTA");
    }

    @Test
    void creaSoloCuentaCuandoNoHayRolAcademico() {
        var resultado = altas.crear(base("solo_cuenta", "solo.cuenta@sgau.test", Set.of(adminRol.getId())));
        assertThat(resultado.getUsuarioId()).isNotNull();
        assertThat(resultado.getRoles()).extracting("id").containsExactly(adminRol.getId());
        assertThat(usuarios.findWithRolesById(resultado.getUsuarioId()).orElseThrow().getRoles())
                .extracting(Rol::getId).containsExactly(adminRol.getId());
        assertThat(login("solo_cuenta").getRoles()).containsExactly("ADMIN_TEST_ALTA");
        assertThat(resultado.getDocenteId()).isNull();
        assertThat(resultado.getEstudianteId()).isNull();
    }

    @Test
    void creaCuentaRolYPerfilDocente() {
        AltaUsuarioRequestDTO request = base("alta_docente", "alta.docente@sgau.test", Set.of(docenteRol.getId()));
        request.setDocente(docente("DOC-ALTA-1"));
        var resultado = altas.crear(request);
        assertThat(resultado.getDocenteId()).isNotNull();
        assertThat(docentes.findById(resultado.getDocenteId()).orElseThrow().getUsuario().getId())
                .isEqualTo(resultado.getUsuarioId());
        assertThat(usuarios.findWithRolesById(resultado.getUsuarioId()).orElseThrow().getRoles())
                .extracting(Rol::getCodigo).containsExactly("DOCENTE");
        assertThat(login("alta_docente").getRoles()).containsExactly("DOCENTE");
    }

    @Test
    void creaCuentaRolYPerfilEstudiante() {
        AltaUsuarioRequestDTO request = base("alta_estudiante", "alta.estudiante@sgau.test", Set.of(estudianteRol.getId()));
        request.setEstudiante(estudiante("EST-ALTA-1", "ID-ALTA-1"));
        var resultado = altas.crear(request);
        assertThat(resultado.getEstudianteId()).isNotNull();
        assertThat(estudiantes.findById(resultado.getEstudianteId()).orElseThrow().getUsuario().getId())
                .isEqualTo(resultado.getUsuarioId());
        assertThat(usuarios.findWithRolesById(resultado.getUsuarioId()).orElseThrow().getRoles())
                .extracting(Rol::getCodigo).containsExactly("ESTUDIANTE");
        assertThat(login("alta_estudiante").getRoles()).containsExactly("ESTUDIANTE");
    }

    @Test
    void soportaAmbosPerfilesEnLaMismaTransaccion() {
        AltaUsuarioRequestDTO request = base("alta_ambos", "alta.ambos@sgau.test",
                Set.of(docenteRol.getId(), estudianteRol.getId()));
        request.setDocente(docente("DOC-ALTA-2"));
        request.setEstudiante(estudiante("EST-ALTA-2", "ID-ALTA-2"));
        var resultado = altas.crear(request);
        assertThat(resultado.getDocenteId()).isNotNull();
        assertThat(resultado.getEstudianteId()).isNotNull();
    }

    @Test
    void falloDocenteNoDejaUsuarioNiRolesParciales() {
        AltaUsuarioRequestDTO primero = base("doc_existente", "doc.existente@sgau.test", Set.of(docenteRol.getId()));
        primero.setDocente(docente("DOC-DUPLICADO"));
        altas.crear(primero);
        long usuariosAntes = usuarios.count();

        AltaUsuarioRequestDTO duplicado = base("doc_fallido", "doc.fallido@sgau.test", Set.of(docenteRol.getId()));
        duplicado.setDocente(docente("DOC-DUPLICADO"));
        assertThatThrownBy(() -> altas.crear(duplicado)).isInstanceOf(AltaUsuarioValidationException.class);

        assertThat(usuarios.count()).isEqualTo(usuariosAntes);
        assertThat(usuarios.findByUsername("doc_fallido")).isEmpty();
    }

    @Test
    void falloEstudianteNoDejaUsuarioNiRolesParciales() {
        AltaUsuarioRequestDTO primero = base("est_existente", "est.existente@sgau.test", Set.of(estudianteRol.getId()));
        primero.setEstudiante(estudiante("EST-DUPLICADO", "ID-DUPLICADO"));
        altas.crear(primero);
        long usuariosAntes = usuarios.count();

        AltaUsuarioRequestDTO duplicado = base("est_fallido", "est.fallido@sgau.test", Set.of(estudianteRol.getId()));
        duplicado.setEstudiante(estudiante("EST-DUPLICADO", "ID-OTRO"));
        assertThatThrownBy(() -> altas.crear(duplicado)).isInstanceOf(AltaUsuarioValidationException.class);

        assertThat(usuarios.count()).isEqualTo(usuariosAntes);
        assertThat(usuarios.findByUsername("est_fallido")).isEmpty();
    }

    @Test
    void rolInexistenteNoDejaUsuarioParcial() {
        long usuariosAntes = usuarios.count();
        AltaUsuarioRequestDTO request = base("rol_inexistente", "rol.inexistente@sgau.test",
                Set.of(Long.MAX_VALUE));

        assertThatThrownBy(() -> altas.crear(request)).isInstanceOf(AltaUsuarioValidationException.class);

        assertThat(usuarios.count()).isEqualTo(usuariosAntes);
        assertThat(usuarios.findByUsername("rol_inexistente")).isEmpty();
    }

    @Test
    void rolInactivoNoDejaUsuarioParcial() {
        Rol inactivo = rol("INACTIVO_TEST_ALTA");
        inactivo.setActivo(false);
        roles.save(inactivo);
        long usuariosAntes = usuarios.count();
        AltaUsuarioRequestDTO request = base("rol_inactivo", "rol.inactivo@sgau.test", Set.of(inactivo.getId()));

        assertThatThrownBy(() -> altas.crear(request)).isInstanceOf(AltaUsuarioValidationException.class);

        assertThat(usuarios.count()).isEqualTo(usuariosAntes);
        assertThat(usuarios.findByUsername("rol_inactivo")).isEmpty();
    }

    @Test
    void listaVaciaNoDejaUsuarioParcial() {
        long usuariosAntes = usuarios.count();
        AltaUsuarioRequestDTO request = base("sin_rol", "sin.rol@sgau.test", Set.of());

        assertThatThrownBy(() -> altas.crear(request)).isInstanceOf(AltaUsuarioValidationException.class);

        assertThat(usuarios.count()).isEqualTo(usuariosAntes);
        assertThat(usuarios.findByUsername("sin_rol")).isEmpty();
    }

    private Rol rol(String codigo) {
        return roles.findByCodigoIgnoreCase(codigo).orElseGet(() -> roles.save(Rol.builder()
                .codigo(codigo).nombre("Rol " + codigo).activo(true).permisos(new HashSet<>()).build()));
    }

    private AltaUsuarioRequestDTO base(String username, String correo, Set<Long> rolIds) {
        AltaUsuarioRequestDTO dto = new AltaUsuarioRequestDTO();
        dto.setUsername(username); dto.setPassword("Password123*"); dto.setNombre("Ana");
        dto.setApellido("Perez"); dto.setCorreo(correo); dto.setRolIds(rolIds);
        return dto;
    }

    private AltaUsuarioDocenteDTO docente(String codigo) {
        AltaUsuarioDocenteDTO dto = new AltaUsuarioDocenteDTO();
        dto.setCodigoDocente(codigo); dto.setEspecialidad("Matematica"); return dto;
    }

    private AltaUsuarioEstudianteDTO estudiante(String codigo, String identificacion) {
        AltaUsuarioEstudianteDTO dto = new AltaUsuarioEstudianteDTO();
        dto.setCodigoEstudiantil(codigo); dto.setNumeroIdentificacion(identificacion);
        dto.setFechaNacimiento(LocalDate.of(2000, 1, 1)); return dto;
    }

    private LoginResponseDTO login(String username) {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setUsername(username);
        request.setPassword("Password123*");
        return auth.login(request);
    }
}
