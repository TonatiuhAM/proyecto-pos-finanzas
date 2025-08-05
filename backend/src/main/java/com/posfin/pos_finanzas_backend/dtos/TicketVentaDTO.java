package com.posfin.pos_finanzas_backend.dtos;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO para representar un ticket de venta pre-pago.
 * Contiene la información consolidada de todas las órdenes de un workspace
 * para mostrar al cliente antes del pago.
 */
public class TicketVentaDTO {
    private String workspaceId;
    private String workspaceNombre;
    private List<ProductoTicketDTO> productos;
    private BigDecimal totalGeneral;
    private int cantidadProductos;

    // Constructor vacío
    public TicketVentaDTO() {
    }

    // Constructor completo
    public TicketVentaDTO(String workspaceId, String workspaceNombre, List<ProductoTicketDTO> productos,
            BigDecimal totalGeneral, int cantidadProductos) {
        this.workspaceId = workspaceId;
        this.workspaceNombre = workspaceNombre;
        this.productos = productos;
        this.totalGeneral = totalGeneral;
        this.cantidadProductos = cantidadProductos;
    }

    // Getters y Setters
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

    public List<ProductoTicketDTO> getProductos() {
        return productos;
    }

    public void setProductos(List<ProductoTicketDTO> productos) {
        this.productos = productos;
    }

    public BigDecimal getTotalGeneral() {
        return totalGeneral;
    }

    public void setTotalGeneral(BigDecimal totalGeneral) {
        this.totalGeneral = totalGeneral;
    }

    public int getCantidadProductos() {
        return cantidadProductos;
    }

    public void setCantidadProductos(int cantidadProductos) {
        this.cantidadProductos = cantidadProductos;
    }

    /**
     * DTO interno para representar un producto en el ticket.
     */
    public static class ProductoTicketDTO {
        private String productoId;
        private String productoNombre;
        private BigDecimal cantidadPz;
        private BigDecimal cantidadKg;
        private BigDecimal precioUnitario;
        private BigDecimal totalPorProducto;

        // Constructor vacío
        public ProductoTicketDTO() {
        }

        // Constructor completo
        public ProductoTicketDTO(String productoId, String productoNombre, BigDecimal cantidadPz,
                BigDecimal cantidadKg, BigDecimal precioUnitario, BigDecimal totalPorProducto) {
            this.productoId = productoId;
            this.productoNombre = productoNombre;
            this.cantidadPz = cantidadPz;
            this.cantidadKg = cantidadKg;
            this.precioUnitario = precioUnitario;
            this.totalPorProducto = totalPorProducto;
        }

        // Getters y Setters
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

        public BigDecimal getPrecioUnitario() {
            return precioUnitario;
        }

        public void setPrecioUnitario(BigDecimal precioUnitario) {
            this.precioUnitario = precioUnitario;
        }

        public BigDecimal getTotalPorProducto() {
            return totalPorProducto;
        }

        public void setTotalPorProducto(BigDecimal totalPorProducto) {
            this.totalPorProducto = totalPorProducto;
        }
    }
}
