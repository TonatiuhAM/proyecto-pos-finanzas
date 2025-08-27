package com.posfin.pos_finanzas_backend.dtos;

import java.time.LocalDateTime;

public class PersonaResponseDTO {
    
    private String id;
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String nombreCompleto;
    private String rfc;
    private String email;
    private String telefono;
    private String direccion; // Campo adicional para compatibilidad
    private String idCategoriaPersona;
    private String nombreCategoria;
    private String idEstado;
    private String nombreEstado;

    // Constructores
    public PersonaResponseDTO() {}

    public PersonaResponseDTO(String id, String nombre, String apellidoPaterno, String apellidoMaterno, 
                            String rfc, String email, String telefono, String idCategoriaPersona, 
                            String nombreCategoria, String idEstado, String nombreEstado) {
        this.id = id;
        this.nombre = nombre;
        this.apellidoPaterno = apellidoPaterno;
        this.apellidoMaterno = apellidoMaterno;
        this.nombreCompleto = nombre + " " + (apellidoPaterno != null ? apellidoPaterno : "") + 
                             " " + (apellidoMaterno != null ? apellidoMaterno : "");
        this.rfc = rfc;
        this.email = email;
        this.telefono = telefono;
        this.direccion = null; // No tenemos direccion en el modelo actual
        this.idCategoriaPersona = idCategoriaPersona;
        this.nombreCategoria = nombreCategoria;
        this.idEstado = idEstado;
        this.nombreEstado = nombreEstado;
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
        this.nombreCompleto = nombre + " " + (apellidoPaterno != null ? apellidoPaterno : "") + 
                             " " + (apellidoMaterno != null ? apellidoMaterno : "");
    }

    public String getApellidoPaterno() {
        return apellidoPaterno;
    }

    public void setApellidoPaterno(String apellidoPaterno) {
        this.apellidoPaterno = apellidoPaterno;
        this.nombreCompleto = (nombre != null ? nombre : "") + " " + apellidoPaterno + 
                             " " + (apellidoMaterno != null ? apellidoMaterno : "");
    }

    public String getApellidoMaterno() {
        return apellidoMaterno;
    }

    public void setApellidoMaterno(String apellidoMaterno) {
        this.apellidoMaterno = apellidoMaterno;
        this.nombreCompleto = (nombre != null ? nombre : "") + " " + 
                             (apellidoPaterno != null ? apellidoPaterno : "") + " " + apellidoMaterno;
    }

    // Método de compatibilidad para el frontend
    public String getApellidos() {
        String apellidos = "";
        if (apellidoPaterno != null) {
            apellidos += apellidoPaterno;
        }
        if (apellidoMaterno != null) {
            if (!apellidos.isEmpty()) {
                apellidos += " ";
            }
            apellidos += apellidoMaterno;
        }
        return apellidos.isEmpty() ? null : apellidos;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getRfc() {
        return rfc;
    }

    public void setRfc(String rfc) {
        this.rfc = rfc;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getIdCategoriaPersona() {
        return idCategoriaPersona;
    }

    public void setIdCategoriaPersona(String idCategoriaPersona) {
        this.idCategoriaPersona = idCategoriaPersona;
    }

    public String getNombreCategoria() {
        return nombreCategoria;
    }

    public void setNombreCategoria(String nombreCategoria) {
        this.nombreCategoria = nombreCategoria;
    }

    public String getIdEstado() {
        return idEstado;
    }

    public void setIdEstado(String idEstado) {
        this.idEstado = idEstado;
    }

    public String getNombreEstado() {
        return nombreEstado;
    }

    public void setNombreEstado(String nombreEstado) {
        this.nombreEstado = nombreEstado;
    }
}