package com.umg.sgau.usuario.dto;


public class UsuarioRequestDTO {
    private String username;   
    private String password;   
    private String email;      
    private String nombre;     
    private String apellido;   
    private Boolean activo;    
   
    public UsuarioRequestDTO() {}

    // Getters y Setters para cada campo
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}
