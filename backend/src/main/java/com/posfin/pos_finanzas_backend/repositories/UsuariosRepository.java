package com.posfin.pos_finanzas_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.posfin.pos_finanzas_backend.models.Usuarios;

public interface UsuariosRepository extends JpaRepository<Usuarios, String> {
    // Aquí puedes definir métodos personalizados para interactuar con la base de
    // datos
    // relacionados con los usuarios, si es necesario.
    // Por ejemplo, podrías agregar métodos para buscar por nombre de usuario,
    // o cualquier otra lógica específica que necesites implementar.

}
