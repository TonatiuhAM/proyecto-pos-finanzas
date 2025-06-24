package com.posfin.pos_finanzas_backend.models;

import jakarta.persistence.*;

@Entity
@Table(name = "tipo_movimientos")
public class TipoMovimientos {
    @Id
    // UUID se asignará manualmente antes de persistir
    private String id;

    @Column(name = "movimiento", nullable = false)
    private String movimiento;

    // Constructor
    public TipoMovimientos() {
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

    public String getMovimiento() {
        return movimiento;
    }

    public void setMovimiento(String movimiento) {
        this.movimiento = movimiento;
    }
}
