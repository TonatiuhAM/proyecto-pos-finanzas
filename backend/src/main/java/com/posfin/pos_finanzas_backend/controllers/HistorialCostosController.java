package com.posfin.pos_finanzas_backend.controllers;

import com.posfin.pos_finanzas_backend.models.HistorialCostos;
import com.posfin.pos_finanzas_backend.models.Productos;
import com.posfin.pos_finanzas_backend.dtos.HistorialCostosDTO;
import com.posfin.pos_finanzas_backend.repositories.HistorialCostosRepository;
import com.posfin.pos_finanzas_backend.repositories.ProductosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Map;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

@RestController
@RequestMapping("/api/historial-costos")
public class HistorialCostosController {

    @Autowired
    private HistorialCostosRepository historialCostosRepository;

    @Autowired
    private ProductosRepository productosRepository;

    @GetMapping
    public List<HistorialCostosDTO> getAllHistorialCostos() {
        List<HistorialCostos> historialCostos = historialCostosRepository.findAll();
        List<HistorialCostosDTO> historialCostosDTO = new ArrayList<>();

        for (HistorialCostos historial : historialCostos) {
            HistorialCostosDTO dto = new HistorialCostosDTO();
            dto.setId(historial.getId());
            dto.setCosto(historial.getCosto());
            dto.setFechaDeRegistro(historial.getFechaDeRegistro());

            // Mapear relaciones aplanadas
            dto.setProductosId(historial.getProductos().getId());
            dto.setProductosNombre(historial.getProductos().getNombre());

            historialCostosDTO.add(dto);
        }

        return historialCostosDTO;
    }

    @GetMapping("/{id}")
    public ResponseEntity<HistorialCostosDTO> getHistorialCostoById(@PathVariable String id) {
        Optional<HistorialCostos> optionalHistorial = historialCostosRepository.findById(id);

        if (!optionalHistorial.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        HistorialCostos historial = optionalHistorial.get();
        HistorialCostosDTO dto = new HistorialCostosDTO();
        dto.setId(historial.getId());
        dto.setCosto(historial.getCosto());
        dto.setFechaDeRegistro(historial.getFechaDeRegistro());

        // Mapear relaciones aplanadas
        dto.setProductosId(historial.getProductos().getId());
        dto.setProductosNombre(historial.getProductos().getNombre());

        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<HistorialCostosDTO> createHistorialCosto(@RequestBody Map<String, Object> requestBody) {
        try {
            // Obtener datos del request
            String productosId = (String) requestBody.get("productosId");
            BigDecimal costo = new BigDecimal(requestBody.get("costo").toString());

            // Buscar el producto relacionado
            Optional<Productos> producto = productosRepository.findById(productosId);

            if (!producto.isPresent()) {
                return ResponseEntity.badRequest().build();
            }

            // Crear el historial de costo
            HistorialCostos historial = new HistorialCostos();
            historial.setProductos(producto.get());
            historial.setCosto(costo);
            historial.setFechaDeRegistro(ZonedDateTime.now());

            // Guardar
            HistorialCostos savedHistorial = historialCostosRepository.save(historial);

            // Crear DTO para respuesta
            HistorialCostosDTO dto = new HistorialCostosDTO();
            dto.setId(savedHistorial.getId());
            dto.setCosto(savedHistorial.getCosto());
            dto.setFechaDeRegistro(savedHistorial.getFechaDeRegistro());
            dto.setProductosId(savedHistorial.getProductos().getId());
            dto.setProductosNombre(savedHistorial.getProductos().getNombre());

            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<HistorialCostosDTO> updateHistorialCosto(@PathVariable String id,
            @RequestBody Map<String, Object> requestBody) {
        try {
            Optional<HistorialCostos> optionalHistorial = historialCostosRepository.findById(id);
            if (!optionalHistorial.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            HistorialCostos historial = optionalHistorial.get();

            // Obtener datos del request
            String productosId = (String) requestBody.get("productosId");
            BigDecimal costo = new BigDecimal(requestBody.get("costo").toString());

            // Buscar el producto relacionado
            Optional<Productos> producto = productosRepository.findById(productosId);

            if (!producto.isPresent()) {
                return ResponseEntity.badRequest().build();
            }

            // Actualizar el historial de costo
            historial.setProductos(producto.get());
            historial.setCosto(costo);
            // Nota: No actualizamos la fecha de registro ya que debe ser inmutable

            // Guardar
            HistorialCostos savedHistorial = historialCostosRepository.save(historial);

            // Crear DTO para respuesta
            HistorialCostosDTO dto = new HistorialCostosDTO();
            dto.setId(savedHistorial.getId());
            dto.setCosto(savedHistorial.getCosto());
            dto.setFechaDeRegistro(savedHistorial.getFechaDeRegistro());
            dto.setProductosId(savedHistorial.getProductos().getId());
            dto.setProductosNombre(savedHistorial.getProductos().getNombre());

            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHistorialCosto(@PathVariable String id) {
        if (!historialCostosRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        historialCostosRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> partialUpdateHistorialCosto(@PathVariable String id,
            @RequestBody Map<String, Object> updates) {
        // 1. Buscar la entidad existente
        Optional<HistorialCostos> historialOptional = historialCostosRepository.findById(id);
        if (historialOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        HistorialCostos historialExistente = historialOptional.get();

        // 2. Actualizar campos simples
        if (updates.containsKey("costo")) {
            Number costoValue = (Number) updates.get("costo");
            historialExistente.setCosto(new BigDecimal(costoValue.toString()));
        }

        // 3. Lógica para actualizar la relación con Productos
        if (updates.containsKey("productos")) {
            Map<String, String> productosMap = (Map<String, String>) updates.get("productos");
            String productosId = productosMap.get("id");
            Optional<Productos> productosOpt = productosRepository.findById(productosId);
            if (productosOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Error: El Producto con ID " + productosId + " no existe.");
            }
            historialExistente.setProductos(productosOpt.get());
        }

        // 4. Guardar y devolver
        HistorialCostos historialActualizado = historialCostosRepository.save(historialExistente);

        // Crear DTO para respuesta
        HistorialCostosDTO dto = new HistorialCostosDTO();
        dto.setId(historialActualizado.getId());
        dto.setCosto(historialActualizado.getCosto());
        dto.setFechaDeRegistro(historialActualizado.getFechaDeRegistro());
        dto.setProductosId(historialActualizado.getProductos().getId());
        dto.setProductosNombre(historialActualizado.getProductos().getNombre());

        return ResponseEntity.ok(dto);
    }
}
