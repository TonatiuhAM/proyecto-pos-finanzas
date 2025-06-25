package com.posfin.pos_finanzas_backend.repositories;

import com.posfin.pos_finanzas_backend.models.HistorialPrecios;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistorialPreciosRepository extends JpaRepository<HistorialPrecios, String> {
}
