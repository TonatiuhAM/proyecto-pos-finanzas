package com.posfin.pos_finanzas_backend.dtos;

import java.math.BigDecimal;

/**
 * DTO para registrar pagos a proveedores
 */
public class PagoRequestDTO {
    
    private String proveedorId;
    private String ordenCompraId;
    private BigDecimal montoPagado;
    private String metodoPagoId;
    private boolean pagarTodoElTotal;
    
    // Constructor
    public PagoRequestDTO() {
    }
    
    public PagoRequestDTO(String proveedorId, String ordenCompraId, BigDecimal montoPagado, 
                         String metodoPagoId, boolean pagarTodoElTotal) {
        this.proveedorId = proveedorId;
        this.ordenCompraId = ordenCompraId;
        this.montoPagado = montoPagado;
        this.metodoPagoId = metodoPagoId;
        this.pagarTodoElTotal = pagarTodoElTotal;
    }
    
    // Getters y Setters
    public String getProveedorId() {
        return proveedorId;
    }
    
    public void setProveedorId(String proveedorId) {
        this.proveedorId = proveedorId;
    }
    
    public String getOrdenCompraId() {
        return ordenCompraId;
    }
    
    public void setOrdenCompraId(String ordenCompraId) {
        this.ordenCompraId = ordenCompraId;
    }
    
    public BigDecimal getMontoPagado() {
        return montoPagado;
    }
    
    public void setMontoPagado(BigDecimal montoPagado) {
        this.montoPagado = montoPagado;
    }
    
    public String getMetodoPagoId() {
        return metodoPagoId;
    }
    
    public void setMetodoPagoId(String metodoPagoId) {
        this.metodoPagoId = metodoPagoId;
    }
    
    public boolean isPagarTodoElTotal() {
        return pagarTodoElTotal;
    }
    
    public void setPagarTodoElTotal(boolean pagarTodoElTotal) {
        this.pagarTodoElTotal = pagarTodoElTotal;
    }
}