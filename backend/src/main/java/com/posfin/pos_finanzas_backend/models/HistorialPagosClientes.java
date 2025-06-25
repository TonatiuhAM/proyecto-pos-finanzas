package com.posfin.pos_finanzas_backend.models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "historial_pagos_clientes")
public class HistorialPagosClientes {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personas_id", nullable = false)
    private Personas persona;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_de_venta_id", nullable = false)
    private OrdenesDeVentas ordenDeVenta;

    @Column(name = "monto_pagado", nullable = false)
    private BigDecimal montoPagado;

    @Column(name = "fecha", nullable = false)
    private OffsetDateTime fecha;

    // Constructor
    public HistorialPagosClientes() {
    }

    // Función para generar un ID único antes de persistir
    @PrePersist
    public void generateId() {
        if (this.id == null) {
            this.id = java.util.UUID.randomUUID().toString();
        }
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Personas getPersona() {
        return persona;
    }

    public void setPersona(Personas persona) {
        this.persona = persona;
    }

    public OrdenesDeVentas getOrdenDeVenta() {
        return ordenDeVenta;
    }

    public void setOrdenDeVenta(OrdenesDeVentas ordenDeVenta) {
        this.ordenDeVenta = ordenDeVenta;
    }

    public BigDecimal getMontoPagado() {
        return montoPagado;
    }

    public void setMontoPagado(BigDecimal montoPagado) {
        this.montoPagado = montoPagado;
    }

    public OffsetDateTime getFecha() {
        return fecha;
    }

    public void setFecha(OffsetDateTime fecha) {
        this.fecha = fecha;
    }
}
