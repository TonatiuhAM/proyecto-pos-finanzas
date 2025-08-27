package com.posfin.pos_finanzas_backend.repositories;

import com.posfin.pos_finanzas_backend.models.Productos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductosRepository extends JpaRepository<Productos, String> {
    // ¡Magia! No necesitas escribir nada más aquí.
    // Spring Data JPA implementará automáticamente los métodos como:
    // save(), findById(), findAll(), deleteById(), etc.
    
    /**
     * Encuentra productos activos de un proveedor específico
     */
    @Query("SELECT p FROM Productos p WHERE p.proveedor.id = :proveedorId AND p.estados.estado = 'Activo' ORDER BY p.nombre")
    List<Productos> findByProveedorIdAndEstadoActivo(@Param("proveedorId") String proveedorId);
    
    /**
     * Encuentra productos activos (para filtro general)
     */
    @Query("SELECT p FROM Productos p WHERE p.estados.estado = 'Activo' ORDER BY p.nombre")
    List<Productos> findProductosActivos();
}
