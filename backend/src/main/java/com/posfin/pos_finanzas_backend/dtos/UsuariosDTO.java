package com.posfin.pos_finanzas_backend.dtos;

public class UsuariosDTO {
    private String id;
    private String nombre;
    private String contrasena;
    private String telefono;

    // Campos aplanados de la relación con Roles
    private String rolesId;
    private String rolesRoles;

    // Campos aplanados de la relación con Estados
    private String estadosId;
    private String estadosEstado;

    // Constructor
    public UsuariosDTO() {
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getRolesId() {
        return rolesId;
    }

    public void setRolesId(String rolesId) {
        this.rolesId = rolesId;
    }

    public String getRolesRoles() {
        return rolesRoles;
    }

    public void setRolesRoles(String rolesRoles) {
        this.rolesRoles = rolesRoles;
    }

    public String getEstadosId() {
        return estadosId;
    }

    public void setEstadosId(String estadosId) {
        this.estadosId = estadosId;
    }

    public String getEstadosEstado() {
        return estadosEstado;
    }

    public void setEstadosEstado(String estadosEstado) {
        this.estadosEstado = estadosEstado;
    }
}
