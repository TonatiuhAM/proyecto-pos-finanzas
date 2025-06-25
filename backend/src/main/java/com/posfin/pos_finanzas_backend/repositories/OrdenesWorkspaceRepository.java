package com.posfin.pos_finanzas_backend.repositories;

import com.posfin.pos_finanzas_backend.models.OrdenesWorkspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdenesWorkspaceRepository extends JpaRepository<OrdenesWorkspace, String> {
    // Aquí puedes definir métodos personalizados para interactuar con la base de
    // datos
    // relacionados con las órdenes de workspace, si es necesario.
    // Por ejemplo, podrías agregar métodos para buscar por workspace, producto,
    // etc.
}
