package com.umg.sgau.carrera.controller;

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

import com.umg.sgau.carrera.entity.Carrera;
import com.umg.sgau.carrera.exception.CarreraExceptionHandler;
import com.umg.sgau.carrera.exception.CarreraNoEncontradaException;
import com.umg.sgau.carrera.exception.CodigoCarreraDuplicadoException;
import com.umg.sgau.carrera.exception.NombreCarreraDuplicadoException;
import com.umg.sgau.carrera.service.CarreraService;
import java.time.LocalDateTime;
import java.util.Iterator;
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
class CarreraControllerTest {

    private static final String BASE_URL = "/api/carreras";

    @Mock
    private CarreraService carreraService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new CarreraController(carreraService))
                .setControllerAdvice(new CarreraExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setValidator(validator)
                .build();
    }

    @Test
    void crearConDatosValidosRetorna201() throws Exception {
        Carrera creada = carrera(1L, "ISI", "Ingenieria en Sistemas", true);
        when(carreraService.crear(any(Carrera.class))).thenReturn(creada);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "codigo", "ISI",
                                "nombre", "Ingenieria en Sistemas",
                                "descripcion", "Carrera de software",
                                "duracionAnios", 5))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.codigo").value("ISI"))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    void obtenerPorIdExistenteRetorna200() throws Exception {
        when(carreraService.obtenerPorId(1L))
                .thenReturn(carrera(1L, "ISI", "Ingenieria en Sistemas", true));

        mockMvc.perform(get(BASE_URL + "/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("Ingenieria en Sistemas"));
    }

    @Test
    void obtenerPorIdInexistenteRetorna404() throws Exception {
        when(carreraService.obtenerPorId(99L))
                .thenThrow(new CarreraNoEncontradaException(99L));

        mockMvc.perform(get(BASE_URL + "/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message", containsString("ID: 99")));
    }

    @Test
    void crearConCodigoDuplicadoRetorna409() throws Exception {
        when(carreraService.crear(any(Carrera.class)))
                .thenThrow(new CodigoCarreraDuplicadoException("ISI"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "codigo", "ISI",
                                "nombre", "Otra carrera",
                                "descripcion", "Codigo duplicado",
                                "duracionAnios", 4))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message", containsString("ISI")));
    }

    @Test
    void crearConNombreDuplicadoRetorna409() throws Exception {
        when(carreraService.crear(any(Carrera.class)))
                .thenThrow(new NombreCarreraDuplicadoException("Ingenieria en Sistemas"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "codigo", "OTRA",
                                "nombre", "Ingenieria en Sistemas",
                                "descripcion", "Nombre duplicado",
                                "duracionAnios", 4))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message", containsString("Ingenieria en Sistemas")));
    }

    @Test
    void crearConCodigoVacioRetorna400() throws Exception {
        assertBadRequest(Map.of(
                "codigo", "",
                "nombre", "Carrera de prueba",
                "descripcion", "Codigo vacio",
                "duracionAnios", 4));
    }

    @Test
    void crearConCodigoInvalidoRetorna400() throws Exception {
        assertBadRequest(Map.of(
                "codigo", "ISI@2026",
                "nombre", "Carrera invalida",
                "descripcion", "Codigo invalido",
                "duracionAnios", 4));
    }

    @Test
    void crearConNombreVacioRetorna400() throws Exception {
        assertBadRequest(Map.of(
                "codigo", "TEST",
                "nombre", "",
                "descripcion", "Nombre vacio",
                "duracionAnios", 4));
    }

    @Test
    void crearConDuracionCeroRetorna400() throws Exception {
        assertBadRequest(Map.of(
                "codigo", "TEST-0",
                "nombre", "Carrera duracion cero",
                "descripcion", "Duracion invalida",
                "duracionAnios", 0));
    }

    @Test
    void crearConDuracionMayorADiezRetorna400() throws Exception {
        assertBadRequest(Map.of(
                "codigo", "TEST-11",
                "nombre", "Carrera duracion invalida",
                "descripcion", "Duracion invalida",
                "duracionAnios", 11));
    }

    @Test
    void actualizarConDatosValidosRetorna200() throws Exception {
        Carrera actualizada = carrera(1L, "ISI", "Ingenieria en Sistemas de Informacion", true);
        when(carreraService.actualizar(eq(1L), any(Carrera.class))).thenReturn(actualizada);

        mockMvc.perform(put(BASE_URL + "/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "codigo", "ISI",
                                "nombre", "Ingenieria en Sistemas de Informacion",
                                "descripcion", "Carrera actualizada",
                                "duracionAnios", 5))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("Ingenieria en Sistemas de Informacion"));
    }

    @Test
    void cambiarEstadoRetorna200() throws Exception {
        Carrera inactiva = carrera(1L, "ISI", "Ingenieria en Sistemas", false);
        when(carreraService.cambiarEstado(1L, false)).thenReturn(inactiva);

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
    void listarConFiltrosPaginacionYOrdenamientoRetorna200() throws Exception {
        Page<Carrera> pagina = new PageImpl<>(
                List.of(
                        carrera(2L, "IIND", "Ingenieria Industrial", true),
                        carrera(1L, "ISI", "Ingenieria en Sistemas", true)),
                PageRequest.of(0, 10, Sort.by("nombre").ascending()),
                2);
        when(carreraService.listar(eq("ingenieria"), eq(true), any(Pageable.class))).thenReturn(pagina);

        mockMvc.perform(get(BASE_URL)
                        .param("texto", "ingenieria")
                        .param("activo", "true")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "nombre,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].nombre", containsInRelativeOrder(
                        "Ingenieria Industrial",
                        "Ingenieria en Sistemas")));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(carreraService).listar(eq("ingenieria"), eq(true), pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();

        org.assertj.core.api.Assertions.assertThat(pageable.getPageNumber()).isZero();
        org.assertj.core.api.Assertions.assertThat(pageable.getPageSize()).isEqualTo(10);
        org.assertj.core.api.Assertions.assertThat(pageable.getSort().getOrderFor("nombre")).isNotNull();
        org.assertj.core.api.Assertions.assertThat(pageable.getSort().getOrderFor("nombre").isAscending()).isTrue();
    }

    @Test
    void obtenerCarrerasActivasRetorna200() throws Exception {
        when(carreraService.obtenerCarrerasActivas())
                .thenReturn(List.of(carrera(1L, "ISI", "Ingenieria en Sistemas", true)));

        mockMvc.perform(get(BASE_URL + "/activas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].activo").value(true));
    }

    @Test
    void obtenerNombresDeCarrerasActivasRetorna200() throws Exception {
        when(carreraService.obtenerNombresDeCarrerasActivas())
                .thenReturn(List.of("Ingenieria Industrial", "Ingenieria en Sistemas"));

        mockMvc.perform(get(BASE_URL + "/nombres-activos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("Ingenieria Industrial"))
                .andExpect(jsonPath("$[1]").value("Ingenieria en Sistemas"));
    }

    private void assertBadRequest(Map<String, Object> request) throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isBadRequest());
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

    private Carrera carrera(Long id, String codigo, String nombre, Boolean activo) {
        LocalDateTime fecha = LocalDateTime.of(2026, 7, 29, 15, 0);

        return Carrera.builder()
                .id(id)
                .codigo(codigo)
                .nombre(nombre)
                .descripcion("Descripcion de prueba")
                .duracionAnios(5)
                .activo(activo)
                .fechaCreacion(fecha)
                .fechaActualizacion(fecha)
                .build();
    }
}
