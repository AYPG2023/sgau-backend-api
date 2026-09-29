package com.umg.sgau.auditoria.aspect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umg.sgau.auditoria.service.AuditoriaService;
import com.umg.sgau.auditoria.service.AuditoriaSnapshotService;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Map;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class AuditoriaMutationAspectTest {
    @Mock AuditoriaService service; @Mock AuditoriaSnapshotService snapshots; @Mock HttpServletRequest request; @Mock ProceedingJoinPoint jp; @Mock MethodSignature signature;
    @AfterEach void clear() { SecurityContextHolder.clearContext(); }

    @Test void eliminaPasswordYTokenDeLosCambios() throws Throwable {
        when(request.getMethod()).thenReturn("PUT"); when(request.getRequestURI()).thenReturn("/api/usuarios/7");
        when(snapshots.obtener("USUARIOS", "7")).thenReturn(null);
        when(jp.getArgs()).thenReturn(new Object[]{Map.of("nombre", "Ana", "password", "secreto", "token", "jwt")});
        when(jp.proceed()).thenReturn(ResponseEntity.ok(Map.of("id", 7, "nombre", "Ana", "accessToken", "jwt")));
        when(jp.getSignature()).thenReturn(signature); Method m=getClass().getDeclaredMethod("metodoEjemplo"); when(signature.getMethod()).thenReturn(m);
        new AuditoriaMutationAspect(service, new ObjectMapper(), request, snapshots).auditar(jp);
        ArgumentCaptor<String> antes=ArgumentCaptor.forClass(String.class), despues=ArgumentCaptor.forClass(String.class);
        verify(service).registrar(any(), eq("EDITAR"), eq("USUARIOS"), eq("USUARIO"), eq("7"), any(), antes.capture(), despues.capture());
        assertThat(antes.getValue()).contains("Ana").doesNotContain("secreto", "password", "jwt", "token");
        assertThat(despues.getValue()).contains("Ana").doesNotContain("accessToken", "jwt");
    }

    @Test void noAuditaLogin() throws Throwable {
        when(request.getMethod()).thenReturn("POST"); when(request.getRequestURI()).thenReturn("/api/auth/login");
        when(jp.proceed()).thenReturn(ResponseEntity.ok().build());
        new AuditoriaMutationAspect(service, new ObjectMapper(), request, snapshots).auditar(jp);
        verify(service, never()).registrar(any(), any(), any(), any(), any(), any(), any(), any());
    }

    private void metodoEjemplo() {}
}
