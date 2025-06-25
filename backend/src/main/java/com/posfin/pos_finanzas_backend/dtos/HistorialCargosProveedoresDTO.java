package com.posfin.pos_finanzas_backend.dtos;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * DTO (Data Transfer Object) para la entidad HistorialCargosProveedores.
 * Se utiliza para controlar y dar forma a los datos que se envían
 * a través de la API, desacoplando la API del modelo de la base de datos.
 */
public class HistorialCargosProveedoresDTO {

    private String id;
    private BigDecimal montoPagado;
    private OffsetDateTime fecha;

    // Campos "aplanados" de la entidad relacionada Personas (Proveedor)
    private String personaId;
    private String personaNombre;
    private String personaApellidoPaterno;
    private String personaApellidoMaterno;
    private String personaTelefono;
    private String personaEmail;

    // Campos "aplanados" de la entidad relacionada OrdenesDeCompras
    private String ordenDeCompraId;
    private BigDecimal ordenDeCompraTotalCompra;
    private OffsetDateTime ordenDeCompraFechaOrden;

    // Constructor vacío
    public HistorialCargosProveedoresDTO() {
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BigDecimal getMontoPagado() {
        return montoPagado;
    }

    public void setMontoPagado(BigDecimal montoPagado) {
        this.montoPagado = montoPagado;
    }

    public OffsetDateTime getFecha() {
        return fecha;
    }

    public void setFecha(OffsetDateTime fecha) {
        this.fecha = fecha;
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

    public String getOrdenDeCompraId() {
        return ordenDeCompraId;
    }

    public void setOrdenDeCompraId(String ordenDeCompraId) {
        this.ordenDeCompraId = ordenDeCompraId;
    }

    public BigDecimal getOrdenDeCompraTotalCompra() {
        return ordenDeCompraTotalCompra;
    }

    public void setOrdenDeCompraTotalCompra(BigDecimal ordenDeCompraTotalCompra) {
        this.ordenDeCompraTotalCompra = ordenDeCompraTotalCompra;
    }

    public OffsetDateTime getOrdenDeCompraFechaOrden() {
        return ordenDeCompraFechaOrden;
    }

    public void setOrdenDeCompraFechaOrden(OffsetDateTime ordenDeCompraFechaOrden) {
        this.ordenDeCompraFechaOrden = ordenDeCompraFechaOrden;
    }
}
