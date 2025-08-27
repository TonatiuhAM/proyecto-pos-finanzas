package com.posfin.pos_finanzas_backend.controllers;

import com.posfin.pos_finanzas_backend.dtos.RolResponseDTO;
import com.posfin.pos_finanzas_backend.services.RolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@CrossOrigin(origins = "*")
public class RolController {

    @Autowired
    private RolService rolService;

    /**
     * 🔍 Obtener lista de todos los roles para popular el dropdown en el frontend
     * Endpoint: GET /api/roles
     * Response: Lista de RolResponseDTO con id y nombre de cada rol
     * Usage: Modal de crear empleado - dropdown de selección de rol
     */
    @GetMapping
    public ResponseEntity<List<RolResponseDTO>> obtenerTodosLosRoles() {
        try {
            List<RolResponseDTO> roles = rolService.obtenerTodosLosRoles();
            return ResponseEntity.ok(roles);
        } catch (Exception e) {
            // Log del error para debugging
            System.err.println("Error al obtener roles: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 🔍 Obtener rol por ID (para casos específicos del frontend)
     * Endpoint: GET /api/roles/{id}
     * Path Variable: id del rol
     * Response: RolResponseDTO del rol solicitado
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerRolPorId(@PathVariable Long id) {
        try {
            RolResponseDTO rol = rolService.obtenerRolPorId(id);
            return ResponseEntity.ok(rol);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // Log del error para debugging
            System.err.println("Error al obtener rol por ID: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error interno del servidor");
        }
    }
}
