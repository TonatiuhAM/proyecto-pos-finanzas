package com.posfin.pos_finanzas_backend.dtos;

/**
 * DTO (Data Transfer Object) para la entidad Workspaces.
 * Se utiliza para controlar y dar forma a los datos que se envían
 * a través de la API, desacoplando la API del modelo de la base de datos.
 */
public class WorkspacesDTO {
    private String id;
    private String nombre;
    private Boolean permanente;

    // Constructor vacío
    public WorkspacesDTO() {
    }

    // Constructor con parámetros
    public WorkspacesDTO(String id, String nombre, Boolean permanente) {
        this.id = id;
        this.nombre = nombre;
        this.permanente = permanente;
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

    public Boolean getPermanente() {
        return permanente;
    }

    public void setPermanente(Boolean permanente) {
        this.permanente = permanente;
    }
}
