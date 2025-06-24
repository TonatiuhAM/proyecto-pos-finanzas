package com.posfin.pos_finanzas_backend.repositories;

import com.posfin.pos_finanzas_backend.models.Roles;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolesRepository extends JpaRepository<Roles, String> {
    // Aquí puedes definir métodos personalizados para interactuar con la base de
    // datos relacionados con los roles, si es necesario.
    // Por ejemplo, podrías agregar métodos para buscar por nombre de rol,
    // o cualquier otra lógica específica que necesites implementar.

}
