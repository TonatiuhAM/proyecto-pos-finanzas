package com.posfin.pos_finanzas_backend.dtos;

/**
 * DTO para recibir datos de creación de empleados desde el frontend
 */
public class EmpleadoCreateRequestDTO {

    private String nombre;
    private String contrasena;
    private String telefono;
    private String rolId;

    // Constructor vacío
    public EmpleadoCreateRequestDTO() {
    }

    // Constructor completo
    public EmpleadoCreateRequestDTO(String nombre, String contrasena, String telefono, String rolId) {
        this.nombre = nombre;
        this.contrasena = contrasena;
        this.telefono = telefono;
        this.rolId = rolId;
    }

    // Getters y Setters
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

    public String getRolId() {
        return rolId;
    }

    public void setRolId(String rolId) {
        this.rolId = rolId;
    }
}
