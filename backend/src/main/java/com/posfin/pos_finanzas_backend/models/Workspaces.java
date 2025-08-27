package com.posfin.pos_finanzas_backend.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "workspaces")
public class Workspaces {
    @Id
    private String id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "permanente", nullable = false)
    private Boolean permanente = false;

    // Campo temporal para indicar si el workspace solicita cuenta
    // Este campo no está en la BD, se maneja en memoria
    @Column(nullable = true)
    private Boolean solicitudCuenta = false;

    // Constructor
    public Workspaces() {
    }

    // Función para generar un ID único antes de persistir
    @PrePersist
    public void generateId() {
        if (this.id == null) {
            this.id = java.util.UUID.randomUUID().toString();
        }
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

    public Boolean getSolicitudCuenta() {
        return solicitudCuenta;
    }

    public void setSolicitudCuenta(Boolean solicitudCuenta) {
        this.solicitudCuenta = solicitudCuenta;
    }
}
