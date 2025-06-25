package com.posfin.pos_finanzas_backend.dtos;

import java.math.BigDecimal;

/**
 * DTO (Data Transfer Object) para la entidad DetallesOrdenesDeCompras.
 * Se utiliza para controlar y dar forma a los datos que se envían
 * a través de la API, desacoplando la API del modelo de la base de datos.
 */
public class DetallesOrdenesDeComprasDTO {

    private String id;
    private BigDecimal cantidadPz;
    private BigDecimal cantidadKg;

    // Campos "aplanados" de la entidad relacionada OrdenesDeCompras
    private String ordenDeCompraId;

    // Campos "aplanados" de la entidad relacionada Productos
    private String productoId;
    private String productoNombre;

    // Campos "aplanados" de la entidad relacionada HistorialCostos
    private String historialCostoId;
    private BigDecimal costo;

    // Constructor vacío
    public DetallesOrdenesDeComprasDTO() {
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getOrdenDeCompraId() {
        return ordenDeCompraId;
    }

    public void setOrdenDeCompraId(String ordenDeCompraId) {
        this.ordenDeCompraId = ordenDeCompraId;
    }

    public String getProductoId() {
        return productoId;
    }

    public void setProductoId(String productoId) {
        this.productoId = productoId;
    }

    public String getProductoNombre() {
        return productoNombre;
    }

    public void setProductoNombre(String productoNombre) {
        this.productoNombre = productoNombre;
    }

    public String getHistorialCostoId() {
        return historialCostoId;
    }

    public void setHistorialCostoId(String historialCostoId) {
        this.historialCostoId = historialCostoId;
    }

    public BigDecimal getCosto() {
        return costo;
    }

    public void setCosto(BigDecimal costo) {
        this.costo = costo;
    }
}
