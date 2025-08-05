package com.posfin.pos_finanzas_backend.dtos;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * DTO para la respuesta de finalizar una venta.
 * Contiene información de confirmación sobre la venta creada.
 */
public class VentaFinalizadaResponseDTO {
    private String ventaId;
    private String workspaceNombre;
    private BigDecimal totalVenta;
    private OffsetDateTime fechaVenta;
    private String metodoPagoNombre;
    private String usuarioNombre;
    private String clienteNombre;
    private int cantidadProductos;
    private String mensaje;

    // Constructor vacío
    public VentaFinalizadaResponseDTO() {
    }

    // Constructor completo
    public VentaFinalizadaResponseDTO(String ventaId, String workspaceNombre, BigDecimal totalVenta,
            OffsetDateTime fechaVenta, String metodoPagoNombre, String usuarioNombre,
            String clienteNombre, int cantidadProductos, String mensaje) {
        this.ventaId = ventaId;
        this.workspaceNombre = workspaceNombre;
        this.totalVenta = totalVenta;
        this.fechaVenta = fechaVenta;
        this.metodoPagoNombre = metodoPagoNombre;
        this.usuarioNombre = usuarioNombre;
        this.clienteNombre = clienteNombre;
        this.cantidadProductos = cantidadProductos;
        this.mensaje = mensaje;
    }

    // Getters y Setters
    public String getVentaId() {
        return ventaId;
    }

    public void setVentaId(String ventaId) {
        this.ventaId = ventaId;
    }

    public String getWorkspaceNombre() {
        return workspaceNombre;
    }

    public void setWorkspaceNombre(String workspaceNombre) {
        this.workspaceNombre = workspaceNombre;
    }

    public BigDecimal getTotalVenta() {
        return totalVenta;
    }

    public void setTotalVenta(BigDecimal totalVenta) {
        this.totalVenta = totalVenta;
    }

    public OffsetDateTime getFechaVenta() {
        return fechaVenta;
    }

    public void setFechaVenta(OffsetDateTime fechaVenta) {
        this.fechaVenta = fechaVenta;
    }

    public String getMetodoPagoNombre() {
        return metodoPagoNombre;
    }

    public void setMetodoPagoNombre(String metodoPagoNombre) {
        this.metodoPagoNombre = metodoPagoNombre;
    }

    public String getUsuarioNombre() {
        return usuarioNombre;
    }

    public void setUsuarioNombre(String usuarioNombre) {
        this.usuarioNombre = usuarioNombre;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }

    public int getCantidadProductos() {
        return cantidadProductos;
    }

    public void setCantidadProductos(int cantidadProductos) {
        this.cantidadProductos = cantidadProductos;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
