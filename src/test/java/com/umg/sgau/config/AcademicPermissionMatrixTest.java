package com.umg.sgau.config;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class AcademicPermissionMatrixTest {
    @Test void auditoriaEsPermisoEspecificoDelCatalogo() {
        assertThat(PermissionCatalog.todos()).containsKey(PermissionCatalog.AUDITORIA_LEER);
        assertThat(PermissionCatalog.AUDITORIA_LEER).isNotEqualTo(PermissionCatalog.CURSOS_LEER);
    }
}
