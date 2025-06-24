package com.posfin.pos_finanzas_backend.repositories;

import com.posfin.pos_finanzas_backend.models.Inventarios;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventarioRepository extends JpaRepository<Inventarios, String> {
    // Aquí puedes definir métodos personalizados para interactuar con la base de
    // datos
    // relacionados con los inventarios, si es necesario.
    // Por ejemplo, podrías agregar métodos para buscar por productoId o
    // ubicacionesId,
    // o cualquier otra lógica específica que necesites implementar.

}
