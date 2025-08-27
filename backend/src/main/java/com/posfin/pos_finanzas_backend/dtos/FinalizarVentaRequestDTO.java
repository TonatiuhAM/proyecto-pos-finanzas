package com.posfin.pos_finanzas_backend.dtos;

/**
 * DTO para la solicitud de finalizar una venta desde un workspace.
 * Contiene la información necesaria para convertir las órdenes temporales
 * en una venta permanente.
 */
public class FinalizarVentaRequestDTO {
    private String metodoPagoId;
    private String clienteId; // Opcional, puede ser null para cliente genérico
    private String usuarioId; // ID del mesero/empleado que procesa la venta

    // Constructor vacío
    public FinalizarVentaRequestDTO() {
    }

    // Constructor completo
    public FinalizarVentaRequestDTO(String metodoPagoId, String clienteId, String usuarioId) {
        this.metodoPagoId = metodoPagoId;
        this.clienteId = clienteId;
        this.usuarioId = usuarioId;
    }

    // Getters y Setters
    public String getMetodoPagoId() {
        return metodoPagoId;
    }

    public void setMetodoPagoId(String metodoPagoId) {
        this.metodoPagoId = metodoPagoId;
    }

    public String getClienteId() {
        return clienteId;
    }

    public void setClienteId(String clienteId) {
        this.clienteId = clienteId;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }
}
