package com.posfin.pos_finanzas_backend.repositories;

import com.posfin.pos_finanzas_backend.models.Personas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonasRepository extends JpaRepository<Personas, String> {
    
    /**
     * Encuentra personas por categoría ordenadas por nombre
     */
    List<Personas> findByCategoriaPersonas_IdOrderByNombreAsc(String idCategoriaPersona);
    
    /**
     * Encuentra personas por categoría y estado ordenadas por nombre
     */
    List<Personas> findByCategoriaPersonas_IdAndEstados_EstadoOrderByNombreAsc(String idCategoriaPersona, String estado);
    
    /**
     * Encuentra personas por RFC y estado
     */
    List<Personas> findByRfcAndEstados_Estado(String rfc, String estado);
    
    /**
     * Encuentra personas por email y estado
     */
    List<Personas> findByEmailAndEstados_Estado(String email, String estado);
    
    /**
     * Encuentra todas las personas por estado ordenadas por nombre
     */
    List<Personas> findByEstados_EstadoOrderByNombreAsc(String estado);
    
    /**
     * Encuentra personas por categoría (para mantener compatibilidad)
     */
    @Query("SELECT p FROM Personas p WHERE p.categoriaPersonas.categoria = :categoriaNombre AND p.estados.estado = 'Activo' ORDER BY p.nombre")
    List<Personas> findByCategoriaPersonasNombreAndEstadoActivo(@Param("categoriaNombre") String categoriaNombre);
    
    /**
     * Encuentra proveedores activos específicamente
     */
    @Query("SELECT p FROM Personas p WHERE p.categoriaPersonas.categoria = 'Proveedor' AND p.estados.estado = 'Activo' ORDER BY p.nombre")
    List<Personas> findProveedoresActivos();
}
