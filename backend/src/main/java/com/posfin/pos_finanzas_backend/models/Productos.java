package com.posfin.pos_finanzas_backend.models;

import jakarta.persistence.*;

@Entity
@Table(name = "productos")
public class Productos {

    @Id
    // UUID se asignará manualmente antes de persistir
    private String id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "categorias_productos_id", nullable = false)
    private String categoriasProductosId;

    @Column(name = "proveedor_id", nullable = false)
    private String proveedorId;

    @Column(name = "estados_id", nullable = false)
    private String estadosId;

    // Constructor
    public Productos() {
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

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCategoriaProductosId() {
        return categoriasProductosId;
    }

    public void setCategoriaProductosId(String categoriaProductosId) {
        this.categoriasProductosId = categoriaProductosId;
    }

    public String getProveedorId() {
        return proveedorId;
    }

    public void setProveedorId(String proveedorId) {
        this.proveedorId = proveedorId;
    }

    public String getEstadosId() {
        return estadosId;
    }

    public void setEstadosId(String estadosId) {
        this.estadosId = estadosId;
    }
}
