package com.posfin.pos_finanzas_backend.dtos;

/**
 * DTO para enviar información de roles al frontend
 */
public class RolResponseDTO {
    private String id;
    private String nombre;

    // Constructor vacío
    public RolResponseDTO() {
    }

    // Constructor completo
    public RolResponseDTO(String id, String nombre) {
        this.id = id;
        this.nombre = nombre;
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
}
