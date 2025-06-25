package com.posfin.pos_finanzas_backend.models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "ordenes_de_ventas")
public class OrdenesDeVentas {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personas_id", nullable = false)
    private Personas persona;

    @Column(name = "fecha_orden", nullable = false)
    private OffsetDateTime fechaOrden;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuarios_id", nullable = false)
    private Usuarios usuario;

    @Column(name = "total_venta", nullable = false)
    private BigDecimal totalVenta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "metodos_pago_id", nullable = false)
    private MetodosPago metodoPago;

    // Constructor
    public OrdenesDeVentas() {
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

    public Usuarios getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuarios usuario) {
        this.usuario = usuario;
    }

    public BigDecimal getTotalVenta() {
        return totalVenta;
    }

    public void setTotalVenta(BigDecimal totalVenta) {
        this.totalVenta = totalVenta;
    }

    public MetodosPago getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(MetodosPago metodoPago) {
        this.metodoPago = metodoPago;
    }
}
