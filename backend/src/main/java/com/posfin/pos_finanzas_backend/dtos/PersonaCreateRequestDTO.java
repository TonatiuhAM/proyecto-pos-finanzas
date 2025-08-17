package com.posfin.pos_finanzas_backend.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class PersonaCreateRequestDTO {
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombre;
    
    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(min = 2, max = 100, message = "Los apellidos deben tener entre 2 y 100 caracteres")
    private String apellidos;
    
    @Pattern(regexp = "^[A-ZÑ&]{3,4}[0-9]{6}[A-Z0-9]{3}$", 
             message = "El RFC debe tener el formato correcto (ej: XAXX010101000)")
    private String rfc;
    
    @Email(message = "El email debe tener un formato válido")
    private String email;
    
    @Pattern(regexp = "^[0-9]{10}$", 
             message = "El teléfono debe contener exactamente 10 dígitos")
    private String telefono;
    
    @Size(max = 255, message = "La dirección no puede exceder 255 caracteres")
    private String direccion;
    
    @NotBlank(message = "El ID de categoría es obligatorio")
    private String idCategoriaPersona;

    // Constructores
    public PersonaCreateRequestDTO() {}

    public PersonaCreateRequestDTO(String nombre, String apellidos, String rfc, String email, 
                                 String telefono, String direccion, String idCategoriaPersona) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.rfc = rfc;
        this.email = email;
        this.telefono = telefono;
        this.direccion = direccion;
        this.idCategoriaPersona = idCategoriaPersona;
    }

    // Getters y Setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
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

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getIdCategoriaPersona() {
        return idCategoriaPersona;
    }

    public void setIdCategoriaPersona(String idCategoriaPersona) {
        this.idCategoriaPersona = idCategoriaPersona;
    }
}