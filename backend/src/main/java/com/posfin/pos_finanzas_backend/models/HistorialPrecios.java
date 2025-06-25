package com.posfin.pos_finanzas_backend.models;

import jakarta.persistence.*;
import java.time.ZonedDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "historial_precios")
public class HistorialPrecios {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productos_id", nullable = false)
    private Productos productos;

    @Column(name = "precio", nullable = false)
    private BigDecimal precio;

    @Column(name = "fecha_de_registro", nullable = false)
    private ZonedDateTime fechaDeRegistro;

    // Constructor
    public HistorialPrecios() {
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

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public ZonedDateTime getFechaDeRegistro() {
        return fechaDeRegistro;
    }

    public void setFechaDeRegistro(ZonedDateTime fechaDeRegistro) {
        this.fechaDeRegistro = fechaDeRegistro;
    }
}
