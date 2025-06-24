package com.posfin.pos_finanzas_backend.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "inventarios")
public class Inventarios {
    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productos_id", nullable = false) // Asegúrate que 'name' coincida con tu columna en la BD
    private Productos producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ubicaciones_id", nullable = false) // Asegúrate que 'name' coincida con tu columna en la BD
    private Ubicaciones ubicacion;

    @Column(name = "cantidad_pz")
    private Integer cantidadPz;

    @Column(name = "cantidad_kg")
    private Integer cantidadKg;

    @Column(name = "cantidad_minima", nullable = false)
    private Integer cantidadMinima;

    @Column(name = "cantidad_maxima", nullable = false)
    private Integer cantidadMaxima;

    // Constructor
    public Inventarios() {
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

    public Productos getProducto() {
        return producto;
    }

    public void setProducto(Productos productoId) {
        this.producto = productoId;
    }

    public Ubicaciones getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(Ubicaciones ubicacionesId) {
        this.ubicacion = ubicacionesId;
    }

    public Integer getCantidadPz() {
        return cantidadPz;
    }

    public void setCantidadPz(Integer cantidadPz) {
        this.cantidadPz = cantidadPz;
    }

    public Integer getCantidadKg() {
        return cantidadKg;
    }

    public void setCantidadKg(Integer cantidadKg) {
        this.cantidadKg = cantidadKg;
    }

    public int getCantidadMinima() {
        return cantidadMinima;
    }

    public void setCantidadMinima(int cantidadMinima) {
        this.cantidadMinima = cantidadMinima;
    }

    public int getCantidadMaxima() {
        return cantidadMaxima;
    }

    public void setCantidadMaxima(int cantidadMaxima) {
        this.cantidadMaxima = cantidadMaxima;
    }
}
