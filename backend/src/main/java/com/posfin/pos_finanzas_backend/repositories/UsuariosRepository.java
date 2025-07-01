package com.posfin.pos_finanzas_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.posfin.pos_finanzas_backend.models.Usuarios;
import java.util.Optional;

public interface UsuariosRepository extends JpaRepository<Usuarios, String> {
    // Método para buscar usuario por nombre
    Optional<Usuarios> findByNombre(String nombre);
}
