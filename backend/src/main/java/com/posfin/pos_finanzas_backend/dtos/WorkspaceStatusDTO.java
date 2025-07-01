package com.posfin.pos_finanzas_backend.dtos;

public class WorkspaceStatusDTO {
    private String id;
    private String nombre;
    private String estado; // "disponible", "ocupado", "cuenta"
    private int cantidadOrdenes;
    private Boolean permanente;

    // Constructor
    public WorkspaceStatusDTO() {
    }

    public WorkspaceStatusDTO(String id, String nombre, String estado, int cantidadOrdenes, Boolean permanente) {
        this.id = id;
        this.nombre = nombre;
        this.estado = estado;
        this.cantidadOrdenes = cantidadOrdenes;
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

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getCantidadOrdenes() {
        return cantidadOrdenes;
    }

    public void setCantidadOrdenes(int cantidadOrdenes) {
        this.cantidadOrdenes = cantidadOrdenes;
    }

    public Boolean getPermanente() {
        return permanente;
    }

    public void setPermanente(Boolean permanente) {
        this.permanente = permanente;
    }
}