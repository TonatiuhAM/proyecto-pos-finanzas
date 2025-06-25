package com.posfin.pos_finanzas_backend.repositories;

import com.posfin.pos_finanzas_backend.models.MovimientosInventarios;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovimientosInventariosRepository extends JpaRepository<MovimientosInventarios, String> {
    // Aquí puedes definir métodos personalizados para interactuar con la base de
    // datos
    // relacionados con los movimientos de inventarios, si es necesario.
    // Por ejemplo, podrías agregar métodos para buscar por producto, ubicación,
    // tipo de movimiento, usuario, fecha, etc.
}
