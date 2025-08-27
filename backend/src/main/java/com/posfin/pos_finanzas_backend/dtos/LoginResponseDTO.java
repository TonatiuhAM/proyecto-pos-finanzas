package com.posfin.pos_finanzas_backend.dtos;

/**
 * DTO para la respuesta del login exitoso
 * Incluye token JWT y información del usuario autenticado con su rol
 */
public class LoginResponseDTO {

    private String token;
    private String usuario;
    private String rolNombre;
    private String rolId;
    private long expiresIn; // En milisegundos

    // Constructor vacío
    public LoginResponseDTO() {
    }

    // Constructor completo
    public LoginResponseDTO(String token, String usuario, String rolNombre, String rolId, long expiresIn) {
        this.token = token;
        this.usuario = usuario;
        this.rolNombre = rolNombre;
        this.rolId = rolId;
        this.expiresIn = expiresIn;
    }

    // Getters y Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getRolNombre() {
        return rolNombre;
    }

    public void setRolNombre(String rolNombre) {
        this.rolNombre = rolNombre;
    }

    public String getRolId() {
        return rolId;
    }

    public void setRolId(String rolId) {
        this.rolId = rolId;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    @Override
    public String toString() {
        return "LoginResponseDTO{" +
                "token='" + (token != null ? "[PRESENTE]" : "null") + '\'' +
                ", usuario='" + usuario + '\'' +
                ", rolNombre='" + rolNombre + '\'' +
                ", rolId='" + rolId + '\'' +
                ", expiresIn=" + expiresIn +
                '}';
    }
}
