package com.posfin.pos_finanzas_backend.controllers;

import com.posfin.pos_finanzas_backend.models.HistorialPrecios;
import com.posfin.pos_finanzas_backend.models.Productos;
import com.posfin.pos_finanzas_backend.dtos.HistorialPreciosDTO;
import com.posfin.pos_finanzas_backend.repositories.HistorialPreciosRepository;
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
@RequestMapping("/api/historial-precios")
public class HistorialPreciosController {

    @Autowired
    private HistorialPreciosRepository historialPreciosRepository;

    @Autowired
    private ProductosRepository productosRepository;

    @GetMapping
    public List<HistorialPreciosDTO> getAllHistorialPrecios() {
        List<HistorialPrecios> historialPrecios = historialPreciosRepository.findAll();
        List<HistorialPreciosDTO> historialPreciosDTO = new ArrayList<>();

        for (HistorialPrecios historial : historialPrecios) {
            HistorialPreciosDTO dto = new HistorialPreciosDTO();
            dto.setId(historial.getId());
            dto.setPrecio(historial.getPrecio());
            dto.setFechaDeRegistro(historial.getFechaDeRegistro());

            // Mapear relaciones aplanadas
            dto.setProductosId(historial.getProductos().getId());
            dto.setProductosNombre(historial.getProductos().getNombre());

            historialPreciosDTO.add(dto);
        }

        return historialPreciosDTO;
    }

    @GetMapping("/{id}")
    public ResponseEntity<HistorialPreciosDTO> getHistorialPrecioById(@PathVariable String id) {
        Optional<HistorialPrecios> optionalHistorial = historialPreciosRepository.findById(id);

        if (!optionalHistorial.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        HistorialPrecios historial = optionalHistorial.get();
        HistorialPreciosDTO dto = new HistorialPreciosDTO();
        dto.setId(historial.getId());
        dto.setPrecio(historial.getPrecio());
        dto.setFechaDeRegistro(historial.getFechaDeRegistro());

        // Mapear relaciones aplanadas
        dto.setProductosId(historial.getProductos().getId());
        dto.setProductosNombre(historial.getProductos().getNombre());

        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<HistorialPreciosDTO> createHistorialPrecio(@RequestBody Map<String, Object> requestBody) {
        try {
            // Obtener datos del request
            String productosId = (String) requestBody.get("productosId");
            BigDecimal precio = new BigDecimal(requestBody.get("precio").toString());

            // Buscar el producto relacionado
            Optional<Productos> producto = productosRepository.findById(productosId);

            if (!producto.isPresent()) {
                return ResponseEntity.badRequest().build();
            }

            // Crear el historial de precio
            HistorialPrecios historial = new HistorialPrecios();
            historial.setProductos(producto.get());
            historial.setPrecio(precio);
            historial.setFechaDeRegistro(ZonedDateTime.now());

            // Guardar
            HistorialPrecios savedHistorial = historialPreciosRepository.save(historial);

            // Crear DTO para respuesta
            HistorialPreciosDTO dto = new HistorialPreciosDTO();
            dto.setId(savedHistorial.getId());
            dto.setPrecio(savedHistorial.getPrecio());
            dto.setFechaDeRegistro(savedHistorial.getFechaDeRegistro());
            dto.setProductosId(savedHistorial.getProductos().getId());
            dto.setProductosNombre(savedHistorial.getProductos().getNombre());

            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<HistorialPreciosDTO> updateHistorialPrecio(@PathVariable String id,
            @RequestBody Map<String, Object> requestBody) {
        try {
            Optional<HistorialPrecios> optionalHistorial = historialPreciosRepository.findById(id);
            if (!optionalHistorial.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            HistorialPrecios historial = optionalHistorial.get();

            // Obtener datos del request
            String productosId = (String) requestBody.get("productosId");
            BigDecimal precio = new BigDecimal(requestBody.get("precio").toString());

            // Buscar el producto relacionado
            Optional<Productos> producto = productosRepository.findById(productosId);

            if (!producto.isPresent()) {
                return ResponseEntity.badRequest().build();
            }

            // Actualizar el historial de precio
            historial.setProductos(producto.get());
            historial.setPrecio(precio);
            // Nota: No actualizamos la fecha de registro ya que debe ser inmutable

            // Guardar
            HistorialPrecios savedHistorial = historialPreciosRepository.save(historial);

            // Crear DTO para respuesta
            HistorialPreciosDTO dto = new HistorialPreciosDTO();
            dto.setId(savedHistorial.getId());
            dto.setPrecio(savedHistorial.getPrecio());
            dto.setFechaDeRegistro(savedHistorial.getFechaDeRegistro());
            dto.setProductosId(savedHistorial.getProductos().getId());
            dto.setProductosNombre(savedHistorial.getProductos().getNombre());

            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHistorialPrecio(@PathVariable String id) {
        if (!historialPreciosRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        historialPreciosRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> partialUpdateHistorialPrecio(@PathVariable String id,
            @RequestBody Map<String, Object> updates) {
        // 1. Buscar la entidad existente
        Optional<HistorialPrecios> historialOptional = historialPreciosRepository.findById(id);
        if (historialOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        HistorialPrecios historialExistente = historialOptional.get();

        // 2. Actualizar campos simples
        if (updates.containsKey("precio")) {
            Number precioValue = (Number) updates.get("precio");
            historialExistente.setPrecio(new BigDecimal(precioValue.toString()));
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
        HistorialPrecios historialActualizado = historialPreciosRepository.save(historialExistente);

        // Crear DTO para respuesta
        HistorialPreciosDTO dto = new HistorialPreciosDTO();
        dto.setId(historialActualizado.getId());
        dto.setPrecio(historialActualizado.getPrecio());
        dto.setFechaDeRegistro(historialActualizado.getFechaDeRegistro());
        dto.setProductosId(historialActualizado.getProductos().getId());
        dto.setProductosNombre(historialActualizado.getProductos().getNombre());

        return ResponseEntity.ok(dto);
    }
}
