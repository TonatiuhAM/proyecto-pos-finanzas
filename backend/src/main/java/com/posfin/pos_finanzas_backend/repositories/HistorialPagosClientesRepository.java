package com.posfin.pos_finanzas_backend.repositories;

import com.posfin.pos_finanzas_backend.models.HistorialPagosClientes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistorialPagosClientesRepository extends JpaRepository<HistorialPagosClientes, String> {
    // Aquí puedes definir métodos personalizados para interactuar con la base de
    // datos
    // relacionados con el historial de pagos de clientes, si es necesario.
    // Por ejemplo, podrías agregar métodos para buscar por cliente, orden de venta,
    // fecha, etc.
}
