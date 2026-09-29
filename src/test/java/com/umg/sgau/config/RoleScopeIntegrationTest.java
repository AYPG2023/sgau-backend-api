package com.umg.sgau.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umg.sgau.carrera.entity.Carrera;
import com.umg.sgau.carrera.repository.CarreraRepository;
import com.umg.sgau.colegiatura.entity.Colegiatura;
import com.umg.sgau.colegiatura.repository.ColegiaturaRepository;
import com.umg.sgau.curso.entity.Curso;
import com.umg.sgau.curso.repository.CursoRepository;
import com.umg.sgau.docente.entity.Docente;
import com.umg.sgau.docente.repository.DocenteRepository;
import com.umg.sgau.estudiante.entity.Estudiante;
import com.umg.sgau.estudiante.repository.EstudianteRepository;
import com.umg.sgau.inscripcion.entity.Inscripcion;
import com.umg.sgau.inscripcion.repository.InscripcionRepository;
import com.umg.sgau.nota.entity.Nota;
import com.umg.sgau.nota.repository.NotaRepository;
import com.umg.sgau.rol.entity.Rol;
import com.umg.sgau.rol.repository.RolRepository;
import com.umg.sgau.usuario.entity.Usuario;
import com.umg.sgau.usuario.repository.UsuarioRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "jwt.secret=clave-de-pruebas-segura-de-al-menos-32-bytes")
@AutoConfigureMockMvc
class RoleScopeIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired RolRepository rolRepository;
    @Autowired EstudianteRepository estudianteRepository;
    @Autowired DocenteRepository docenteRepository;
    @Autowired CarreraRepository carreraRepository;
    @Autowired CursoRepository cursoRepository;
    @Autowired InscripcionRepository inscripcionRepository;
    @Autowired NotaRepository notaRepository;
    @Autowired ColegiaturaRepository colegiaturaRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired PermissionCatalogInitializer initializer;

    @Test
    void matrizDeRolesYAlcanceImpidenAccesoHorizontal() throws Exception {
        Rol estudianteRol = rol("ESTUDIANTE");
        Rol docenteRol = rol("DOCENTE");
        Rol adminRol = rol("ADMIN");
        initializer.run(null);

        Estudiante propio = estudiante("E001", "estudiante@sgau.test");
        Estudiante ajeno = estudiante("E002", "otro@sgau.test");
        Docente docente = docente("D001", "docente@sgau.test");
        Docente otroDocente = docente("D002", "otrodocente@sgau.test");
        Carrera carrera = carreraRepository.save(Carrera.builder().codigo("SIS").nombre("Sistemas")
                .duracionAnios(5).activo(true).build());
        Curso propioDocente = curso("CUR1", carrera, docente);
        Curso ajenoDocente = curso("CUR2", carrera, otroDocente);
        inscripcionRepository.save(Inscripcion.builder().estudiante(propio).carrera(carrera).curso(propioDocente)
                .grado("1").seccion("A").cicloAnio(2026).fechaInscripcion(LocalDate.now()).activo(true).build());
        Nota notaPropia = nota(propio, propioDocente, "PARCIAL");
        Nota notaAjena = nota(ajeno, ajenoDocente, "FINAL");
        Colegiatura cuotaPropia = cuota(propio, "Cuota propia");
        Colegiatura cuotaAjena = cuota(ajeno, "Cuota ajena");

        usuario("estudiante_scope", propio.getCorreo(), estudianteRol);
        usuario("docente_scope", docente.getEmail(), docenteRol);
        usuario("admin_scope", "admin@sgau.test", adminRol);

        String estudianteToken = login("estudiante_scope");
        autorizado(get("/api/academico/estudiante/me"), estudianteToken, 200);
        autorizado(get("/api/academico/estudiante/me/inscripciones"), estudianteToken, 200);
        autorizado(get("/api/academico/estudiante/me/cursos"), estudianteToken, 200);
        autorizado(get("/api/academico/estudiante/me/notas"), estudianteToken, 200);
        autorizado(get("/api/academico/estudiante/me/colegiaturas"), estudianteToken, 200);
        autorizado(get("/api/auditoria"), estudianteToken, 403);
        autorizado(get("/api/notas/" + notaPropia.getId()), estudianteToken, 200);
        autorizado(get("/api/colegiaturas/" + cuotaPropia.getId()), estudianteToken, 200);
        autorizado(get("/api/cursos/" + propioDocente.getId()), estudianteToken, 200);
        autorizado(get("/api/usuarios"), estudianteToken, 403);
        autorizado(get("/api/notas/" + notaAjena.getId()), estudianteToken, 403);
        autorizado(get("/api/notas/estudiante/" + ajeno.getId()), estudianteToken, 403);
        autorizado(get("/api/colegiaturas/" + cuotaAjena.getId()), estudianteToken, 403);
        autorizado(get("/api/colegiaturas/estudiante/" + ajeno.getId()), estudianteToken, 403);

        String docenteToken = login("docente_scope");
        autorizado(get("/api/academico/docente/me/cursos"), docenteToken, 200);
        autorizado(get("/api/academico/docente/me/cursos/" + propioDocente.getId() + "/estudiantes"), docenteToken, 200);
        autorizado(get("/api/academico/docente/me/cursos/" + ajenoDocente.getId() + "/estudiantes"), docenteToken, 403);
        autorizado(post("/api/notas").contentType(MediaType.APPLICATION_JSON).content("""
                {"estudianteId":%d,"cursoId":%d,"cicloAnio":2026,"tipoEvaluacion":"QUIZ","calificacion":91}
                """.formatted(propio.getId(), propioDocente.getId())), docenteToken, 201);
        autorizado(post("/api/notas").contentType(MediaType.APPLICATION_JSON).content("""
                {"estudianteId":%d,"cursoId":%d,"cicloAnio":2026,"tipoEvaluacion":"QUIZ","calificacion":91}
                """.formatted(ajeno.getId(), ajenoDocente.getId())), docenteToken, 403);
        autorizado(get("/api/auditoria"), docenteToken, 403);
        autorizado(get("/api/cursos/docente/" + docente.getId()), docenteToken, 200);
        autorizado(get("/api/notas/curso/" + propioDocente.getId()), docenteToken, 403);
        autorizado(get("/api/academico/docente/me/cursos/" + propioDocente.getId() + "/notas"), docenteToken, 200);
        autorizado(get("/api/notas/" + notaPropia.getId()), docenteToken, 200);
        autorizado(get("/api/notas/curso/" + ajenoDocente.getId()), docenteToken, 403);
        autorizado(get("/api/notas/" + notaAjena.getId()), docenteToken, 403);
        autorizado(get("/api/colegiaturas/" + cuotaPropia.getId()), docenteToken, 403);

        String adminToken = login("admin_scope");
        autorizado(get("/api/auditoria"), adminToken, 200);
        mockMvc.perform(get("/api/auditoria").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].username").value("docente_scope"))
                .andExpect(jsonPath("$.content[0].accion").value("CREAR"))
                .andExpect(jsonPath("$.content[0].modulo").value("NOTAS"))
                .andExpect(jsonPath("$.content[0].tipoEntidad").value("NOTA"))
                .andExpect(jsonPath("$.content[0].entidadId").isNotEmpty())
                .andExpect(jsonPath("$.content[0].cambiosAntes").value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("password"))))
                .andExpect(jsonPath("$.content[0].cambiosDespues").value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("token"))));
        autorizado(get("/api/usuarios"), adminToken, 200);
        autorizado(get("/api/notas/" + notaAjena.getId()), adminToken, 200);
        autorizado(get("/api/colegiaturas/" + cuotaAjena.getId()), adminToken, 200);
    }

    private void autorizado(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request,
            String token, int status) throws Exception {
        mockMvc.perform(request.header("Authorization", "Bearer " + token))
                .andExpect(status().is(status));
    }

    private String login(String username) throws Exception {
        String json = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"Usuario123*\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(json).get("accessToken").asText();
    }

    private Rol rol(String codigo) {
        Rol rol = new Rol(); rol.setCodigo(codigo); rol.setNombre("Rol " + codigo); rol.setActivo(true);
        rol.setPermisos(new HashSet<>()); return rolRepository.save(rol);
    }

    private void usuario(String username, String email, Rol rol) {
        Usuario u = new Usuario(); u.setUsername(username); u.setEmail(email); u.setNombre("Usuario");
        u.setApellido("Prueba"); u.setPassword(passwordEncoder.encode("Usuario123*")); u.setActivo(true);
        u.setRoles(new HashSet<>(Set.of(rolRepository.findWithPermisosById(rol.getId()).orElseThrow())));
        usuarioRepository.save(u);
    }

    private Estudiante estudiante(String codigo, String correo) {
        return estudianteRepository.save(Estudiante.builder().codigoEstudiantil(codigo)
                .numeroIdentificacion("ID" + codigo).nombres("Nombre").apellidos("Apellido")
                .fechaNacimiento(LocalDate.of(2000, 1, 1)).correo(correo).activo(true).build());
    }

    private Docente docente(String codigo, String email) {
        return docenteRepository.save(Docente.builder().codigoDocente(codigo).nombre("Docente")
                .apellido(codigo).email(email).activo(true).build());
    }

    private Curso curso(String codigo, Carrera carrera, Docente docente) {
        return cursoRepository.save(Curso.builder().codigo(codigo).nombre(codigo).creditos(3)
                .horasSemanales(4).carrera(carrera).docente(docente).cicloAnio(2026).activo(true).build());
    }

    private Nota nota(Estudiante estudiante, Curso curso, String tipo) {
        return notaRepository.save(Nota.builder().estudiante(estudiante).curso(curso).cicloAnio(2026)
                .tipoEvaluacion(tipo).calificacion(new BigDecimal("80.00")).activo(true).build());
    }

    private Colegiatura cuota(Estudiante estudiante, String concepto) {
        return colegiaturaRepository.save(Colegiatura.builder().estudiante(estudiante).cicloAnio(2026)
                .concepto(concepto).montoTotal(new BigDecimal("500.00")).montoPagado(BigDecimal.ZERO)
                .saldoPendiente(new BigDecimal("500.00")).fechaEmision(LocalDate.now())
                .fechaVencimiento(LocalDate.now().plusDays(30)).estado("PENDIENTE").activo(true).build());
    }
}
