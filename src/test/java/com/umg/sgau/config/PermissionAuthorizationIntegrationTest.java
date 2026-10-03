package com.umg.sgau.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umg.sgau.permiso.entity.Permiso;
import com.umg.sgau.permiso.repository.PermisoRepository;
import com.umg.sgau.rol.entity.Rol;
import com.umg.sgau.rol.repository.RolRepository;
import com.umg.sgau.usuario.entity.Usuario;
import com.umg.sgau.usuario.repository.UsuarioRepository;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "jwt.secret=clave-de-pruebas-segura-de-al-menos-32-bytes")
@AutoConfigureMockMvc
@Transactional
class PermissionAuthorizationIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private RolRepository rolRepository;
    @Autowired private PermisoRepository permisoRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private PermissionCatalogInitializer initializer;

    @Test
    void permisosControlanCadaSolicitudYSeExponenEnLaSesion() throws Exception {
        Permiso leer = permisoRepository.findByCodigoIgnoreCase(PermissionCatalog.USUARIOS_LEER).orElseThrow();
        Rol lectorA = guardarRol("LECTOR_A", Set.of(leer));
        Rol lectorB = guardarRol("LECTOR_B", Set.of(leer));
        guardarUsuario("lector_permisos", Set.of(lectorA, lectorB));
        guardarUsuario("sin_permisos", Set.of(guardarRol("VACIO", Set.of())));

        Rol admin = guardarRol("ADMIN", Set.of());
        guardarUsuario("admin_permisos", Set.of(admin));
        initializer.run(null);

        String tokenLector = login("lector_permisos")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permisos", hasSize(1)))
                .andExpect(jsonPath("$.permisos", hasItem(PermissionCatalog.USUARIOS_LEER)))
                .andReturn().getResponse().getContentAsString();
        tokenLector = objectMapper.readTree(tokenLector).get("accessToken").asText();

        mockMvc.perform(get("/api/usuarios").header("Authorization", "Bearer " + tokenLector))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/usuarios")
                        .header("Authorization", "Bearer " + tokenLector)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());

        String tokenSinPermisos = token(login("sin_permisos").andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
        mockMvc.perform(get("/api/usuarios").header("Authorization", "Bearer " + tokenSinPermisos))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isUnauthorized());

        String tokenAdmin = token(login("admin_permisos").andExpect(status().isOk())
                .andExpect(jsonPath("$.permisos", hasItem(PermissionCatalog.USUARIOS_CREAR)))
                .andReturn().getResponse().getContentAsString());
        mockMvc.perform(get("/api/usuarios").header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + tokenLector))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permisos", hasSize(1)))
                .andExpect(jsonPath("$.permisos", hasItem(PermissionCatalog.USUARIOS_LEER)));
    }

    @Test
    void altaConjuntaExigeCrearUsuarioYAsignarRoles() throws Exception {
        Permiso crear = permisoRepository.findByCodigoIgnoreCase(PermissionCatalog.USUARIOS_CREAR).orElseThrow();
        Permiso asignar = permisoRepository.findByCodigoIgnoreCase(PermissionCatalog.USUARIOS_ASIGNAR_ROLES).orElseThrow();
        Rol destino = guardarRol("DESTINO_ALTA_SEGURA", Set.of());
        guardarUsuario("solo_crea", Set.of(guardarRol("SOLO_CREA", Set.of(crear))));
        guardarUsuario("crea_y_asigna", Set.of(guardarRol("CREA_Y_ASIGNA", Set.of(crear, asignar))));

        String body = """
                {"username":"alta_segura","password":"Usuario123*","nombre":"Alta",
                 "apellido":"Segura","correo":"alta.segura@sgau.test","rolIds":[%d]}
                """.formatted(destino.getId());

        String tokenSoloCrea = token(login("solo_crea").andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
        mockMvc.perform(post("/api/usuarios/alta-conjunta")
                        .header("Authorization", "Bearer " + tokenSoloCrea)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        assertThat(usuarioRepository.findByUsername("alta_segura")).isEmpty();

        String tokenCompleto = token(login("crea_y_asigna").andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
        mockMvc.perform(post("/api/usuarios/alta-conjunta")
                        .header("Authorization", "Bearer " + tokenCompleto)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roles", hasSize(1)))
                .andExpect(jsonPath("$.roles[0].id").value(destino.getId()));
    }

    @Test
    void inicializadorNoSobrescribePermisosAdministradosDeEstudiante() throws Exception {
        Permiso permisoElegido = permisoRepository.findByCodigoIgnoreCase(PermissionCatalog.NOTAS_LEER).orElseThrow();
        Rol estudiante = rolRepository.findByCodigoIgnoreCase("ESTUDIANTE").orElseGet(() -> {
            Rol nuevo = new Rol();
            nuevo.setCodigo("ESTUDIANTE");
            nuevo.setNombre("Estudiante");
            nuevo.setActivo(true);
            return rolRepository.save(nuevo);
        });
        estudiante.setPermisos(new HashSet<>(Set.of(permisoElegido)));
        rolRepository.saveAndFlush(estudiante);

        initializer.run(null);

        Rol recargado = rolRepository.findWithPermisosById(estudiante.getId()).orElseThrow();
        assertThat(recargado.getPermisos()).extracting(Permiso::getCodigo)
                .containsExactly(PermissionCatalog.NOTAS_LEER);
    }

    private org.springframework.test.web.servlet.ResultActions login(String username) throws Exception {
        return mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + username + "\",\"password\":\"Usuario123*\"}"));
    }

    private String token(String json) throws Exception {
        JsonNode body = objectMapper.readTree(json);
        return body.get("accessToken").asText();
    }

    private Rol guardarRol(String codigo, Set<Permiso> permisos) {
        Rol rol = new Rol();
        rol.setCodigo(codigo);
        rol.setNombre("Rol " + codigo);
        rol.setActivo(true);
        rol.setPermisos(new HashSet<>(permisos));
        return rolRepository.save(rol);
    }

    private void guardarUsuario(String username, Set<Rol> roles) {
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPassword(passwordEncoder.encode("Usuario123*"));
        usuario.setEmail(username + "@sgau.test");
        usuario.setNombre("Usuario");
        usuario.setApellido("Prueba");
        usuario.setActivo(true);
        usuario.setRoles(new HashSet<>(roles));
        usuarioRepository.save(usuario);
    }
}
