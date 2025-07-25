
package com.posfin.pos_finanzas_backend.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Componente que se ejecuta al iniciar la aplicación para limpiar datos NULL
 * en la base de datos que podrían causar problemas con las validaciones.
 */
@Component
@Order(1)
public class DatabaseCleanupRunner implements InitializingBean {

    @Autowired
    private DataSource dataSource;

    @Override
    public void afterPropertiesSet() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            // Limpiar valores NULL en clave_movimiento
            cleanupClaveMovimiento(connection);

            // Limpiar valores NULL en cantidades de inventario
            cleanupInventarioCantidades(connection);

            System.out.println("✓ Limpieza de base de datos completada exitosamente");
        } catch (SQLException e) {
            System.err.println("⚠ Error durante la limpieza de base de datos: " + e.getMessage());
            // No lanzar excepción para no impedir el inicio de la aplicación
        }
    }

    private void cleanupClaveMovimiento(Connection connection) throws SQLException {
        String updateQuery = "UPDATE movimientos_inventarios SET clave_movimiento = CONCAT('MIGRATION-', id) WHERE clave_movimiento IS NULL";
        try (PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
            int updatedRows = stmt.executeUpdate();
            if (updatedRows > 0) {
                System.out.println("✓ Corregidos " + updatedRows + " registros NULL en clave_movimiento");
            }
        }
    }

    private void cleanupInventarioCantidades(Connection connection) throws SQLException {
        // Limpiar cantidad_pz NULL
        String updatePzQuery = "UPDATE inventarios SET cantidad_pz = 0 WHERE cantidad_pz IS NULL";
        try (PreparedStatement stmt = connection.prepareStatement(updatePzQuery)) {
            int updatedRows = stmt.executeUpdate();
            if (updatedRows > 0) {
                System.out.println("✓ Corregidos " + updatedRows + " registros NULL en cantidad_pz");
            }
        }

        // Limpiar cantidad_kg NULL
        String updateKgQuery = "UPDATE inventarios SET cantidad_kg = 0 WHERE cantidad_kg IS NULL";
        try (PreparedStatement stmt = connection.prepareStatement(updateKgQuery)) {
            int updatedRows = stmt.executeUpdate();
            if (updatedRows > 0) {
                System.out.println("✓ Corregidos " + updatedRows + " registros NULL en cantidad_kg");
            }
        }
    }
}
