package com.posfin.pos_finanzas_backend.repositories;

import com.posfin.pos_finanzas_backend.models.OrdenesWorkspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdenesWorkspaceRepository extends JpaRepository<OrdenesWorkspace, String> {
    // Método para contar órdenes por workspace ID
    long countByWorkspaceId(String workspaceId);

    // Método para encontrar órdenes por workspace ID
    java.util.List<OrdenesWorkspace> findByWorkspaceId(String workspaceId);
}
