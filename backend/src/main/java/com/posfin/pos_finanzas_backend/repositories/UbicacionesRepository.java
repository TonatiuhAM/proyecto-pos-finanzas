package com.posfin.pos_finanzas_backend.repositories;

import com.posfin.pos_finanzas_backend.models.Ubicaciones;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UbicacionesRepository extends JpaRepository<Ubicaciones, String> {
    // Aquí puedes definir métodos personalizados para interactuar con la base de
    // datos relacionados con las ubicaciones, si es necesario.
    // Por ejemplo, podrías agregar métodos para buscar por nombre de ubicación,
    // o cualquier otra lógica específica que necesites implementar.

}
