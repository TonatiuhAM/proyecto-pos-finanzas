package com.posfin.pos_finanzas_backend.dtos;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * DTO (Data Transfer Object) para la entidad MovimientosInventarios.
 * Se utiliza para controlar y dar forma a los datos que se envían
 * a través de la API, desacoplando la API del modelo de la base de datos.
 */
public class MovimientosInventariosDTO {

    private String id;
    private BigDecimal cantidad;
    private OffsetDateTime fechaMovimiento;
    private String claveMovimiento;

    // Campos "aplanados" de la entidad relacionada Productos
    private String productoId;
    private String productoNombre;

    // Campos "aplanados" de la entidad relacionada Ubicaciones
    private String ubicacionId;
    private String ubicacionNombre;
    private String ubicacionDescripcion;

    // Campos "aplanados" de la entidad relacionada TipoMovimientos
    private String tipoMovimientoId;
    private String tipoMovimientoNombre;

    // Campos "aplanados" de la entidad relacionada Usuarios
    private String usuarioId;
    private String usuarioNombre;
    private String usuarioTelefono;

    // Constructor vacío
    public MovimientosInventariosDTO() {
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }

    public OffsetDateTime getFechaMovimiento() {
        return fechaMovimiento;
    }

    public void setFechaMovimiento(OffsetDateTime fechaMovimiento) {
        this.fechaMovimiento = fechaMovimiento;
    }

    public String getClaveMovimiento() {
        return claveMovimiento;
    }

    public void setClaveMovimiento(String claveMovimiento) {
        this.claveMovimiento = claveMovimiento;
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

    public String getUbicacionId() {
        return ubicacionId;
    }

    public void setUbicacionId(String ubicacionId) {
        this.ubicacionId = ubicacionId;
    }

    public String getUbicacionNombre() {
        return ubicacionNombre;
    }

    public void setUbicacionNombre(String ubicacionNombre) {
        this.ubicacionNombre = ubicacionNombre;
    }

    public String getUbicacionDescripcion() {
        return ubicacionDescripcion;
    }

    public void setUbicacionDescripcion(String ubicacionDescripcion) {
        this.ubicacionDescripcion = ubicacionDescripcion;
    }

    public String getTipoMovimientoId() {
        return tipoMovimientoId;
    }

    public void setTipoMovimientoId(String tipoMovimientoId) {
        this.tipoMovimientoId = tipoMovimientoId;
    }

    public String getTipoMovimientoNombre() {
        return tipoMovimientoNombre;
    }

    public void setTipoMovimientoNombre(String tipoMovimientoNombre) {
        this.tipoMovimientoNombre = tipoMovimientoNombre;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getUsuarioNombre() {
        return usuarioNombre;
    }

    public void setUsuarioNombre(String usuarioNombre) {
        this.usuarioNombre = usuarioNombre;
    }

    public String getUsuarioTelefono() {
        return usuarioTelefono;
    }

    public void setUsuarioTelefono(String usuarioTelefono) {
        this.usuarioTelefono = usuarioTelefono;
    }
}
