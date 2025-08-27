package com.posfin.pos_finanzas_backend.dtos;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO para crear nuevas órdenes de compra
 */
public class CompraRequestDTO {
    
    private String proveedorId;
    private String metodoPagoId; // Por defecto "Efectivo"
    private List<ProductoCompraDTO> productos;
    
    // Constructor
    public CompraRequestDTO() {
    }
    
    // Getters y Setters
    public String getProveedorId() {
        return proveedorId;
    }
    
    public void setProveedorId(String proveedorId) {
        this.proveedorId = proveedorId;
    }
    
    public String getMetodoPagoId() {
        return metodoPagoId;
    }
    
    public void setMetodoPagoId(String metodoPagoId) {
        this.metodoPagoId = metodoPagoId;
    }
    
    public List<ProductoCompraDTO> getProductos() {
        return productos;
    }
    
    public void setProductos(List<ProductoCompraDTO> productos) {
        this.productos = productos;
    }
    
    /**
     * Clase interna para productos en la compra
     */
    public static class ProductoCompraDTO {
        private String productoId;
        private BigDecimal cantidadPz;
        private BigDecimal cantidadKg;
        
        public ProductoCompraDTO() {
        }
        
        public ProductoCompraDTO(String productoId, BigDecimal cantidadPz, BigDecimal cantidadKg) {
            this.productoId = productoId;
            this.cantidadPz = cantidadPz;
            this.cantidadKg = cantidadKg;
        }
        
        public String getProductoId() {
            return productoId;
        }
        
        public void setProductoId(String productoId) {
            this.productoId = productoId;
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
    }
}