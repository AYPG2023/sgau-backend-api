package com.umg.sgau.auditoria.aspect;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.umg.sgau.auditoria.service.AuditoriaService;
import com.umg.sgau.auditoria.service.AuditoriaSnapshotService;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditoriaMutationAspect {
    private static final Logger log = LoggerFactory.getLogger(AuditoriaMutationAspect.class);
    private static final Set<String> SENSIBLES = Set.of("password", "contrasena", "currentpassword", "newpassword",
            "token", "accesstoken", "authorization", "secret", "credential", "jwt");
    private final AuditoriaService service;
    private final ObjectMapper mapper;
    private final HttpServletRequest request;
    private final AuditoriaSnapshotService snapshots;
    public AuditoriaMutationAspect(AuditoriaService service, ObjectMapper mapper, HttpServletRequest request,
            AuditoriaSnapshotService snapshots) {
        this.service=service; this.mapper=mapper; this.request=request; this.snapshots=snapshots;
    }

    @Around("within(@org.springframework.web.bind.annotation.RestController *) && execution(public * *(..))")
    public Object auditar(ProceedingJoinPoint jp) throws Throwable {
        String metodo = request.getMethod();
        if (request.getRequestURI().equals("/api/auth/login")) return jp.proceed();
        if (!(metodo.equals("POST") || metodo.equals("PUT") || metodo.equals("PATCH") || metodo.equals("DELETE")))
            return jp.proceed();
        Object[] args = jp.getArgs();
        String modulo = modulo();
        String idPrevio = idRuta();
        Object estadoPrevio = snapshots.obtener(modulo, idPrevio);
        String antes = estadoPrevio == null ? serializarArgumentos(args) : sanitizar(estadoPrevio);
        Object result = jp.proceed();
        try {
            Method method = ((MethodSignature) jp.getSignature()).getMethod();
            String accion = accion(metodo, request.getRequestURI());
            Object body = result instanceof ResponseEntity<?> response ? response.getBody() : result;
            String entidadId = idEntidad(body);
            Object estadoPosterior = snapshots.obtener(modulo, entidadId);
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            service.registrar(auth, accion, modulo, singular(modulo), entidadId,
                    metodo + " " + request.getRequestURI() + " confirmado por " + method.getName(), antes,
                    sanitizar(estadoPosterior == null ? body : estadoPosterior));
        } catch (Exception ex) {
            log.error("La operacion fue confirmada, pero no se pudo persistir su auditoria", ex);
        }
        return result;
    }

    private String modulo() {
        String[] partes=request.getRequestURI().split("/");
        return partes.length > 2 ? partes[2].toUpperCase(Locale.ROOT) : "SISTEMA";
    }
    private String singular(String modulo) { return modulo.endsWith("ES") ? modulo.substring(0, modulo.length()-2) : modulo.endsWith("S") ? modulo.substring(0, modulo.length()-1) : modulo; }
    private String accion(String metodo, String uri) {
        if (uri.endsWith("/estado")) return "CAMBIAR_ESTADO";
        if (uri.endsWith("/docente")) return metodo.equals("DELETE") ? "QUITAR_DOCENTE" : "ASIGNAR_DOCENTE";
        if (uri.endsWith("/roles")) return "ASIGNAR_ROLES";
        if (uri.endsWith("/permisos")) return "ASIGNAR_PERMISOS";
        if (uri.endsWith("/pago")) return "REGISTRAR_PAGO";
        if (uri.endsWith("/reactivar")) return "REACTIVAR";
        return switch (metodo) { case "POST" -> "CREAR"; case "PUT" -> "EDITAR"; case "PATCH" -> "ACTUALIZAR"; default -> "ELIMINAR"; };
    }
    private String idEntidad(Object body) {
        if (body != null) try { Object id=body.getClass().getMethod("getId").invoke(body); return id == null ? idRuta() : id.toString(); } catch (Exception ignored) {}
        return idRuta();
    }
    private String idRuta() {
        for (String p : request.getRequestURI().split("/")) if (p.matches("\\d+")) return p;
        return null;
    }
    private String serializarArgumentos(Object[] args) {
        var utiles=new ArrayList<>();
        for (Object arg:args) if (!(arg instanceof Authentication) && !(arg instanceof jakarta.servlet.ServletRequest) && !(arg instanceof jakarta.servlet.ServletResponse)) utiles.add(arg);
        return sanitizar(utiles);
    }
    private String sanitizar(Object value) {
        if (value == null) return null;
        try { JsonNode node=mapper.valueToTree(value); limpiar(node); return mapper.writeValueAsString(node); }
        catch (Exception ex) { return "{\"detalle\":\"no serializable\"}"; }
    }
    private void limpiar(JsonNode node) {
        if (node == null) return;
        if (node.isObject()) {
            ObjectNode object=(ObjectNode)node; Iterator<String> names=object.fieldNames(); var borrar=new ArrayList<String>();
            while(names.hasNext()){String name=names.next(); String lower=name.toLowerCase(Locale.ROOT); if(SENSIBLES.stream().anyMatch(lower::contains)) borrar.add(name); else limpiar(object.get(name));}
            borrar.forEach(object::remove);
        } else if(node.isArray()) node.forEach(this::limpiar);
    }
}
