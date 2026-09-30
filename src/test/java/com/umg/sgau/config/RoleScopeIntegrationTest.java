package com.umg.sgau.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umg.sgau.carrera.entity.Carrera;
import com.umg.sgau.carrera.repository.CarreraRepository;
import com.umg.sgau.academico.CicloAcademico;
import com.umg.sgau.academico.CicloAcademicoRepository;
import com.umg.sgau.academico.GradoAcademico;
import com.umg.sgau.academico.GradoAcademicoRepository;
import com.umg.sgau.academico.SeccionAcademica;
import com.umg.sgau.academico.SeccionAcademicaRepository;
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
    @Autowired CicloAcademicoRepository cicloRepository;
    @Autowired GradoAcademicoRepository gradoRepository;
    @Autowired SeccionAcademicaRepository seccionRepository;
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
        Colegiatura cuotaParcial = colegiaturaRepository.save(Colegiatura.builder().estudiante(propio).cicloAnio(2026)
                .concepto("Cuota parcial").montoTotal(new BigDecimal("500.00")).montoPagado(new BigDecimal("200.00"))
                .saldoPendiente(new BigDecimal("300.00")).fechaEmision(LocalDate.now())
                .fechaVencimiento(LocalDate.now().plusDays(30)).estado("PARCIAL").activo(true).build());
        Colegiatura cuotaPagada = colegiaturaRepository.save(Colegiatura.builder().estudiante(propio).cicloAnio(2026)
                .concepto("Cuota pagada").montoTotal(new BigDecimal("500.00")).montoPagado(new BigDecimal("500.00"))
                .saldoPendiente(BigDecimal.ZERO).fechaEmision(LocalDate.now())
                .fechaVencimiento(LocalDate.now().plusDays(30)).estado("PAGADA").activo(true).build());
        Estudiante sinCobros = estudiante("E003", "sin-cobros@sgau.test");
        Colegiatura cuotaAjena = cuota(ajeno, "Cuota ajena");

        usuario("estudiante_scope", propio.getCorreo(), estudianteRol);
        usuario("estudiante_sin_cobros", sinCobros.getCorreo(), estudianteRol);
        usuario("docente_scope", docente.getEmail(), docenteRol);
        usuario("admin_scope", "admin@sgau.test", adminRol);

        Estudiante nuevoInscrito = estudiante("E004", "inscrito@sgau.test");
        usuario("estudiante_inscripcion", nuevoInscrito.getCorreo(), estudianteRol);
        Carrera carreraConfigurada = carreraRepository.save(Carrera.builder().codigo("COBRO")
                .nombre("Carrera con cuotas configuradas").duracionAnios(4).activo(true)
                .mensualidad(new BigDecimal("275.50")).cantidadCuotas(3).diaVencimiento(15).build());
        LocalDate inicioCiclo = LocalDate.now().withDayOfMonth(1);
        CicloAcademico cicloConfigurado = cicloRepository.save(CicloAcademico.builder().nombre("Ciclo cobro")
                .anio(LocalDate.now().getYear()).fechaInicio(inicioCiclo).fechaFin(inicioCiclo.plusMonths(4))
                .activo(true).build());
        GradoAcademico gradoConfigurado = gradoRepository.save(GradoAcademico.builder()
                .codigo("GC1").nombre("Grado cobro").activo(true).build());
        SeccionAcademica seccionConfigurada = seccionRepository.save(SeccionAcademica.builder()
                .codigo("SC1").nombre("Sección cobro").grado(gradoConfigurado).activo(true).build());
        String tokenInscripcion = login("estudiante_inscripcion");
        mockMvc.perform(post("/api/academico/estudiante/me/inscripciones")
                        .header("Authorization", "Bearer " + tokenInscripcion)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"carreraId\":" + carreraConfigurada.getId() + ",\"cicloId\":"
                                + cicloConfigurado.getId() + ",\"gradoId\":" + gradoConfigurado.getId()
                                + ",\"seccionId\":" + seccionConfigurada.getId() + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensualidad").value(275.50))
                .andExpect(jsonPath("$.cantidadCuotas").value(3));
        var cuotasGeneradas = colegiaturaRepository.findByEstudiante_Id(nuevoInscrito.getId(),
                org.springframework.data.domain.Pageable.unpaged()).getContent();
        org.assertj.core.api.Assertions.assertThat(cuotasGeneradas).hasSize(3)
                .allSatisfy(cuotaGenerada -> org.assertj.core.api.Assertions.assertThat(cuotaGenerada.getMontoTotal())
                        .isEqualByComparingTo("275.50"));

        String estudianteToken = login("estudiante_scope");
        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + estudianteToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles", org.hamcrest.Matchers.hasItem("ESTUDIANTE")))
                .andExpect(jsonPath("$.permisos", org.hamcrest.Matchers.hasItem("COLEGIATURAS_REGISTRAR_PAGO")));
        autorizado(get("/api/academico/estudiante/me"), estudianteToken, 200);
        autorizado(get("/api/academico/estudiante/me/inscripciones"), estudianteToken, 200);
        autorizado(get("/api/academico/estudiante/me/cursos"), estudianteToken, 200);
        autorizado(get("/api/academico/estudiante/me/notas"), estudianteToken, 200);
        autorizado(get("/api/academico/estudiante/me/colegiaturas"), estudianteToken, 200);
        mockMvc.perform(get("/api/academico/estudiante/me/estado-cuenta").header("Authorization", "Bearer " + estudianteToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidadCargos").value(3))
                .andExpect(jsonPath("$.cantidadPendientes").value(2))
                .andExpect(jsonPath("$.totalCargos").value(1500.00))
                .andExpect(jsonPath("$.totalPagado").value(700.00))
                .andExpect(jsonPath("$.saldoPendiente").value(800.00))
                .andExpect(jsonPath("$.detalle[*].estado").value(org.hamcrest.Matchers.containsInAnyOrder(
                        "PENDIENTE", "PARCIAL", "PAGADA")));
        autorizado(get("/api/auditoria"), estudianteToken, 403);
        autorizado(get("/api/notas/" + notaPropia.getId()), estudianteToken, 200);
        autorizado(get("/api/colegiaturas/" + cuotaPropia.getId()), estudianteToken, 200);
        autorizado(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch(
                        "/api/colegiaturas/" + cuotaPropia.getId() + "/pago")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"montoPago\":100.00,\"fechaPago\":\"" + LocalDate.now() + "\"}"), estudianteToken, 403);
        autorizado(get("/api/cursos/" + propioDocente.getId()), estudianteToken, 200);
        autorizado(get("/api/usuarios"), estudianteToken, 403);
        autorizado(get("/api/notas/" + notaAjena.getId()), estudianteToken, 403);
        autorizado(get("/api/notas/estudiante/" + ajeno.getId()), estudianteToken, 403);
        autorizado(get("/api/colegiaturas/" + cuotaAjena.getId()), estudianteToken, 403);
        autorizado(get("/api/colegiaturas/estudiante/" + ajeno.getId()), estudianteToken, 403);
        String estudianteSinCobrosToken = login("estudiante_sin_cobros");
        mockMvc.perform(get("/api/academico/estudiante/me/estado-cuenta").header("Authorization", "Bearer " + estudianteSinCobrosToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidadCargos").value(0))
                .andExpect(jsonPath("$.totalCargos").value(0.00))
                .andExpect(jsonPath("$.saldoPendiente").value(0.00));

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
        String altaManual = """
                {"estudianteId":%d,"cicloAnio":2026,"concepto":"AJUSTE ADMINISTRATIVO","montoTotal":125.50,"fechaEmision":"%s","fechaVencimiento":"%s"}
                """.formatted(sinCobros.getId(), LocalDate.now(), LocalDate.now().plusDays(20));
        String altaResponse = mockMvc.perform(post("/api/colegiaturas").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(altaManual))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estudianteId").value(sinCobros.getId()))
                .andExpect(jsonPath("$.montoTotal").value(125.50))
                .andExpect(jsonPath("$.saldoPendiente").value(125.50))
                .andReturn().getResponse().getContentAsString();
        long altaId = objectMapper.readTree(altaResponse).get("id").asLong();
        mockMvc.perform(post("/api/colegiaturas").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(altaManual))
                .andExpect(status().isConflict());
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .put("/api/colegiaturas/" + altaId).header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"concepto":"AJUSTE ADMINISTRATIVO CORREGIDO","montoTotal":130.00,"fechaEmision":"%s","fechaVencimiento":"%s"}
                                """.formatted(LocalDate.now(), LocalDate.now().plusDays(25))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estudianteId").value(sinCobros.getId()))
                .andExpect(jsonPath("$.montoTotal").value(130.00))
                .andExpect(jsonPath("$.saldoPendiente").value(130.00));
        mockMvc.perform(post("/api/colegiaturas").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"estudianteId":-1,"cicloAnio":1900,"concepto":"","montoTotal":0,"fechaEmision":null,"fechaVencimiento":null}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Revisa los campos indicados."))
                .andExpect(jsonPath("$.fieldErrors.estudianteId").exists())
                .andExpect(jsonPath("$.fieldErrors.cicloAnio").exists())
                .andExpect(jsonPath("$.fieldErrors.concepto").exists())
                .andExpect(jsonPath("$.fieldErrors.montoTotal").exists())
                .andExpect(jsonPath("$.fieldErrors.fechaEmision").exists())
                .andExpect(jsonPath("$.fieldErrors.fechaVencimiento").exists());
        mockMvc.perform(post("/api/colegiaturas").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"estudianteId":%d,"cicloAnio":2026,"concepto":"FECHAS INVALIDAS","montoTotal":25.00,"fechaEmision":"%s","fechaVencimiento":"%s"}
                                """.formatted(sinCobros.getId(), LocalDate.now(), LocalDate.now().minusDays(1))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.fechaVencimiento").exists());
        String auditResponse = mockMvc.perform(get("/api/auditoria").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        var auditRows = objectMapper.readTree(auditResponse).get("content");
        com.fasterxml.jackson.databind.JsonNode docenteAudit = null;
        for (var row : auditRows) if ("docente_scope".equals(row.path("username").asText())) docenteAudit = row;
        org.assertj.core.api.Assertions.assertThat(docenteAudit).isNotNull();
        org.assertj.core.api.Assertions.assertThat(docenteAudit.path("accion").asText()).isEqualTo("CREAR");
        org.assertj.core.api.Assertions.assertThat(docenteAudit.path("modulo").asText()).isEqualTo("NOTAS");
        org.assertj.core.api.Assertions.assertThat(docenteAudit.path("tipoEntidad").asText()).isEqualTo("NOTA");
        org.assertj.core.api.Assertions.assertThat(docenteAudit.path("entidadId").asText()).isNotEmpty();
        org.assertj.core.api.Assertions.assertThat(docenteAudit.path("cambiosAntes").asText()).doesNotContain("password");
        org.assertj.core.api.Assertions.assertThat(docenteAudit.path("cambiosDespues").asText()).doesNotContain("token");
        autorizado(get("/api/usuarios"), adminToken, 200);
        autorizado(get("/api/notas/" + notaAjena.getId()), adminToken, 200);
        autorizado(get("/api/colegiaturas/" + cuotaAjena.getId()), adminToken, 200);

        autorizado(post("/api/academico/estudiante/me/colegiaturas/" + cuotaPropia.getId() + "/pagos")
                .contentType(MediaType.APPLICATION_JSON).content("""
                {"monto":100.00,"fechaPago":"%s","referencia":"BOLETA-PROPIA","metodoPago":"BANCO","idempotencyKey":"pago-propio-scope"}
                """.formatted(LocalDate.now())), estudianteToken, 201);
        autorizado(post("/api/academico/estudiante/me/colegiaturas/" + cuotaAjena.getId() + "/pagos")
                .contentType(MediaType.APPLICATION_JSON).content("""
                {"monto":100.00,"fechaPago":"%s","referencia":"BOLETA-AJENA","metodoPago":"BANCO","idempotencyKey":"pago-ajeno-scope"}
                """.formatted(LocalDate.now())), estudianteToken, 403);
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
        Usuario usuarioGuardado = usuarioRepository.save(u);
        if ("ESTUDIANTE".equalsIgnoreCase(rol.getCodigo())) {
            estudianteRepository.findByCorreoIgnoreCase(email).ifPresent(perfil -> {
                perfil.setUsuario(usuarioGuardado);
                estudianteRepository.save(perfil);
            });
        } else if ("DOCENTE".equalsIgnoreCase(rol.getCodigo())) {
            docenteRepository.findByEmailIgnoreCase(email).ifPresent(perfil -> {
                perfil.setUsuario(usuarioGuardado);
                docenteRepository.save(perfil);
            });
        }
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
