package com.posfin.pos_finanzas_backend.repositories;

import com.posfin.pos_finanzas_backend.models.CategoriaPersonas;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaPersonasRepository extends JpaRepository<CategoriaPersonas, String> {
    // Aquí puedes definir métodos personalizados para interactuar con la base de
    // datos
    // relacionados con las categorías de personas, si es necesario.
    // Por ejemplo, podrías agregar métodos para buscar por nombre de categoría,
    // o cualquier otra lógica específica que necesites implementar.

}
