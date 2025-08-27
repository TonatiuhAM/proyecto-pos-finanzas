package com.posfin.pos_finanzas_backend.repositories;

import com.posfin.pos_finanzas_backend.models.HistorialCargosProveedores;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface HistorialCargosProveedoresRepository extends JpaRepository<HistorialCargosProveedores, String> {
    
    /**
     * Encuentra todos los pagos de un proveedor específico
     */
    List<HistorialCargosProveedores> findByPersonaId(String proveedorId);
    
    /**
     * Encuentra pagos por orden de compra específica
     */
    List<HistorialCargosProveedores> findByOrdenDeCompraId(String ordenCompraId);
    
    /**
     * Encuentra pagos de un proveedor ordenados por fecha descendente
     */
    @Query("SELECT h FROM HistorialCargosProveedores h WHERE h.persona.id = :proveedorId ORDER BY h.fecha DESC")
    List<HistorialCargosProveedores> findByProveedorIdOrderByFechaDesc(@Param("proveedorId") String proveedorId);
    
    /**
     * Calcula el total de pagos realizados a un proveedor
     */
    @Query("SELECT COALESCE(SUM(h.montoPagado), 0) FROM HistorialCargosProveedores h WHERE h.persona.id = :proveedorId")
    BigDecimal calcularTotalPagosProveedor(@Param("proveedorId") String proveedorId);
    
    /**
     * Calcula el total de pagos realizados para una orden de compra específica
     */
    @Query("SELECT COALESCE(SUM(h.montoPagado), 0) FROM HistorialCargosProveedores h WHERE h.ordenDeCompra.id = :ordenCompraId")
    BigDecimal calcularTotalPagosOrdenCompra(@Param("ordenCompraId") String ordenCompraId);
}
