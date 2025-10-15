package com.posfin.pos_finanzas_backend.controllers;

import com.posfin.pos_finanzas_backend.dtos.PersonaCreateRequestDTO;
import com.posfin.pos_finanzas_backend.dtos.PersonaResponseDTO;
import com.posfin.pos_finanzas_backend.services.PersonaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/personas")
@CrossOrigin(origins = "http://localhost:5173")
public class PersonaController {

    @Autowired
    private PersonaService personaService;

    /**
     * Crear una nueva persona
     */
    @PostMapping
    public ResponseEntity<PersonaResponseDTO> crearPersona(@Valid @RequestBody PersonaCreateRequestDTO request) {
        try {
            PersonaResponseDTO persona = personaService.crearPersona(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(persona);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener todas las personas por categoría
     */
    @GetMapping("/categoria/{idCategoria}")
    public ResponseEntity<List<PersonaResponseDTO>> obtenerPersonasPorCategoria(@PathVariable String idCategoria) {
        try {
            List<PersonaResponseDTO> personas = personaService.obtenerPersonasPorCategoria(idCategoria);
            return ResponseEntity.ok(personas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener todas las personas activas por categoría
     */
    @GetMapping("/categoria/{idCategoria}/activos")
    public ResponseEntity<List<PersonaResponseDTO>> obtenerPersonasActivasPorCategoria(@PathVariable String idCategoria) {
        try {
            List<PersonaResponseDTO> personas = personaService.obtenerPersonasActivasPorCategoria(idCategoria);
            return ResponseEntity.ok(personas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener todas las personas (para uso en formularios)
     */
    @GetMapping
    public ResponseEntity<List<PersonaResponseDTO>> obtenerTodasLasPersonas() {
        try {
            List<PersonaResponseDTO> personas = personaService.obtenerTodasLasPersonas();
            return ResponseEntity.ok(personas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener una persona por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<PersonaResponseDTO> obtenerPersonaPorId(@PathVariable String id) {
        try {
            PersonaResponseDTO persona = personaService.obtenerPersonaPorId(id);
            return ResponseEntity.ok(persona);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Actualizar el estado de una persona
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<PersonaResponseDTO> actualizarEstadoPersona(
            @PathVariable String id, 
            @RequestBody Map<String, String> request) {
        try {
            System.out.println("🔧 [PERSONA-CONTROLLER-DEBUG] Recibida petición cambio de estado - ID: " + id);
            System.out.println("🔧 [PERSONA-CONTROLLER-DEBUG] Request body: " + request);
            
            String nuevoEstadoNombre = request.get("estadoNombre");
            if (nuevoEstadoNombre == null) {
                System.err.println("❌ [PERSONA-CONTROLLER-ERROR] estadoNombre no encontrado en request");
                return ResponseEntity.badRequest().build();
            }
            
            System.out.println("🔧 [PERSONA-CONTROLLER-DEBUG] Estado solicitado: " + nuevoEstadoNombre);
            
            PersonaResponseDTO persona = personaService.actualizarEstadoPersona(id, nuevoEstadoNombre);
            System.out.println("✅ [PERSONA-CONTROLLER-DEBUG] Cambio de estado exitoso");
            return ResponseEntity.ok(persona);
        } catch (IllegalArgumentException e) {
            System.err.println("❌ [PERSONA-CONTROLLER-ERROR] Error de validación: " + e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("❌ [PERSONA-CONTROLLER-ERROR] Error interno: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Eliminar una persona (soft delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPersona(@PathVariable String id) {
        try {
            personaService.eliminarPersona(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}