package com.posfin.pos_finanzas_backend.repositories;

import com.posfin.pos_finanzas_backend.models.OrdenesDeCompras;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface OrdenesDeComprasRepository extends JpaRepository<OrdenesDeCompras, String> {
    
    /**
     * Encuentra todas las órdenes de compra de un proveedor específico
     */
    List<OrdenesDeCompras> findByPersonaId(String proveedorId);
    
    /**
     * Encuentra órdenes de compra por proveedor y estado
     */
    @Query("SELECT o FROM OrdenesDeCompras o WHERE o.persona.id = :proveedorId AND o.estado.estado = :estadoNombre")
    List<OrdenesDeCompras> findByProveedorIdAndEstadoNombre(@Param("proveedorId") String proveedorId, 
                                                            @Param("estadoNombre") String estadoNombre);
    
    /**
     * Calcula el total de compras de un proveedor (suma de todos los totales de órdenes)
     */
    @Query("SELECT COALESCE(SUM(o.totalCompra), 0) FROM OrdenesDeCompras o WHERE o.persona.id = :proveedorId")
    BigDecimal calcularTotalComprasProveedor(@Param("proveedorId") String proveedorId);
    
    /**
     * Encuentra órdenes de compra por estado
     */
    @Query("SELECT o FROM OrdenesDeCompras o WHERE o.estado.estado = :estadoNombre ORDER BY o.fechaOrden DESC")
    List<OrdenesDeCompras> findByEstadoNombreOrderByFechaOrdenDesc(@Param("estadoNombre") String estadoNombre);
    
    /**
     * Obtiene la orden de compra más antigua de un proveedor específico
     */
    @Query("SELECT o FROM OrdenesDeCompras o WHERE o.persona.id = :proveedorId ORDER BY o.fechaOrden ASC")
    List<OrdenesDeCompras> findOrdersMasAntiguasByProveedorId(@Param("proveedorId") String proveedorId);
    
    /**
     * Cuenta las órdenes pendientes de un proveedor
     */
    @Query("SELECT COUNT(o) FROM OrdenesDeCompras o WHERE o.persona.id = :proveedorId AND o.estado.estado = 'Pendiente'")
    Long contarOrdenesPendientesByProveedorId(@Param("proveedorId") String proveedorId);
    
    /**
     * Obtiene información agregada de deudas por proveedor
     */
    @Query(value = """
        SELECT 
            p.id as proveedorId,
            p.nombre as proveedorNombre,
            p.telefono as telefonoProveedor,
            p.email as emailProveedor,
            COALESCE(compras.total_compras, 0) as totalCompras,
            COALESCE(pagos.total_pagos, 0) as totalPagos,
            compras.fecha_orden_mas_antigua as fechaOrdenMasAntigua,
            COALESCE(compras.cantidad_ordenes, 0) as cantidadOrdenes
        FROM personas p
        LEFT JOIN (
            SELECT 
                o.persona_id,
                SUM(o.total_compra) as total_compras,
                MIN(o.fecha_orden) as fecha_orden_mas_antigua,
                COUNT(o.id) as cantidad_ordenes
            FROM ordenes_de_compras o
            GROUP BY o.persona_id
        ) compras ON p.id = compras.persona_id
        LEFT JOIN (
            SELECT 
                hcp.personas_id,
                SUM(hcp.monto_pagado) as total_pagos
            FROM historial_cargos_proveedores hcp
            GROUP BY hcp.personas_id
        ) pagos ON p.id = pagos.personas_id
        WHERE p.categoria_personas_id = (SELECT id FROM categoria_personas WHERE categoria = 'Proveedor')
        AND (COALESCE(compras.total_compras, 0) > COALESCE(pagos.total_pagos, 0))
        ORDER BY (COALESCE(compras.total_compras, 0) - COALESCE(pagos.total_pagos, 0)) DESC
        """, nativeQuery = true)
    List<Object[]> obtenerDeudasProveedores();
}
