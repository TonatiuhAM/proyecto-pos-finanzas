package com.posfin.pos_finanzas_backend.repositories;

import com.posfin.pos_finanzas_backend.models.TipoMovimientos;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TIpoMovimientosRepository extends JpaRepository<TipoMovimientos, String> {
    // Método para buscar tipo de movimiento por movimiento
    Optional<TipoMovimientos> findByMovimiento(String movimiento);
}
