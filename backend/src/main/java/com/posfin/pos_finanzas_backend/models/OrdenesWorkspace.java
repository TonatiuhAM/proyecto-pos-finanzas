package com.posfin.pos_finanzas_backend.models;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "ordenes_workspace")
public class OrdenesWorkspace {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspaces workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productos_id", nullable = false)
    private Productos producto;

    @Column(name = "cantidad_pz", nullable = false)
    private BigDecimal cantidadPz;

    @Column(name = "cantidad_kg", nullable = false)
    private BigDecimal cantidadKg;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "historial_precios_id", nullable = false)
    private HistorialPrecios historialPrecio;

    // Constructor
    public OrdenesWorkspace() {
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

    public Workspaces getWorkspace() {
        return workspace;
    }

    public void setWorkspace(Workspaces workspace) {
        this.workspace = workspace;
    }

    public Productos getProducto() {
        return producto;
    }

    public void setProducto(Productos producto) {
        this.producto = producto;
    }

    public BigDecimal getCantidadPz() {
        return cantidadPz;
    }

    public void setCantidadPz(BigDecimal cantidadPz) {
        this.cantidadPz = cantidadPz;
    }

    public BigDecimal getCantidadKg() {
        return cantidadKg;
    }

    public void setCantidadKg(BigDecimal cantidadKg) {
        this.cantidadKg = cantidadKg;
    }

    public HistorialPrecios getHistorialPrecio() {
        return historialPrecio;
    }

    public void setHistorialPrecio(HistorialPrecios historialPrecio) {
        this.historialPrecio = historialPrecio;
    }
}
