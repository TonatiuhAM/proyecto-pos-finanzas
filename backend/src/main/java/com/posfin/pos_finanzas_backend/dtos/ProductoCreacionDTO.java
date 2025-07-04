package com.posfin.pos_finanzas_backend.dtos;

import java.math.BigDecimal;

/**
 * DTO (Data Transfer Object) para la creación completa de productos.
 * Incluye todos los datos necesarios para crear un producto completo
 * con su historial de precios, costos, inventario inicial y movimiento.
 */
public class ProductoCreacionDTO {
    private String nombre;
    private String categoriasProductosId;
    private String proveedorId;
    private BigDecimal precioVenta;
    private BigDecimal precioCompra;
    private String unidadMedida; // "piezas" o "kilogramos"
    private Integer stockInicial;
    private String ubicacionId;
    private Integer stockMinimo;
    private Integer stockMaximo;
    private String usuarioId; // Para el movimiento de inventario

    // Constructor vacío
    public ProductoCreacionDTO() {
    }

    // Getters y Setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCategoriasProductosId() {
        return categoriasProductosId;
    }

    public void setCategoriasProductosId(String categoriasProductosId) {
        this.categoriasProductosId = categoriasProductosId;
    }

    public String getProveedorId() {
        return proveedorId;
    }

    public void setProveedorId(String proveedorId) {
        this.proveedorId = proveedorId;
    }

    public BigDecimal getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(BigDecimal precioVenta) {
        this.precioVenta = precioVenta;
    }

    public BigDecimal getPrecioCompra() {
        return precioCompra;
    }

    public void setPrecioCompra(BigDecimal precioCompra) {
        this.precioCompra = precioCompra;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public Integer getStockInicial() {
        return stockInicial;
    }

    public void setStockInicial(Integer stockInicial) {
        this.stockInicial = stockInicial;
    }

    public String getUbicacionId() {
        return ubicacionId;
    }

    public void setUbicacionId(String ubicacionId) {
        this.ubicacionId = ubicacionId;
    }

    public Integer getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(Integer stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public Integer getStockMaximo() {
        return stockMaximo;
    }

    public void setStockMaximo(Integer stockMaximo) {
        this.stockMaximo = stockMaximo;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }
}
