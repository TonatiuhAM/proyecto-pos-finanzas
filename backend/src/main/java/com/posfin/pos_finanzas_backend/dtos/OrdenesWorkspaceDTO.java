package com.posfin.pos_finanzas_backend.dtos;

import java.math.BigDecimal;

/**
 * DTO (Data Transfer Object) para la entidad OrdenesWorkspace.
 * Se utiliza para controlar y dar forma a los datos que se envían
 * a través de la API, desacoplando la API del modelo de la base de datos.
 */
public class OrdenesWorkspaceDTO {

    private String id;
    private BigDecimal cantidadPz;
    private BigDecimal cantidadKg;

    // Campos "aplanados" de la entidad relacionada Workspaces
    private String workspaceId;
    private String workspaceNombre;

    // Campos "aplanados" de la entidad relacionada Productos
    private String productoId;
    private String productoNombre;

    // Campos "aplanados" de la entidad relacionada HistorialPrecios
    private String historialPrecioId;
    private BigDecimal precio;

    // Constructor vacío
    public OrdenesWorkspaceDTO() {
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

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getWorkspaceNombre() {
        return workspaceNombre;
    }

    public void setWorkspaceNombre(String workspaceNombre) {
        this.workspaceNombre = workspaceNombre;
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

    public String getHistorialPrecioId() {
        return historialPrecioId;
    }

    public void setHistorialPrecioId(String historialPrecioId) {
        this.historialPrecioId = historialPrecioId;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }
}
