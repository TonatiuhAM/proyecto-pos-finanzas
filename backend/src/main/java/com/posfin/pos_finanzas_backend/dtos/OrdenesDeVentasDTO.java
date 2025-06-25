package com.posfin.pos_finanzas_backend.dtos;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * DTO (Data Transfer Object) para la entidad OrdenesDeVentas.
 * Se utiliza para controlar y dar forma a los datos que se envían
 * a través de la API, desacoplando la API del modelo de la base de datos.
 */
public class OrdenesDeVentasDTO {

    private String id;
    private OffsetDateTime fechaOrden;
    private BigDecimal totalVenta;

    // Campos "aplanados" de la entidad relacionada Personas
    private String personaId;
    private String personaNombre;
    private String personaApellidoPaterno;
    private String personaApellidoMaterno;
    private String personaTelefono;
    private String personaEmail;

    // Campos "aplanados" de la entidad relacionada Usuarios
    private String usuarioId;
    private String usuarioNombre;
    private String usuarioTelefono;

    // Campos "aplanados" de la entidad relacionada MetodosPago
    private String metodoPagoId;
    private String metodoPagoNombre;

    // Constructor vacío
    public OrdenesDeVentasDTO() {
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public OffsetDateTime getFechaOrden() {
        return fechaOrden;
    }

    public void setFechaOrden(OffsetDateTime fechaOrden) {
        this.fechaOrden = fechaOrden;
    }

    public BigDecimal getTotalVenta() {
        return totalVenta;
    }

    public void setTotalVenta(BigDecimal totalVenta) {
        this.totalVenta = totalVenta;
    }

    public String getPersonaId() {
        return personaId;
    }

    public void setPersonaId(String personaId) {
        this.personaId = personaId;
    }

    public String getPersonaNombre() {
        return personaNombre;
    }

    public void setPersonaNombre(String personaNombre) {
        this.personaNombre = personaNombre;
    }

    public String getPersonaApellidoPaterno() {
        return personaApellidoPaterno;
    }

    public void setPersonaApellidoPaterno(String personaApellidoPaterno) {
        this.personaApellidoPaterno = personaApellidoPaterno;
    }

    public String getPersonaApellidoMaterno() {
        return personaApellidoMaterno;
    }

    public void setPersonaApellidoMaterno(String personaApellidoMaterno) {
        this.personaApellidoMaterno = personaApellidoMaterno;
    }

    public String getPersonaTelefono() {
        return personaTelefono;
    }

    public void setPersonaTelefono(String personaTelefono) {
        this.personaTelefono = personaTelefono;
    }

    public String getPersonaEmail() {
        return personaEmail;
    }

    public void setPersonaEmail(String personaEmail) {
        this.personaEmail = personaEmail;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getUsuarioNombre() {
        return usuarioNombre;
    }

    public void setUsuarioNombre(String usuarioNombre) {
        this.usuarioNombre = usuarioNombre;
    }

    public String getUsuarioTelefono() {
        return usuarioTelefono;
    }

    public void setUsuarioTelefono(String usuarioTelefono) {
        this.usuarioTelefono = usuarioTelefono;
    }

    public String getMetodoPagoId() {
        return metodoPagoId;
    }

    public void setMetodoPagoId(String metodoPagoId) {
        this.metodoPagoId = metodoPagoId;
    }

    public String getMetodoPagoNombre() {
        return metodoPagoNombre;
    }

    public void setMetodoPagoNombre(String metodoPagoNombre) {
        this.metodoPagoNombre = metodoPagoNombre;
    }
}
