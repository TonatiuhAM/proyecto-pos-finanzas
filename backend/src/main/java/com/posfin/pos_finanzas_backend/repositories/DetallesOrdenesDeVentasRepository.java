package com.posfin.pos_finanzas_backend.repositories;

import com.posfin.pos_finanzas_backend.models.DetallesOrdenesDeVentas;
import com.posfin.pos_finanzas_backend.models.OrdenesDeVentas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DetallesOrdenesDeVentasRepository extends JpaRepository<DetallesOrdenesDeVentas, String> {
    // Métodos personalizados para interactuar con la base de datos
    
    /**
     * Busca todos los detalles de una orden de venta específica
     */
    List<DetallesOrdenesDeVentas> findByOrdenDeVenta(OrdenesDeVentas ordenDeVenta);
}
