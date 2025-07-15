package com.posfin.pos_finanzas_backend.repositories;

import com.posfin.pos_finanzas_backend.models.HistorialCostos;
import com.posfin.pos_finanzas_backend.models.Productos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HistorialCostosRepository extends JpaRepository<HistorialCostos, String> {

    @Query("SELECT hc FROM HistorialCostos hc WHERE hc.productos = :producto ORDER BY hc.fechaDeRegistro DESC LIMIT 1")
    Optional<HistorialCostos> findLatestByProducto(@Param("producto") Productos producto);
}
