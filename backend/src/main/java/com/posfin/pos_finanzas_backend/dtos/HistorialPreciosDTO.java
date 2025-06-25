package com.posfin.pos_finanzas_backend.dtos;

import java.time.ZonedDateTime;
import java.math.BigDecimal;

public class HistorialPreciosDTO {
    private String id;
    private BigDecimal precio;
    private ZonedDateTime fechaDeRegistro;

    // Campos aplanados de la relación con Productos
    private String productosId;
    private String productosNombre;

    // Constructor
    public HistorialPreciosDTO() {
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getProductosId() {
        return productosId;
    }

    public void setProductosId(String productosId) {
        this.productosId = productosId;
    }

    public String getProductosNombre() {
        return productosNombre;
    }

    public void setProductosNombre(String productosNombre) {
        this.productosNombre = productosNombre;
    }
}
