package com.posfin.pos_finanzas_backend.repositories;

import com.posfin.pos_finanzas_backend.models.DetallesOrdenesDeVentas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DetallesOrdenesDeVentasRepository extends JpaRepository<DetallesOrdenesDeVentas, String> {
    // Aquí puedes definir métodos personalizados para interactuar con la base de
    // datos
    // relacionados con los detalles de órdenes de ventas, si es necesario.
    // Por ejemplo, podrías agregar métodos para buscar por orden de venta,
    // producto, etc.
}
