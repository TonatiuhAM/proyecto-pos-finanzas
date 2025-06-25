package com.posfin.pos_finanzas_backend.dtos;

public class PersonasDTO {
    private String id;
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String rfc;
    private String telefono;
    private String email;

    // Campos aplanados de la relación con Estados
    private String estadosId;
    private String estadosEstado;

    // Campos aplanados de la relación con CategoriaPersonas
    private String categoriaPersonasId;
    private String categoriaPersonasCategoria;

    // Constructor
    public PersonasDTO() {
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

    public String getEstadosId() {
        return estadosId;
    }

    public void setEstadosId(String estadosId) {
        this.estadosId = estadosId;
    }

    public String getEstadosEstado() {
        return estadosEstado;
    }

    public void setEstadosEstado(String estadosEstado) {
        this.estadosEstado = estadosEstado;
    }

    public String getCategoriaPersonasId() {
        return categoriaPersonasId;
    }

    public void setCategoriaPersonasId(String categoriaPersonasId) {
        this.categoriaPersonasId = categoriaPersonasId;
    }

    public String getCategoriaPersonasCategoria() {
        return categoriaPersonasCategoria;
    }

    public void setCategoriaPersonasCategoria(String categoriaPersonasCategoria) {
        this.categoriaPersonasCategoria = categoriaPersonasCategoria;
    }
}
