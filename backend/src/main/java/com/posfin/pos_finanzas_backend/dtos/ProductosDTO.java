package com.posfin.pos_finanzas_backend.dtos;

import java.math.BigDecimal;

public class ProductosDTO {
    private String id;
    private String nombre;

    // Campos aplanados de la relación con CategoriasProductos
    private String categoriasProductosId;
    private String categoriasProductosCategoria;

    // Campos aplanados de la relación con Personas (proveedor)
    private String proveedorId;
    private String proveedorNombre;
    private String proveedorApellidoPaterno;
    private String proveedorApellidoMaterno;

    // Campos aplanados de la relación con Estados
    private String estadosId;
    private String estadosEstado;

    // Campos para precios y costos más recientes
    private BigDecimal precioVentaActual;
    private BigDecimal precioCompraActual;

    // Campos para inventario (cantidad actual)
    private Integer cantidadInventario;

    // Constructor
    public ProductosDTO() {
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

    public String getCategoriasProductosId() {
        return categoriasProductosId;
    }

    public void setCategoriasProductosId(String categoriasProductosId) {
        this.categoriasProductosId = categoriasProductosId;
    }

    public String getCategoriasProductosCategoria() {
        return categoriasProductosCategoria;
    }

    public void setCategoriasProductosCategoria(String categoriasProductosCategoria) {
        this.categoriasProductosCategoria = categoriasProductosCategoria;
    }

    public String getProveedorId() {
        return proveedorId;
    }

    public void setProveedorId(String proveedorId) {
        this.proveedorId = proveedorId;
    }

    public String getProveedorNombre() {
        return proveedorNombre;
    }

    public void setProveedorNombre(String proveedorNombre) {
        this.proveedorNombre = proveedorNombre;
    }

    public String getProveedorApellidoPaterno() {
        return proveedorApellidoPaterno;
    }

    public void setProveedorApellidoPaterno(String proveedorApellidoPaterno) {
        this.proveedorApellidoPaterno = proveedorApellidoPaterno;
    }

    public String getProveedorApellidoMaterno() {
        return proveedorApellidoMaterno;
    }

    public void setProveedorApellidoMaterno(String proveedorApellidoMaterno) {
        this.proveedorApellidoMaterno = proveedorApellidoMaterno;
    }

    public String getEstadosId() {
        return estadosId;
    }

    public void setEstadosId(String estadosId) {
        this.estadosId = estadosId;
    }

    public String getEstadosEstado() {
        return estadosEstado;
    }

    public void setEstadosEstado(String estadosEstado) {
        this.estadosEstado = estadosEstado;
    }

    public BigDecimal getPrecioVentaActual() {
        return precioVentaActual;
    }

    public void setPrecioVentaActual(BigDecimal precioVentaActual) {
        this.precioVentaActual = precioVentaActual;
    }

    public BigDecimal getPrecioCompraActual() {
        return precioCompraActual;
    }

    public void setPrecioCompraActual(BigDecimal precioCompraActual) {
        this.precioCompraActual = precioCompraActual;
    }

    public Integer getCantidadInventario() {
        return cantidadInventario;
    }

    public void setCantidadInventario(Integer cantidadInventario) {
        this.cantidadInventario = cantidadInventario;
    }
}
