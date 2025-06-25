package com.posfin.pos_finanzas_backend.models;

import jakarta.persistence.*;

@Entity
@Table(name = "productos")
public class Productos {

    @Id
    private String id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categorias_productos_id", nullable = false)
    private CategoriasProductos categoriasProductos;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Personas proveedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estados_id", nullable = false)
    private Estados estados;

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

    public CategoriasProductos getCategoriasProductos() {
        return categoriasProductos;
    }

    public void setCategoriasProductos(CategoriasProductos categoriasProductos) {
        this.categoriasProductos = categoriasProductos;
    }

    public Personas getProveedor() {
        return proveedor;
    }

    public void setProveedor(Personas proveedor) {
        this.proveedor = proveedor;
    }

    public Estados getEstados() {
        return estados;
    }

    public void setEstados(Estados estados) {
        this.estados = estados;
    }
}
