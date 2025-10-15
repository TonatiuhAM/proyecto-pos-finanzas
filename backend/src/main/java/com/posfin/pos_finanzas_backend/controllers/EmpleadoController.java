package com.posfin.pos_finanzas_backend.controllers;

import com.posfin.pos_finanzas_backend.dtos.EmpleadoCreateRequestDTO;
import com.posfin.pos_finanzas_backend.dtos.EmpleadoEstadoRequestDTO;
import com.posfin.pos_finanzas_backend.dtos.EmpleadoResponseDTO;
import com.posfin.pos_finanzas_backend.services.EmpleadoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empleados")
@CrossOrigin(origins = "*")
public class EmpleadoController {

    @Autowired
    private EmpleadoService empleadoService;

    /**
     * 🔍 Obtener lista de todos los empleados para mostrar en la tabla del frontend
     * Endpoint: GET /api/empleados
     * Response: Lista de EmpleadoResponseDTO con información completa de cada
     * empleado
     */
    @GetMapping
    public ResponseEntity<List<EmpleadoResponseDTO>> obtenerTodosLosEmpleados() {
        try {
            List<EmpleadoResponseDTO> empleados = empleadoService.obtenerTodosLosEmpleados();
            return ResponseEntity.ok(empleados);
        } catch (Exception e) {
            // Log del error para debugging
            System.err.println("Error al obtener empleados: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * ➕ Crear nuevo empleado desde el modal del frontend
     * Endpoint: POST /api/empleados
     * Request Body: EmpleadoCreateRequestDTO con datos del formulario
     * Response: EmpleadoResponseDTO del empleado creado
     */
    @PostMapping
    public ResponseEntity<?> crearEmpleado(@RequestBody EmpleadoCreateRequestDTO request) {
        try {
            // Validaciones básicas
            if (request.getNombre() == null || request.getNombre().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El nombre es requerido");
            }
            if (request.getContrasena() == null || request.getContrasena().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("La contraseña es requerida");
            }
            if (request.getRolId() == null) {
                return ResponseEntity.badRequest().body("El rol es requerido");
            }

            EmpleadoResponseDTO empleado = empleadoService.crearEmpleado(request);
            return ResponseEntity.ok(empleado);
        } catch (IllegalArgumentException e) {
            // Errores de validación de negocio (ej: rol no existe, nombre duplicado)
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // Log del error para debugging
            System.err.println("Error al crear empleado: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error interno del servidor");
        }
    }

    /**
     * 🔄 Cambiar estado de empleado (Activo/Inactivo) desde el toggle del frontend
     * Endpoint: PUT /api/empleados/{id}/estado
     * Path Variable: id del usuario/empleado
     * Request Body: EmpleadoEstadoRequestDTO con el nuevo estado
     * Response: EmpleadoResponseDTO actualizado
     */
    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstadoEmpleado(@PathVariable String id,
            @RequestBody EmpleadoEstadoRequestDTO request) {
        try {
            System.out.println("🔧 [CONTROLLER-DEBUG] Recibida petición cambio de estado - ID: " + id);
            System.out.println("🔧 [CONTROLLER-DEBUG] Estado solicitado: " + (request != null ? request.getEstado() : "null"));
            
            // Validaciones básicas
            if (request.getEstado() == null || request.getEstado().trim().isEmpty()) {
                System.err.println("❌ [CONTROLLER-ERROR] Estado vacío o nulo");
                return ResponseEntity.badRequest().body("El estado es requerido");
            }
            if (!"Activo".equals(request.getEstado()) && !"Inactivo".equals(request.getEstado())) {
                System.err.println("❌ [CONTROLLER-ERROR] Estado inválido: " + request.getEstado());
                return ResponseEntity.badRequest().body("Estado inválido. Debe ser 'Activo' o 'Inactivo'");
            }

            EmpleadoResponseDTO empleado = empleadoService.cambiarEstadoEmpleado(id, request.getEstado());
            System.out.println("✅ [CONTROLLER-DEBUG] Cambio de estado exitoso");
            return ResponseEntity.ok(empleado);
        } catch (IllegalArgumentException e) {
            // Errores de validación (ej: empleado no encontrado, estado no existe)
            System.err.println("❌ [CONTROLLER-ERROR] Error de validación: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // Log del error para debugging
            System.err.println("❌ [CONTROLLER-ERROR] Error interno al cambiar estado del empleado: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error interno del servidor");
        }
    }

    /**
     * 🔍 Obtener empleado por ID (para casos específicos del frontend)
     * Endpoint: GET /api/empleados/{id}
     * Path Variable: id del usuario/empleado
     * Response: EmpleadoResponseDTO del empleado solicitado
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerEmpleadoPorId(@PathVariable String id) {
        try {
            EmpleadoResponseDTO empleado = empleadoService.obtenerEmpleadoPorId(id);
            return ResponseEntity.ok(empleado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // Log del error para debugging
            System.err.println("Error al obtener empleado por ID: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error interno del servidor");
        }
    }
}
