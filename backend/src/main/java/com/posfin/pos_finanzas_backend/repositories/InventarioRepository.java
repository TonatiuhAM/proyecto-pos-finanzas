package com.posfin.pos_finanzas_backend.repositories;

import com.posfin.pos_finanzas_backend.models.Inventarios;
import com.posfin.pos_finanzas_backend.models.Productos;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventarioRepository extends JpaRepository<Inventarios, String> {
    // Método para buscar inventario por producto
    Optional<Inventarios> findByProducto(Productos producto);
}
