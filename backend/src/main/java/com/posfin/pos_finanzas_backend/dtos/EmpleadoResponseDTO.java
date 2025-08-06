package com.posfin.pos_finanzas_backend.dtos;

/**
 * DTO para enviar información de empleados al frontend en la lista de empleados
 */
public class EmpleadoResponseDTO {
    private String id;
    private String nombre;
    private String telefono;
    private String rolId;
    private String rolNombre;
    private String estadoId;
    private String estadoNombre;

    // Constructor vacío
    public EmpleadoResponseDTO() {
    }

    // Constructor completo
    public EmpleadoResponseDTO(String id, String nombre, String telefono, 
                              String rolId, String rolNombre, 
                              String estadoId, String estadoNombre) {
        this.id = id;
        this.nombre = nombre;
        this.telefono = telefono;
        this.rolId = rolId;
        this.rolNombre = rolNombre;
        this.estadoId = estadoId;
        this.estadoNombre = estadoNombre;
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

    public String getRolNombre() {
        return rolNombre;
    }

    public void setRolNombre(String rolNombre) {
        this.rolNombre = rolNombre;
    }

    public String getEstadoId() {
        return estadoId;
    }

    public void setEstadoId(String estadoId) {
        this.estadoId = estadoId;
    }

    public String getEstadoNombre() {
        return estadoNombre;
    }

    public void setEstadoNombre(String estadoNombre) {
        this.estadoNombre = estadoNombre;
    }
}
