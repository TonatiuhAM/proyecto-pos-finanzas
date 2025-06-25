package com.posfin.pos_finanzas_backend.models;

import jakarta.persistence.*;
import java.time.ZonedDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "historial_costos")
public class HistorialCostos {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productos_id", nullable = false)
    private Productos productos;

    @Column(name = "costo", nullable = false)
    private BigDecimal costo;

    @Column(name = "fecha_de_registro", nullable = false)
    private ZonedDateTime fechaDeRegistro;

    // Constructor
    public HistorialCostos() {
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

    public Productos getProductos() {
        return productos;
    }

    public void setProductos(Productos productos) {
        this.productos = productos;
    }

    public BigDecimal getCosto() {
        return costo;
    }

    public void setCosto(BigDecimal costo) {
        this.costo = costo;
    }

    public ZonedDateTime getFechaDeRegistro() {
        return fechaDeRegistro;
    }

    public void setFechaDeRegistro(ZonedDateTime fechaDeRegistro) {
        this.fechaDeRegistro = fechaDeRegistro;
    }
}
