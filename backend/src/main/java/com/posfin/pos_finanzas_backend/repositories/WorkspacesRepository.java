package com.posfin.pos_finanzas_backend.repositories;

import com.posfin.pos_finanzas_backend.models.Workspaces;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspacesRepository extends JpaRepository<Workspaces, String> {
    // Aquí puedes definir métodos personalizados para interactuar con la base de
    // datos relacionados con los espacios de trabajo, si es necesario.
    // Por ejemplo, podrías agregar métodos para buscar por nombre de espacio de
    // trabajo,
    // o cualquier otra lógica específica que necesites implementar.

}
