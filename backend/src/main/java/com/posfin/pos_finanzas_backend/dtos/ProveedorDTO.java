package com.posfin.pos_finanzas_backend.dtos;

import java.math.BigDecimal;

/**
 * DTO para representar proveedores con información de deuda
 */
public class ProveedorDTO {
    
    private String id;
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String telefono;
    private String email;
    private String estadoNombre;
    private BigDecimal deudaPendiente;
    private String estadoDeuda; // "verde" o "amarillo"
    
    // Constructores
    public ProveedorDTO() {
    }
    
    public ProveedorDTO(String id, String nombre, String apellidoPaterno, 
                       String apellidoMaterno, String telefono, String email, 
                       String estadoNombre, BigDecimal deudaPendiente) {
        this.id = id;
        this.nombre = nombre;
        this.apellidoPaterno = apellidoPaterno;
        this.apellidoMaterno = apellidoMaterno;
        this.telefono = telefono;
        this.email = email;
        this.estadoNombre = estadoNombre;
        this.deudaPendiente = deudaPendiente;
        this.estadoDeuda = calcularEstadoDeuda(deudaPendiente);
    }
    
    private String calcularEstadoDeuda(BigDecimal deuda) {
        if (deuda == null || deuda.compareTo(BigDecimal.ZERO) <= 0) {
            return "verde"; // Sin deuda
        } else {
            return "amarillo"; // Con deuda
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
    
    public String getEstadoNombre() {
        return estadoNombre;
    }
    
    public void setEstadoNombre(String estadoNombre) {
        this.estadoNombre = estadoNombre;
    }
    
    public BigDecimal getDeudaPendiente() {
        return deudaPendiente;
    }
    
    public void setDeudaPendiente(BigDecimal deudaPendiente) {
        this.deudaPendiente = deudaPendiente;
        this.estadoDeuda = calcularEstadoDeuda(deudaPendiente);
    }
    
    public String getEstadoDeuda() {
        return estadoDeuda;
    }
    
    public void setEstadoDeuda(String estadoDeuda) {
        this.estadoDeuda = estadoDeuda;
    }
    
    /**
     * Obtiene el nombre completo del proveedor
     */
    public String getNombreCompleto() {
        StringBuilder nombreCompleto = new StringBuilder(nombre);
        if (apellidoPaterno != null && !apellidoPaterno.trim().isEmpty()) {
            nombreCompleto.append(" ").append(apellidoPaterno);
        }
        if (apellidoMaterno != null && !apellidoMaterno.trim().isEmpty()) {
            nombreCompleto.append(" ").append(apellidoMaterno);
        }
        return nombreCompleto.toString();
    }
}