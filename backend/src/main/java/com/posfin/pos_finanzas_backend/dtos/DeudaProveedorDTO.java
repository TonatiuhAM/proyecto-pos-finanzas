package com.posfin.pos_finanzas_backend.dtos;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * DTO para respuesta de cálculo de deuda de proveedor
 * Incluye información detallada del proveedor y sus deudas
 */
public class DeudaProveedorDTO {
    
    private String proveedorId;
    private String proveedorNombre;
    private String telefonoProveedor;
    private String emailProveedor;
    private BigDecimal totalCompras;
    private BigDecimal totalPagos;
    private BigDecimal deudaPendiente;
    private String estadoDeuda; // "verde" o "amarillo"
    private OffsetDateTime fechaOrdenMasAntigua;
    private int cantidadOrdenesPendientes;
    
    // Constructor
    public DeudaProveedorDTO() {
    }
    
    // Constructor de compatibilidad hacia atrás (para ComprasService)
    public DeudaProveedorDTO(String proveedorId, String proveedorNombre, 
                            BigDecimal totalCompras, BigDecimal totalPagos) {
        this.proveedorId = proveedorId;
        this.proveedorNombre = proveedorNombre;
        this.totalCompras = totalCompras != null ? totalCompras : BigDecimal.ZERO;
        this.totalPagos = totalPagos != null ? totalPagos : BigDecimal.ZERO;
        this.deudaPendiente = this.totalCompras.subtract(this.totalPagos);
        this.estadoDeuda = calcularEstadoDeuda();
        // Valores por defecto para nuevos campos
        this.telefonoProveedor = null;
        this.emailProveedor = null;
        this.fechaOrdenMasAntigua = null;
        this.cantidadOrdenesPendientes = 0;
    }
    
    public DeudaProveedorDTO(String proveedorId, String proveedorNombre, String telefonoProveedor,
                            String emailProveedor, BigDecimal totalCompras, BigDecimal totalPagos,
                            OffsetDateTime fechaOrdenMasAntigua, int cantidadOrdenesPendientes) {
        this.proveedorId = proveedorId;
        this.proveedorNombre = proveedorNombre;
        this.telefonoProveedor = telefonoProveedor;
        this.emailProveedor = emailProveedor;
        this.totalCompras = totalCompras != null ? totalCompras : BigDecimal.ZERO;
        this.totalPagos = totalPagos != null ? totalPagos : BigDecimal.ZERO;
        this.deudaPendiente = this.totalCompras.subtract(this.totalPagos);
        this.estadoDeuda = calcularEstadoDeuda();
        this.fechaOrdenMasAntigua = fechaOrdenMasAntigua;
        this.cantidadOrdenesPendientes = cantidadOrdenesPendientes;
    }
    
    private String calcularEstadoDeuda() {
        if (deudaPendiente.compareTo(BigDecimal.ZERO) <= 0) {
            return "verde"; // Sin deuda
        } else {
            return "amarillo"; // Con deuda
        }
    }
    
    // Getters y Setters
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
    
    public BigDecimal getTotalCompras() {
        return totalCompras;
    }
    
    public void setTotalCompras(BigDecimal totalCompras) {
        this.totalCompras = totalCompras;
        actualizarDeuda();
    }
    
    public BigDecimal getTotalPagos() {
        return totalPagos;
    }
    
    public void setTotalPagos(BigDecimal totalPagos) {
        this.totalPagos = totalPagos;
        actualizarDeuda();
    }
    
    public BigDecimal getDeudaPendiente() {
        return deudaPendiente;
    }
    
    public void setDeudaPendiente(BigDecimal deudaPendiente) {
        this.deudaPendiente = deudaPendiente;
        this.estadoDeuda = calcularEstadoDeuda();
    }
    
    public String getEstadoDeuda() {
        return estadoDeuda;
    }
    
    public void setEstadoDeuda(String estadoDeuda) {
        this.estadoDeuda = estadoDeuda;
    }

    public String getTelefonoProveedor() {
        return telefonoProveedor;
    }

    public void setTelefonoProveedor(String telefonoProveedor) {
        this.telefonoProveedor = telefonoProveedor;
    }

    public String getEmailProveedor() {
        return emailProveedor;
    }

    public void setEmailProveedor(String emailProveedor) {
        this.emailProveedor = emailProveedor;
    }

    public OffsetDateTime getFechaOrdenMasAntigua() {
        return fechaOrdenMasAntigua;
    }

    public void setFechaOrdenMasAntigua(OffsetDateTime fechaOrdenMasAntigua) {
        this.fechaOrdenMasAntigua = fechaOrdenMasAntigua;
    }

    public int getCantidadOrdenesPendientes() {
        return cantidadOrdenesPendientes;
    }

    public void setCantidadOrdenesPendientes(int cantidadOrdenesPendientes) {
        this.cantidadOrdenesPendientes = cantidadOrdenesPendientes;
    }
    
    private void actualizarDeuda() {
        if (totalCompras != null && totalPagos != null) {
            this.deudaPendiente = totalCompras.subtract(totalPagos);
            this.estadoDeuda = calcularEstadoDeuda();
        }
    }
}