package com.umg.sgau.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.umg.sgau.usuario.entity.Usuario;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

class JwtServiceTest {

    private static final String SECRET = "0123456789abcdef0123456789abcdef";

    @Test
    void generarTokenIncluyeSubjectYRespetaExpiracionConfigurada() {
        JwtService jwtService = new JwtService(SECRET, 3_600_000L, "sgau-test");
        UserDetails userDetails = userDetails("admin");
        Usuario usuario = usuario("admin");

        String token = jwtService.generarToken(usuario, userDetails);

        assertThat(jwtService.extraerUsername(token)).isEqualTo("admin");
        assertThat(jwtService.esTokenValido(token, userDetails)).isTrue();
        assertThat(jwtService.getExpirationSeconds()).isEqualTo(3600L);
    }

    @Test
    void tokenNoEsValidoParaOtroUsuario() {
        JwtService jwtService = new JwtService(SECRET, 3_600_000L, "sgau-test");
        String token = jwtService.generarToken(usuario("admin"), userDetails("admin"));

        assertThat(jwtService.esTokenValido(token, userDetails("docente"))).isFalse();
    }

    @Test
    void secretoInvalidoFallaAlGenerarToken() {
        JwtService jwtService = new JwtService("secreto-corto", 3_600_000L, "sgau-test");

        assertThatThrownBy(() -> jwtService.generarToken(userDetails("admin")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("al menos 32 bytes");
    }

    private UserDetails userDetails(String username) {
        return new User(
                username,
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    private Usuario usuario(String username) {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername(username);
        usuario.setEmail(username + "@sgau.test");
        return usuario;
    }
}
