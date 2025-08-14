package com.posfin.pos_finanzas_backend.repositories;

import com.posfin.pos_finanzas_backend.models.MovimientosInventarios;
import com.posfin.pos_finanzas_backend.models.Productos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimientosInventariosRepository extends JpaRepository<MovimientosInventarios, String> {
    // Buscar movimientos por producto ordenados por fecha descendente (más recientes primero)
    List<MovimientosInventarios> findByProductoOrderByFechaMovimientoDesc(Productos producto);
    
    // Buscar movimientos por producto ordenados por fecha ascendente
    List<MovimientosInventarios> findByProductoOrderByFechaMovimientoAsc(Productos producto);
}
