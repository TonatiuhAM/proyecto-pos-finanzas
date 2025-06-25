package com.posfin.pos_finanzas_backend.repositories;

import com.posfin.pos_finanzas_backend.models.OrdenesDeVentas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdenesDeVentasRepository extends JpaRepository<OrdenesDeVentas, String> {
    // Aquí puedes definir métodos personalizados para interactuar con la base de
    // datos
    // relacionados con las órdenes de ventas, si es necesario.
    // Por ejemplo, podrías agregar métodos para buscar por fecha, cliente, usuario,
    // etc.
}
