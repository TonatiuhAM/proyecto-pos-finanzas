package com.posfin.pos_finanzas_backend.repositories;

import com.posfin.pos_finanzas_backend.models.MetodosPago;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetodosPagoRepository extends JpaRepository<MetodosPago, String> {
    // Aquí puedes definir métodos personalizados para interactuar con la base de
    // datos
    // relacionados con los métodos de pago, si es necesario.
    // Por ejemplo, podrías agregar métodos para buscar por nombre de método de
    // pago,
    // o cualquier otra lógica específica que necesites implementar.

}
