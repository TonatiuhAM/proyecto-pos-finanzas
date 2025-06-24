package com.posfin.pos_finanzas_backend.repositories;

import com.posfin.pos_finanzas_backend.models.Estados;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstadosRepository extends JpaRepository<Estados, String> {
    // Aquí puedes definir métodos personalizados para interactuar con la base de
    // datos relacionados con los estados, si es necesario.
    // Por ejemplo, podrías agregar métodos para buscar por nombre de estado,
    // o cualquier otra lógica específica que necesites implementar.

}
