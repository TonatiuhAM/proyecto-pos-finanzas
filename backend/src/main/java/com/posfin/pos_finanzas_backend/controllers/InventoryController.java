package com.posfin.pos_finanzas_backend.controllers;

import com.posfin.pos_finanzas_backend.dtos.InventarioDTO;
import com.posfin.pos_finanzas_backend.models.Inventarios;
import com.posfin.pos_finanzas_backend.models.Productos;
import com.posfin.pos_finanzas_backend.models.Ubicaciones;
import com.posfin.pos_finanzas_backend.repositories.InventarioRepository;
import com.posfin.pos_finanzas_backend.repositories.ProductosRepository;
import com.posfin.pos_finanzas_backend.repositories.UbicacionesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inventarios")
public class InventoryController {

    @Autowired
    private InventarioRepository inventarioRepository;

    // --- DEPENDENCIAS ADICIONALES NECESARIAS ---
    @Autowired
    private ProductosRepository productosRepository;

    @Autowired
    private UbicacionesRepository ubicacionesRepository; // Asegúrate de haber creado este repositorio

    @GetMapping
    public List<InventarioDTO> getAllInventarios() {
        // 1. Obtén las entidades de la base de datos
        List<Inventarios> inventarios = inventarioRepository.findAll();

        // 2. Conviértelas a DTOs
        return inventarios.stream().map(inventario -> {
            InventarioDTO dto = new InventarioDTO();
            dto.setId(inventario.getId());
            dto.setCantidadPz(inventario.getCantidadPz());

            // Al hacer .getProducto(), Hibernate carga el objeto real
            dto.setProductoNombre(inventario.getProducto().getNombre());
            dto.setUbicacionNombre(inventario.getUbicacion().getNombre());

            return dto;
        }).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Inventarios> getInventarioById(@PathVariable String id) {
        return inventarioRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // --- MÉTODO POST CORREGIDO ---
    @PostMapping
    public ResponseEntity<?> createInventario(@RequestBody Inventarios request) {
        // Busca el producto y la ubicación por los IDs que vienen en el JSON
        Optional<Productos> productoOpt = productosRepository.findById(request.getProducto().getId());
        Optional<Ubicaciones> ubicacionOpt = ubicacionesRepository.findById(request.getUbicacion().getId());

        if (productoOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Producto no encontrado con ID: " + request.getProducto().getId());
        }
        if (ubicacionOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Ubicación no encontrada con ID: " + request.getUbicacion().getId());
        }

        // Si existen, crea el nuevo objeto Inventarios y asigna las entidades completas
        Inventarios nuevoInventario = new Inventarios();
        nuevoInventario.setProducto(productoOpt.get());
        nuevoInventario.setUbicacion(ubicacionOpt.get());
        nuevoInventario.setCantidadPz(request.getCantidadPz());
        nuevoInventario.setCantidadKg(request.getCantidadKg());
        nuevoInventario.setCantidadMinima(request.getCantidadMinima());
        nuevoInventario.setCantidadMaxima(request.getCantidadMaxima());

        Inventarios inventarioGuardado = inventarioRepository.save(nuevoInventario);
        return ResponseEntity.ok(inventarioGuardado);
    }

    // --- MÉTODO PUT CORREGIDO ---
    @PutMapping("/{id}")
    public ResponseEntity<?> updateInventario(@PathVariable String id, @RequestBody Inventarios inventarioDetails) {
        return inventarioRepository.findById(id)
                .map(inventarioExistente -> {
                    // Busca las entidades relacionadas
                    Optional<Productos> productoOpt = productosRepository
                            .findById(inventarioDetails.getProducto().getId());
                    Optional<Ubicaciones> ubicacionOpt = ubicacionesRepository
                            .findById(inventarioDetails.getUbicacion().getId());

                    if (productoOpt.isEmpty() || ubicacionOpt.isEmpty()) {
                        return ResponseEntity.badRequest().body("Producto o Ubicación no válida.");
                    }

                    // Actualiza los campos
                    inventarioExistente.setProducto(productoOpt.get());
                    inventarioExistente.setUbicacion(ubicacionOpt.get());
                    inventarioExistente.setCantidadPz(inventarioDetails.getCantidadPz());
                    inventarioExistente.setCantidadKg(inventarioDetails.getCantidadKg());
                    inventarioExistente.setCantidadMinima(inventarioDetails.getCantidadMinima());
                    inventarioExistente.setCantidadMaxima(inventarioDetails.getCantidadMaxima());

                    Inventarios updatedInventario = inventarioRepository.save(inventarioExistente);
                    return ResponseEntity.ok(updatedInventario);
                }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInventario(@PathVariable String id) {
        if (!inventarioRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        inventarioRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> partialUpdateInventario(@PathVariable String id,
            @RequestBody Map<String, Object> updates) {
        // 1. Buscar la entidad existente
        Optional<Inventarios> inventarioOptional = inventarioRepository.findById(id);
        if (inventarioOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Inventarios inventarioExistente = inventarioOptional.get();

        // 2. Actualizar campos simples
        if (updates.containsKey("cantidadPz")) {
            inventarioExistente.setCantidadPz((Integer) updates.get("cantidadPz"));
        }
        if (updates.containsKey("cantidadKg")) {
            Number kgValue = (Number) updates.get("cantidadKg");
            inventarioExistente.setCantidadKg(kgValue != null ? kgValue.intValue() : null);
        }
        if (updates.containsKey("cantidadMinima")) {
            inventarioExistente.setCantidadMinima((Integer) updates.get("cantidadMinima"));
        }
        if (updates.containsKey("cantidadMaxima")) {
            inventarioExistente.setCantidadMaxima((Integer) updates.get("cantidadMaxima"));
        }

        // 3. Lógica para actualizar la relación con Productos
        if (updates.containsKey("producto")) {
            Map<String, String> productoMap = (Map<String, String>) updates.get("producto");
            String productoId = productoMap.get("id");
            Optional<Productos> productoOpt = productosRepository.findById(productoId);
            if (productoOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Error: El Producto con ID " + productoId + " no existe.");
            }
            inventarioExistente.setProducto(productoOpt.get());
        }

        // 4. Lógica para actualizar la relación con Ubicaciones
        if (updates.containsKey("ubicacion")) {
            Map<String, String> ubicacionMap = (Map<String, String>) updates.get("ubicacion");
            String ubicacionId = ubicacionMap.get("id");
            Optional<Ubicaciones> ubicacionOpt = ubicacionesRepository.findById(ubicacionId);
            if (ubicacionOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Error: La Ubicación con ID " + ubicacionId + " no existe.");
            }
            inventarioExistente.setUbicacion(ubicacionOpt.get());
        }

        // 5. Guardar y devolver
        Inventarios inventarioActualizado = inventarioRepository.save(inventarioExistente);
        return ResponseEntity.ok(inventarioActualizado);
    }
}