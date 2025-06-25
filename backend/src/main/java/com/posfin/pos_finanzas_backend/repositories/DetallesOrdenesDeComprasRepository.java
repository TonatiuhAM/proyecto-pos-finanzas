package com.posfin.pos_finanzas_backend.repositories;

import com.posfin.pos_finanzas_backend.models.DetallesOrdenesDeCompras;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DetallesOrdenesDeComprasRepository extends JpaRepository<DetallesOrdenesDeCompras, String> {
    // Aquí puedes definir métodos personalizados para interactuar con la base de
    // datos
    // relacionados con los detalles de órdenes de compras, si es necesario.
    // Por ejemplo, podrías agregar métodos para buscar por orden de compra,
    // producto, etc.
}
