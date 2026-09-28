package com.umg.sgau.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService customUserDetailsService;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final String allowedOrigins;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            CustomUserDetailsService customUserDetailsService,
            CustomAuthenticationEntryPoint authenticationEntryPoint,
            CustomAccessDeniedHandler accessDeniedHandler,
            @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:5173}") String allowedOrigins) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.customUserDetailsService = customUserDetailsService;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
        this.allowedOrigins = allowedOrigins;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                        .requestMatchers(
                                "/error",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/usuarios/*/roles").hasAuthority(PermissionCatalog.USUARIOS_ASIGNAR_ROLES)
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/**").hasAuthority(PermissionCatalog.USUARIOS_LEER)
                        .requestMatchers(HttpMethod.POST, "/api/usuarios/**").hasAuthority(PermissionCatalog.USUARIOS_CREAR)
                        .requestMatchers(HttpMethod.PUT, "/api/usuarios/**").hasAuthority(PermissionCatalog.USUARIOS_EDITAR)
                        .requestMatchers(HttpMethod.DELETE, "/api/usuarios/**").hasAuthority(PermissionCatalog.USUARIOS_ELIMINAR)
                        .requestMatchers(HttpMethod.PUT, "/api/roles/*/permisos").hasAuthority(PermissionCatalog.ROLES_ASIGNAR_PERMISOS)
                        .requestMatchers(HttpMethod.PATCH, "/api/roles/*/estado").hasAuthority(PermissionCatalog.ROLES_CAMBIAR_ESTADO)
                        .requestMatchers(HttpMethod.GET, "/api/roles/**").hasAuthority(PermissionCatalog.ROLES_LEER)
                        .requestMatchers(HttpMethod.POST, "/api/roles/**").hasAuthority(PermissionCatalog.ROLES_CREAR)
                        .requestMatchers(HttpMethod.PUT, "/api/roles/**").hasAuthority(PermissionCatalog.ROLES_EDITAR)
                        .requestMatchers(HttpMethod.PATCH, "/api/permisos/*/estado").hasAuthority(PermissionCatalog.PERMISOS_CAMBIAR_ESTADO)
                        .requestMatchers(HttpMethod.GET, "/api/permisos/**").hasAuthority(PermissionCatalog.PERMISOS_LEER)
                        .requestMatchers(HttpMethod.POST, "/api/permisos/**").hasAuthority(PermissionCatalog.PERMISOS_CREAR)
                        .requestMatchers(HttpMethod.PUT, "/api/permisos/**").hasAuthority(PermissionCatalog.PERMISOS_EDITAR)
                        .requestMatchers(HttpMethod.PATCH, "/api/carreras/*/estado").hasAuthority(PermissionCatalog.CARRERAS_CAMBIAR_ESTADO)
                        .requestMatchers(HttpMethod.GET, "/api/carreras/**").hasAuthority(PermissionCatalog.CARRERAS_LEER)
                        .requestMatchers(HttpMethod.POST, "/api/carreras/**").hasAuthority(PermissionCatalog.CARRERAS_CREAR)
                        .requestMatchers(HttpMethod.PUT, "/api/carreras/**").hasAuthority(PermissionCatalog.CARRERAS_EDITAR)
                        .requestMatchers(HttpMethod.PATCH, "/api/cursos/*/estado").hasAuthority(PermissionCatalog.CURSOS_CAMBIAR_ESTADO)
                        .requestMatchers(HttpMethod.PATCH, "/api/cursos/*/docente").hasAuthority(PermissionCatalog.CURSOS_ASIGNAR_DOCENTE)
                        .requestMatchers(HttpMethod.DELETE, "/api/cursos/*/docente").hasAuthority(PermissionCatalog.CURSOS_ASIGNAR_DOCENTE)
                        .requestMatchers(HttpMethod.GET, "/api/cursos/**").hasAuthority(PermissionCatalog.CURSOS_LEER)
                        .requestMatchers(HttpMethod.POST, "/api/cursos/**").hasAuthority(PermissionCatalog.CURSOS_CREAR)
                        .requestMatchers(HttpMethod.PUT, "/api/cursos/**").hasAuthority(PermissionCatalog.CURSOS_EDITAR)
                        .requestMatchers(HttpMethod.PATCH, "/api/docentes/*/estado").hasAuthority(PermissionCatalog.DOCENTES_CAMBIAR_ESTADO)
                        .requestMatchers(HttpMethod.GET, "/api/docentes/**").hasAuthority(PermissionCatalog.DOCENTES_LEER)
                        .requestMatchers(HttpMethod.POST, "/api/docentes/**").hasAuthority(PermissionCatalog.DOCENTES_CREAR)
                        .requestMatchers(HttpMethod.PUT, "/api/docentes/**").hasAuthority(PermissionCatalog.DOCENTES_EDITAR)
                        .requestMatchers(HttpMethod.DELETE, "/api/docentes/**").hasAuthority(PermissionCatalog.DOCENTES_ELIMINAR)
                        .requestMatchers(HttpMethod.PATCH, "/api/estudiantes/*/estado").hasAuthority(PermissionCatalog.ESTUDIANTES_CAMBIAR_ESTADO)
                        .requestMatchers(HttpMethod.GET, "/api/estudiantes/**").hasAuthority(PermissionCatalog.ESTUDIANTES_LEER)
                        .requestMatchers(HttpMethod.POST, "/api/estudiantes/**").hasAuthority(PermissionCatalog.ESTUDIANTES_CREAR)
                        .requestMatchers(HttpMethod.PUT, "/api/estudiantes/**").hasAuthority(PermissionCatalog.ESTUDIANTES_EDITAR)
                        .requestMatchers(HttpMethod.PATCH, "/api/inscripciones/*/estado", "/api/inscripciones/*/reactivar").hasAuthority(PermissionCatalog.INSCRIPCIONES_CAMBIAR_ESTADO)
                        .requestMatchers(HttpMethod.GET, "/api/inscripciones/**").hasAuthority(PermissionCatalog.INSCRIPCIONES_LEER)
                        .requestMatchers(HttpMethod.POST, "/api/inscripciones/**").hasAuthority(PermissionCatalog.INSCRIPCIONES_CREAR)
                        .requestMatchers(HttpMethod.PUT, "/api/inscripciones/**").hasAuthority(PermissionCatalog.INSCRIPCIONES_EDITAR)
                        .requestMatchers(HttpMethod.PATCH, "/api/notas/*/estado").hasAuthority(PermissionCatalog.NOTAS_CAMBIAR_ESTADO)
                        .requestMatchers(HttpMethod.GET, "/api/notas/**").hasAuthority(PermissionCatalog.NOTAS_LEER)
                        .requestMatchers(HttpMethod.POST, "/api/notas/**").hasAuthority(PermissionCatalog.NOTAS_CREAR)
                        .requestMatchers(HttpMethod.PUT, "/api/notas/**").hasAuthority(PermissionCatalog.NOTAS_EDITAR)
                        .requestMatchers(HttpMethod.PATCH, "/api/colegiaturas/*/pago").hasAuthority(PermissionCatalog.COLEGIATURAS_REGISTRAR_PAGO)
                        .requestMatchers(HttpMethod.PATCH, "/api/colegiaturas/*/estado").hasAuthority(PermissionCatalog.COLEGIATURAS_CAMBIAR_ESTADO)
                        .requestMatchers(HttpMethod.GET, "/api/colegiaturas/**").hasAuthority(PermissionCatalog.COLEGIATURAS_LEER)
                        .requestMatchers(HttpMethod.POST, "/api/colegiaturas/**").hasAuthority(PermissionCatalog.COLEGIATURAS_CREAR)
                        .requestMatchers(HttpMethod.PUT, "/api/colegiaturas/**").hasAuthority(PermissionCatalog.COLEGIATURAS_EDITAR)
                        .anyRequest().authenticated())
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(parseAllowedOrigins());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private List<String> parseAllowedOrigins() {
        return Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .toList();
    }
}
