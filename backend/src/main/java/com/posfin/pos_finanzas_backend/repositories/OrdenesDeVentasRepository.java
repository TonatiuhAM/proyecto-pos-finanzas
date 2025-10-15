package com.posfin.pos_finanzas_backend.repositories;

import com.posfin.pos_finanzas_backend.models.OrdenesDeVentas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface OrdenesDeVentasRepository extends JpaRepository<OrdenesDeVentas, String> {
    // Métodos personalizados para interactuar con la base de datos
    
    /**
     * Busca órdenes de ventas desde una fecha específica, ordenadas por fecha descendente
     */
    List<OrdenesDeVentas> findByFechaOrdenGreaterThanEqualOrderByFechaOrdenDesc(OffsetDateTime fechaDesde);
    
    /**
     * Obtiene todas las órdenes de ventas ordenadas por fecha descendente
     */
    List<OrdenesDeVentas> findAllByOrderByFechaOrdenDesc();
}
