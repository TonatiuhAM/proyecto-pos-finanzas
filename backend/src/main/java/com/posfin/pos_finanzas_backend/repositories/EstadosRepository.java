package com.posfin.pos_finanzas_backend.repositories;

import com.posfin.pos_finanzas_backend.models.Estados;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EstadosRepository extends JpaRepository<Estados, String> {
    // Método para buscar estado por estado (encuentra el primero en caso de duplicados)
    Optional<Estados> findFirstByEstado(String estado);
    
    // Método original mantenido para compatibilidad (temporal hasta limpiar duplicados)
    Optional<Estados> findByEstado(String estado);
}
