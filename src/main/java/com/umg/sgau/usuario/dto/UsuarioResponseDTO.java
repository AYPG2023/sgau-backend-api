package com.umg.sgau.usuario.dto;

import java.time.LocalDateTime;

public class UsuarioResponseDTO {
    private Long id;                 
    private String username;          
    private String email;             
    private String nombre;            
    private String apellido;          
    private Boolean activo;           
    private LocalDateTime fechaCreacion; 
    // Constructor vacío requerido por frameworks
    public UsuarioResponseDTO() {}

    // Getters y Setters para cada campo
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
