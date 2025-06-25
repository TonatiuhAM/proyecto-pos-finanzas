package com.posfin.pos_finanzas_backend.models;

import jakarta.persistence.*;

@Entity
@Table(name = "personas")
public class Personas {

    @Id
    private String id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "apellido_paterno")
    private String apellidoPaterno;

    @Column(name = "apellido_materno")
    private String apellidoMaterno;

    @Column(name = "rfc")
    private String rfc;

    @Column(name = "telefono", nullable = false)
    private String telefono;

    @Column(name = "email")
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estados_id", nullable = false)
    private Estados estados;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_personas_id", nullable = false)
    private CategoriaPersonas categoriaPersonas;

    // Constructor
    public Personas() {
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

    public String getApellidoPaterno() {
        return apellidoPaterno;
    }

    public void setApellidoPaterno(String apellidoPaterno) {
        this.apellidoPaterno = apellidoPaterno;
    }

    public String getApellidoMaterno() {
        return apellidoMaterno;
    }

    public void setApellidoMaterno(String apellidoMaterno) {
        this.apellidoMaterno = apellidoMaterno;
    }

    public String getRfc() {
        return rfc;
    }

    public void setRfc(String rfc) {
        this.rfc = rfc;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Estados getEstados() {
        return estados;
    }

    public void setEstados(Estados estados) {
        this.estados = estados;
    }

    public CategoriaPersonas getCategoriaPersonas() {
        return categoriaPersonas;
    }

    public void setCategoriaPersonas(CategoriaPersonas categoriaPersonas) {
        this.categoriaPersonas = categoriaPersonas;
    }
}
