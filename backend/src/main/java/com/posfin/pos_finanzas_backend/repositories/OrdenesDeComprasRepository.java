package com.posfin.pos_finanzas_backend.repositories;

import com.posfin.pos_finanzas_backend.models.OrdenesDeCompras;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdenesDeComprasRepository extends JpaRepository<OrdenesDeCompras, String> {
    // Aquí puedes definir métodos personalizados para interactuar con la base de
    // datos
    // relacionados con las órdenes de compras, si es necesario.
    // Por ejemplo, podrías agregar métodos para buscar por proveedor, estado,
    // fecha, etc.
}
