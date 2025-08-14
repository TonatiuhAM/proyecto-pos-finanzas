package com.posfin.pos_finanzas_backend.repositories;

import com.posfin.pos_finanzas_backend.models.Personas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PersonasRepository extends JpaRepository<Personas, String> {
    
    /**
     * Encuentra personas por categoría (para obtener proveedores)
     */
    @Query("SELECT p FROM Personas p WHERE p.categoriaPersonas.categoria = :categoriaNombre AND p.estados.estado = 'Activo'")
    List<Personas> findByCategoriaPersonasNombreAndEstadoActivo(@Param("categoriaNombre") String categoriaNombre);
    
    /**
     * Encuentra proveedores activos específicamente
     */
    @Query("SELECT p FROM Personas p WHERE p.categoriaPersonas.categoria = 'Proveedor' AND p.estados.estado = 'Activo' ORDER BY p.nombre")
    List<Personas> findProveedoresActivos();
}
