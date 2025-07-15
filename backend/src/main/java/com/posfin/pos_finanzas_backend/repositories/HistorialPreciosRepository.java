package com.posfin.pos_finanzas_backend.repositories;

import com.posfin.pos_finanzas_backend.models.HistorialPrecios;
import com.posfin.pos_finanzas_backend.models.Productos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HistorialPreciosRepository extends JpaRepository<HistorialPrecios, String> {

    @Query("SELECT hp FROM HistorialPrecios hp WHERE hp.productos = :producto ORDER BY hp.fechaDeRegistro DESC LIMIT 1")
    Optional<HistorialPrecios> findLatestByProducto(@Param("producto") Productos producto);
}
