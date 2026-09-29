package com.umg.sgau.academico.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.umg.sgau.auth.dto.LoginRequestDTO;
import com.umg.sgau.auth.service.AuthService;
import com.umg.sgau.docente.entity.Docente;
import com.umg.sgau.docente.repository.DocenteRepository;
import com.umg.sgau.docente.service.DocenteService;
import com.umg.sgau.estudiante.entity.Estudiante;
import com.umg.sgau.estudiante.repository.EstudianteRepository;
import com.umg.sgau.estudiante.service.EstudianteService;
import com.umg.sgau.rol.entity.Rol;
import com.umg.sgau.rol.repository.RolRepository;
import com.umg.sgau.usuario.entity.Usuario;
import com.umg.sgau.usuario.repository.UsuarioRepository;
import java.time.LocalDate;
import java.util.HashSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class AcademicAccountLinkIntegrationTest {
    @org.springframework.beans.factory.annotation.Autowired DocenteService docentes;
    @org.springframework.beans.factory.annotation.Autowired EstudianteService estudiantes;
    @org.springframework.beans.factory.annotation.Autowired DocenteRepository docenteRepository;
    @org.springframework.beans.factory.annotation.Autowired EstudianteRepository estudianteRepository;
    @org.springframework.beans.factory.annotation.Autowired UsuarioRepository usuarios;
    @org.springframework.beans.factory.annotation.Autowired RolRepository roles;
    @org.springframework.beans.factory.annotation.Autowired AuthService auth;
    Rol rolDocente; Rol rolEstudiante;

    @BeforeEach void roles() {
        rolDocente=rol("DOCENTE"); rolEstudiante=rol("ESTUDIANTE");
    }

    @Test void creaCuentaPerfilSeleccionableEIniciaSesion() {
        Docente d=docente("DOC-LINK-1","doc.link.1@sgau.test");
        Docente creado=docentes.crearOVincular(d,null,true,"doc_link_1","Password123*!");
        assertThat(creado.getUsuario()).isNotNull();
        assertThat(docentes.obtenerActivos()).extracting(Docente::getId).contains(creado.getId());
        var login=new LoginRequestDTO(); login.setUsername("doc_link_1"); login.setPassword("Password123*!");
        assertThat(auth.login(login).getDocenteId()).isEqualTo(creado.getId());
        assertThat(auth.login(login).getAccessToken()).isNotBlank();
    }

    @Test void vinculaCuentaExistenteSinDuplicarYRechazaRolIncorrecto() {
        Usuario correcto=usuario("student_link_1","student.link.1@sgau.test","Luis","Perez",rolEstudiante);
        Estudiante e=estudiante("EST-LINK-1","ID-LINK-1",correcto.getEmail());
        Estudiante creado=estudiantes.crearOVincular(e,correcto.getId(),true,null,null);
        Estudiante repetido=estudiantes.crearOVincular(e,correcto.getId(),true,null,null);
        assertThat(repetido.getId()).isEqualTo(creado.getId());
        assertThat(estudianteRepository.count()).isGreaterThanOrEqualTo(1);
        assertThat(estudiantes.obtenerActivos()).extracting(Estudiante::getId).contains(creado.getId());

        Usuario incorrecto=usuario("wrong_role_1","wrong.role.1@sgau.test","Luis","Perez",rolDocente);
        assertThatThrownBy(() -> estudiantes.crearOVincular(
                estudiante("EST-LINK-2","ID-LINK-2",incorrecto.getEmail()),incorrecto.getId(),true,null,null))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("ESTUDIANTE");
    }

    @Test void unaCuentaNoPuedeTenerDosPerfilesDelMismoTipo() {
        Usuario u=usuario("doc_unique_1","doc.unique.1@sgau.test","Ana","Lopez",rolDocente);
        docentes.crearOVincular(docente("DOC-UNIQUE-1",u.getEmail()),u.getId(),true,null,null);
        assertThatThrownBy(() -> docentes.crearOVincular(
                docente("DOC-UNIQUE-2","doc.unique.2@sgau.test"),u.getId(),true,null,null))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("ya tiene un perfil");
    }

    @Test void rechazaMariaLopezVinculadaConPerfilDiegoChavezAunqueCoincidaCorreo() {
        Usuario maria=usuario("maria_lopez","identidad@sgau.test","Maria","Lopez",rolDocente);
        Docente diego=Docente.builder().codigoDocente("DOC-ID-1").nombre("Diego").apellido("Chavez")
                .email(maria.getEmail()).especialidad("Fisica").build();
        assertThatThrownBy(() -> docentes.crearOVincular(diego,maria.getId(),true,null,null))
                .isInstanceOf(com.umg.sgau.academico.exception.IdentidadAcademicaInconsistenteException.class)
                .hasMessageContaining("nombre");
        assertThat(docenteRepository.findByUsuarioId(maria.getId())).isEmpty();
    }

    @Test void actualizacionDeUsuarioSincronizaDatosCompartidosDelPerfilVinculado() {
        Usuario maria=usuario("maria_sync","maria.sync@sgau.test","Maria","Lopez",rolDocente);
        Docente perfil=docentes.crearOVincular(Docente.builder().codigoDocente("DOC-SYNC-1").nombre("Maria")
                .apellido("Lopez").email(maria.getEmail()).especialidad("Quimica").build(),maria.getId(),true,null,null);
        var cambio=new com.umg.sgau.auth.dto.PerfilUpdateRequestDTO();
        cambio.setUsername("maria_sync"); cambio.setEmail("maria.nueva@sgau.test");
        cambio.setNombre("Maria Elena"); cambio.setApellido("Lopez");
        auth.actualizarPerfil("maria_sync",cambio);
        Docente actualizado=docenteRepository.findById(perfil.getId()).orElseThrow();
        assertThat(actualizado.getNombre()).isEqualTo("Maria Elena");
        assertThat(actualizado.getApellido()).isEqualTo("Lopez");
        assertThat(actualizado.getEmail()).isEqualTo("maria.nueva@sgau.test");
        assertThat(actualizado.getEspecialidad()).isEqualTo("Quimica");
    }

    @Test void actualizacionAcademicaNoPuedeCambiarLaIdentidadDelUsuario() {
        Usuario maria=usuario("maria_update","maria.update@sgau.test","Maria","Lopez",rolDocente);
        Docente perfil=docentes.crearOVincular(Docente.builder().codigoDocente("DOC-UPD-1").nombre("Maria")
                .apellido("Lopez").email(maria.getEmail()).especialidad("Historia").build(),maria.getId(),true,null,null);
        Docente cambios=Docente.builder().codigoDocente("DOC-UPD-1").nombre("Diego").apellido("Chavez")
                .email(maria.getEmail()).especialidad("Historia").build();
        assertThatThrownBy(() -> docentes.actualizar(perfil.getId(),cambios))
                .isInstanceOf(com.umg.sgau.academico.exception.IdentidadAcademicaInconsistenteException.class);
    }

    private Rol rol(String codigo) {
        return roles.findByCodigoIgnoreCase(codigo).orElseGet(() -> roles.save(Rol.builder()
                .codigo(codigo).nombre("Rol "+codigo).activo(true).permisos(new HashSet<>()).build()));
    }
    private Usuario usuario(String username,String email,String nombre,String apellido,Rol rol) {
        Usuario u=new Usuario();u.setUsername(username);u.setEmail(email);u.setNombre(nombre);u.setApellido(apellido);
        u.setPassword("$2a$10$123456789012345678901u12345678901234567890123456789012");u.setActivo(true);
        u.setRoles(new HashSet<>());u.getRoles().add(rol);return usuarios.save(u);
    }
    private Docente docente(String codigo,String email) { return Docente.builder().codigoDocente(codigo).nombre("Ana").apellido("Lopez").email(email).especialidad("Matematica").build(); }
    private Estudiante estudiante(String codigo,String id,String email) { return Estudiante.builder().codigoEstudiantil(codigo).numeroIdentificacion(id).nombres("Luis").apellidos("Perez").fechaNacimiento(LocalDate.of(2000,1,1)).correo(email).build(); }
}
