package com.posfin.pos_finanzas_backend.models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "ordenes_de_compras")
public class OrdenesDeCompras {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "persona_id", nullable = false)
    private Personas persona;

    @Column(name = "fecha_orden", nullable = false)
    private OffsetDateTime fechaOrden;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estados_id", nullable = false)
    private Estados estado;

    @Column(name = "total_compra", nullable = false)
    private BigDecimal totalCompra;

    // Constructor
    public OrdenesDeCompras() {
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

    public OffsetDateTime getFechaOrden() {
        return fechaOrden;
    }

    public void setFechaOrden(OffsetDateTime fechaOrden) {
        this.fechaOrden = fechaOrden;
    }

    public Estados getEstado() {
        return estado;
    }

    public void setEstado(Estados estado) {
        this.estado = estado;
    }

    public BigDecimal getTotalCompra() {
        return totalCompra;
    }

    public void setTotalCompra(BigDecimal totalCompra) {
        this.totalCompra = totalCompra;
    }
}
