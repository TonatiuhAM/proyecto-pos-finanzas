package com.posfin.pos_finanzas_backend.repositories;

import com.posfin.pos_finanzas_backend.models.TipoMovimientos;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TIpoMovimientosRepository extends JpaRepository<TipoMovimientos, String> {
    // Aquí puedes definir métodos personalizados para interactuar con la base de
    // datos
    // relacionados con los tipos de movimientos, si es necesario.
    // Por ejemplo, podrías agregar métodos para buscar por nombre de movimiento,
    // o cualquier otra lógica específica que necesites implementar.
}
