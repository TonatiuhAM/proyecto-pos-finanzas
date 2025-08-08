package com.posfin.pos_finanzas_backend.dtos;

/**
 * DTO para recibir datos de cambio de estado de empleados
 */
public class EmpleadoEstadoRequestDTO {

    private String estado;

    // Constructor vacío
    public EmpleadoEstadoRequestDTO() {
    }

    // Constructor con estado
    public EmpleadoEstadoRequestDTO(String estado) {
        this.estado = estado;
    }

    // Getters y Setters
    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
