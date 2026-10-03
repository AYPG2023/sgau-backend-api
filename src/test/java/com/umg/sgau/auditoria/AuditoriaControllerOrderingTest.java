package com.umg.sgau.auditoria;

import static org.assertj.core.api.Assertions.assertThat;

import com.umg.sgau.auditoria.controller.AuditoriaController;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

class AuditoriaControllerOrderingTest {

    @Test
    void ordenaDeLaActividadMasAntiguaALaMasRecientePorDefecto() {
        Method endpoint = Arrays.stream(AuditoriaController.class.getDeclaredMethods())
                .filter(method -> method.getName().equals("buscar"))
                .findFirst()
                .orElseThrow();

        PageableDefault pageableDefault = Arrays.stream(endpoint.getParameters())
                .filter(parameter -> parameter.getType().equals(Pageable.class))
                .findFirst()
                .orElseThrow()
                .getAnnotation(PageableDefault.class);

        assertThat(pageableDefault).isNotNull();
        assertThat(pageableDefault.direction()).isEqualTo(Sort.Direction.ASC);
        assertThat(pageableDefault.sort()).containsExactly("fechaHora", "id");
    }
}
