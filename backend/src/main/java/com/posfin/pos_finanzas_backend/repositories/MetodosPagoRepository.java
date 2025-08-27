package com.posfin.pos_finanzas_backend.repositories;

import com.posfin.pos_finanzas_backend.models.MetodosPago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MetodosPagoRepository extends JpaRepository<MetodosPago, String> {
    
    /**
     * Busca método de pago por método de pago
     */
    Optional<MetodosPago> findByMetodoPago(String metodoPago);
}
