package com.umg.sgau.curso.controller;

import static org.hamcrest.Matchers.containsInRelativeOrder;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.umg.sgau.curso.entity.Curso;
import com.umg.sgau.curso.exception.CarreraCursoInvalidaException;
import com.umg.sgau.curso.exception.CodigoCursoDuplicadoException;
import com.umg.sgau.curso.exception.CursoAcademicoDuplicadoException;
import com.umg.sgau.curso.exception.CursoExceptionHandler;
import com.umg.sgau.curso.exception.CursoNoEncontradoException;
import com.umg.sgau.curso.exception.DocenteCursoInvalidoException;
import com.umg.sgau.curso.service.CursoService;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@ExtendWith(MockitoExtension.class)
class CursoControllerTest {

    private static final String BASE_URL = "/api/cursos";

    @Mock
    private CursoService cursoService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new CursoController(cursoService))
                .setControllerAdvice(new CursoExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setValidator(validator)
                .build();
    }

    @Test
    void crearConDatosValidosRetorna201() throws Exception {
        Curso creado = curso(1L, "PROG-1", "Programacion I", 1L, null, true);
        when(cursoService.crear(any(Curso.class))).thenReturn(creado);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(requestValido("PROG-1", "Programacion I"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.codigo").value("PROG-1"))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    void obtenerPorIdExistenteRetorna200() throws Exception {
        when(cursoService.obtenerPorId(1L))
                .thenReturn(curso(1L, "PROG-1", "Programacion I", 1L, null, true));

        mockMvc.perform(get(BASE_URL + "/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("Programacion I"));
    }

    @Test
    void obtenerPorIdInexistenteRetorna404() throws Exception {
        when(cursoService.obtenerPorId(99L))
                .thenThrow(new CursoNoEncontradoException(99L));

        mockMvc.perform(get(BASE_URL + "/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message", containsString("ID: 99")));
    }

    @Test
    void crearConCodigoDuplicadoRetorna409() throws Exception {
        when(cursoService.crear(any(Curso.class)))
                .thenThrow(new CodigoCursoDuplicadoException("PROG-1"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(requestValido("PROG-1", "Curso diferente"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message", containsString("PROG-1")));
    }

    @Test
    void crearConDuplicidadAcademicaRetorna409() throws Exception {
        when(cursoService.crear(any(Curso.class)))
                .thenThrow(new CursoAcademicoDuplicadoException("Programacion I", 1L, 2026));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(requestValido("PROG-2", "Programacion I"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message", containsString("Programacion I")));
    }

    @Test
    void crearConCodigoVacioRetorna400() throws Exception {
        assertBadRequest(request(
                "",
                "Curso de prueba",
                "Codigo vacio",
                4,
                5,
                1,
                null,
                2026));
    }

    @Test
    void crearConCreditosInvalidosRetorna400() throws Exception {
        assertBadRequest(request(
                "TEST-CRED",
                "Curso creditos invalidos",
                "Prueba",
                0,
                5,
                1,
                null,
                2026));
    }

    @Test
    void crearConHorasSemanalesInvalidasRetorna400() throws Exception {
        assertBadRequest(request(
                "TEST-HORAS",
                "Curso horas invalidas",
                "Prueba",
                4,
                0,
                1,
                null,
                2026));
    }

    @Test
    void crearConCarreraInvalidaRetorna400() throws Exception {
        assertBadRequest(request(
                "TEST-CARR",
                "Curso carrera invalida",
                "Prueba",
                4,
                5,
                -1,
                null,
                2026));
    }

    @Test
    void asignarDocenteInvalidoDesdeServicioRetorna400() throws Exception {
        when(cursoService.asignarDocente(1L, 99L))
                .thenThrow(new DocenteCursoInvalidoException(99L));

        mockMvc.perform(patch(BASE_URL + "/{id}/docente", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("docenteId", 99))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message", containsString("99")));
    }

    @Test
    void crearConCarreraInvalidaDesdeServicioRetorna400() throws Exception {
        when(cursoService.crear(any(Curso.class)))
                .thenThrow(new CarreraCursoInvalidaException(-1L));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(requestValido("TEST-CARR", "Curso carrera invalida"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message", containsString("-1")));
    }

    @Test
    void actualizarConDatosValidosRetorna200() throws Exception {
        Curso actualizado = curso(1L, "PROG-1", "Programacion Basica", 1L, null, true);
        when(cursoService.actualizar(eq(1L), any(Curso.class))).thenReturn(actualizado);

        mockMvc.perform(put(BASE_URL + "/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(requestValido("PROG-1", "Programacion Basica"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("Programacion Basica"));
    }

    @Test
    void cambiarEstadoRetorna200() throws Exception {
        Curso inactivo = curso(1L, "PROG-1", "Programacion I", 1L, null, false);
        when(cursoService.cambiarEstado(1L, false)).thenReturn(inactivo);

        mockMvc.perform(patch(BASE_URL + "/{id}/estado", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("activo", false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));
    }

    @Test
    void cambiarEstadoConActivoNuloRetorna400() throws Exception {
        mockMvc.perform(patch(BASE_URL + "/{id}/estado", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"activo\":null}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void asignarDocenteRetorna200() throws Exception {
        Curso actualizado = curso(1L, "PROG-1", "Programacion I", 1L, 3L, true);
        when(cursoService.asignarDocente(1L, 3L)).thenReturn(actualizado);

        mockMvc.perform(patch(BASE_URL + "/{id}/docente", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("docenteId", 3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.docenteId").value(3L));
    }

    @Test
    void asignarDocenteConIdNegativoRetorna400() throws Exception {
        mockMvc.perform(patch(BASE_URL + "/{id}/docente", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("docenteId", -1))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listarConFiltrosPaginacionYOrdenamientoRetorna200() throws Exception {
        Page<Curso> pagina = new PageImpl<>(
                List.of(
                        curso(2L, "BD-1", "Bases de Datos I", 1L, 2L, true),
                        curso(1L, "PROG-1", "Programacion I", 1L, 2L, true)),
                PageRequest.of(0, 10, Sort.by("nombre").ascending()),
                2);
        when(cursoService.listar(
                eq("programacion"),
                eq(1L),
                eq(2L),
                eq(2026),
                eq(true),
                any(Pageable.class))).thenReturn(pagina);

        mockMvc.perform(get(BASE_URL)
                        .param("texto", "programacion")
                        .param("carreraId", "1")
                        .param("docenteId", "2")
                        .param("cicloAnio", "2026")
                        .param("activo", "true")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "nombre,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].nombre", containsInRelativeOrder(
                        "Bases de Datos I",
                        "Programacion I")));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(cursoService).listar(
                eq("programacion"),
                eq(1L),
                eq(2L),
                eq(2026),
                eq(true),
                pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();

        org.assertj.core.api.Assertions.assertThat(pageable.getPageNumber()).isZero();
        org.assertj.core.api.Assertions.assertThat(pageable.getPageSize()).isEqualTo(10);
        org.assertj.core.api.Assertions.assertThat(pageable.getSort().getOrderFor("nombre")).isNotNull();
        org.assertj.core.api.Assertions.assertThat(pageable.getSort().getOrderFor("nombre").isAscending()).isTrue();
    }

    @Test
    void obtenerCursosPorDocenteRetorna200() throws Exception {
        when(cursoService.obtenerCursosPorDocente(3L))
                .thenReturn(List.of(curso(1L, "PROG-1", "Programacion I", 1L, 3L, true)));

        mockMvc.perform(get(BASE_URL + "/docente/{docenteId}", 3L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].activo").value(true));
    }

    @Test
    void obtenerCursosActivosRetorna200() throws Exception {
        when(cursoService.obtenerCursosActivos())
                .thenReturn(List.of(curso(1L, "PROG-1", "Programacion I", 1L, null, true)));

        mockMvc.perform(get(BASE_URL + "/activos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].activo").value(true));
    }

    @Test
    void obtenerNombresDeCursosActivosRetorna200() throws Exception {
        when(cursoService.obtenerNombresDeCursosActivos())
                .thenReturn(List.of("Bases de Datos I", "Programacion Basica"));

        mockMvc.perform(get(BASE_URL + "/nombres-activos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("Bases de Datos I"))
                .andExpect(jsonPath("$[1]").value("Programacion Basica"));
    }

    private void assertBadRequest(Map<String, Object> request) throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isBadRequest());
    }

    private Map<String, Object> requestValido(String codigo, String nombre) {
        return request(
                codigo,
                nombre,
                "Descripcion de prueba",
                5,
                6,
                1,
                null,
                2026);
    }

    private Map<String, Object> request(
            String codigo,
            String nombre,
            String descripcion,
            Integer creditos,
            Integer horasSemanales,
            Integer carreraId,
            Integer docenteId,
            Integer cicloAnio) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("codigo", codigo);
        request.put("nombre", nombre);
        request.put("descripcion", descripcion);
        request.put("creditos", creditos);
        request.put("horasSemanales", horasSemanales);
        request.put("carreraId", carreraId);
        request.put("docenteId", docenteId);
        request.put("cicloAnio", cicloAnio);

        return request;
    }

    private String json(Map<String, Object> value) {
        StringBuilder json = new StringBuilder("{");
        Iterator<Entry<String, Object>> iterator = value.entrySet().iterator();

        while (iterator.hasNext()) {
            Entry<String, Object> entry = iterator.next();
            json.append('"').append(entry.getKey()).append("\":");

            if (entry.getValue() instanceof String text) {
                json.append('"').append(text).append('"');
            } else {
                json.append(entry.getValue());
            }

            if (iterator.hasNext()) {
                json.append(',');
            }
        }

        return json.append('}').toString();
    }

    private Curso curso(
            Long id,
            String codigo,
            String nombre,
            Long carreraId,
            Long docenteId,
            Boolean activo) {
        LocalDateTime fecha = LocalDateTime.of(2026, 7, 29, 15, 0);

        return Curso.builder()
                .id(id)
                .codigo(codigo)
                .nombre(nombre)
                .descripcion("Descripcion de prueba")
                .creditos(5)
                .horasSemanales(6)
                .carreraId(carreraId)
                .docenteId(docenteId)
                .cicloAnio(2026)
                .activo(activo)
                .fechaCreacion(fecha)
                .fechaActualizacion(fecha)
                .build();
    }
}
