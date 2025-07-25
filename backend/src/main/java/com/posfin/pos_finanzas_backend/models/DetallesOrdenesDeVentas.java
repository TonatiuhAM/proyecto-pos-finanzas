package com.posfin.pos_finanzas_backend.models;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "detalles_ordenes_de_ventas")
public class DetallesOrdenesDeVentas {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordenes_de_ventas_id", nullable = false)
    private OrdenesDeVentas ordenDeVenta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productos_id", nullable = false)
    private Productos producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "historial_precios_id", nullable = false)
    private HistorialPrecios historialPrecio;

    @Column(name = "cantidad_pz", nullable = false)
    private BigDecimal cantidadPz;

    @Column(name = "cantidad_kg", nullable = false)
    private BigDecimal cantidadKg;

    // Constructor
    public DetallesOrdenesDeVentas() {
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

    public OrdenesDeVentas getOrdenDeVenta() {
        return ordenDeVenta;
    }

    public void setOrdenDeVenta(OrdenesDeVentas ordenDeVenta) {
        this.ordenDeVenta = ordenDeVenta;
    }

    public Productos getProducto() {
        return producto;
    }

    public void setProducto(Productos producto) {
        this.producto = producto;
    }

    public HistorialPrecios getHistorialPrecio() {
        return historialPrecio;
    }

    public void setHistorialPrecio(HistorialPrecios historialPrecio) {
        this.historialPrecio = historialPrecio;
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
}
