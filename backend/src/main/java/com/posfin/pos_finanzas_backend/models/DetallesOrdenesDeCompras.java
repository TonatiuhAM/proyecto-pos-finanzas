package com.posfin.pos_finanzas_backend.models;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "detalles_ordenes_de_compras")
public class DetallesOrdenesDeCompras {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordenes_de_compras_id", nullable = false)
    private OrdenesDeCompras ordenDeCompra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Productos producto;

    @Column(name = "cantidad_pz", nullable = false)
    private BigDecimal cantidadPz;

    @Column(name = "cantidad_kg", nullable = false)
    private BigDecimal cantidadKg;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "historial_costos_id", nullable = false)
    private HistorialCostos historialCosto;

    // Constructor
    public DetallesOrdenesDeCompras() {
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

    public OrdenesDeCompras getOrdenDeCompra() {
        return ordenDeCompra;
    }

    public void setOrdenDeCompra(OrdenesDeCompras ordenDeCompra) {
        this.ordenDeCompra = ordenDeCompra;
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

    public HistorialCostos getHistorialCosto() {
        return historialCosto;
    }

    public void setHistorialCosto(HistorialCostos historialCosto) {
        this.historialCosto = historialCosto;
    }
}
