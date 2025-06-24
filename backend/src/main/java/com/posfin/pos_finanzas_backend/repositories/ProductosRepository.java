package com.posfin.pos_finanzas_backend.repositories;

import com.posfin.pos_finanzas_backend.models.Productos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductosRepository extends JpaRepository<Productos, String> {
    // ¡Magia! No necesitas escribir nada más aquí.
    // Spring Data JPA implementará automáticamente los métodos como:
    // save(), findById(), findAll(), deleteById(), etc.
    // Aquí puedes agregar métodos personalizados si es necesario
}
