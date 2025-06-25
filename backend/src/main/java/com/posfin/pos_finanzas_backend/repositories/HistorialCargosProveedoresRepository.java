package com.posfin.pos_finanzas_backend.repositories;

import com.posfin.pos_finanzas_backend.models.HistorialCargosProveedores;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistorialCargosProveedoresRepository extends JpaRepository<HistorialCargosProveedores, String> {
    // Aquí puedes definir métodos personalizados para interactuar con la base de
    // datos
    // relacionados con el historial de cargos a proveedores, si es necesario.
    // Por ejemplo, podrías agregar métodos para buscar por proveedor, orden de
    // compra, fecha, etc.
}
