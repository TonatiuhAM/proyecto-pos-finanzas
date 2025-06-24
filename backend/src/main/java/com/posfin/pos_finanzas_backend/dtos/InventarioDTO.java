package com.posfin.pos_finanzas_backend.dtos;

/**
 * DTO (Data Transfer Object) para la entidad Inventarios.
 * Se utiliza para controlar y dar forma a los datos que se envían
 * a través de la API, desacoplando la API del modelo de la base de datos.
 */
public class InventarioDTO {

    private String id;
    private Integer cantidadPz;
    private Integer cantidadKg;
    private int cantidadMinima;
    private int cantidadMaxima;

    // Campos "aplanados" de la entidad relacionada Productos
    private String productoId;
    private String productoNombre;

    // Campos "aplanados" de la entidad relacionada Ubicaciones
    private String ubicacionId;
    private String ubicacionNombre;

    // Constructor vacío
    public InventarioDTO() {
    }

    // --- Getters y Setters para todas las propiedades ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getCantidadPz() {
        return cantidadPz;
    }

    public void setCantidadPz(Integer cantidadPz) {
        this.cantidadPz = cantidadPz;
    }

    public Integer getCantidadKg() {
        return cantidadKg;
    }

    public void setCantidadKg(Integer cantidadKg) {
        this.cantidadKg = cantidadKg;
    }

    public int getCantidadMinima() {
        return cantidadMinima;
    }

    public void setCantidadMinima(int cantidadMinima) {
        this.cantidadMinima = cantidadMinima;
    }

    public int getCantidadMaxima() {
        return cantidadMaxima;
    }

    public void setCantidadMaxima(int cantidadMaxima) {
        this.cantidadMaxima = cantidadMaxima;
    }

    public String getProductoId() {
        return productoId;
    }

    public void setProductoId(String productoId) {
        this.productoId = productoId;
    }

    public String getProductoNombre() {
        return productoNombre;
    }

    public void setProductoNombre(String productoNombre) {
        this.productoNombre = productoNombre;
    }

    public String getUbicacionId() {
        return ubicacionId;
    }

    public void setUbicacionId(String ubicacionId) {
        this.ubicacionId = ubicacionId;
    }

    public String getUbicacionNombre() {
        return ubicacionNombre;
    }

    public void setUbicacionNombre(String ubicacionNombre) {
        this.ubicacionNombre = ubicacionNombre;
    }
}