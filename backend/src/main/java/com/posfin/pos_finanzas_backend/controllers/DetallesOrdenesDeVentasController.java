package com.posfin.pos_finanzas_backend.controllers;

import com.posfin.pos_finanzas_backend.models.DetallesOrdenesDeVentas;
import com.posfin.pos_finanzas_backend.models.OrdenesDeVentas;
import com.posfin.pos_finanzas_backend.models.Productos;
import com.posfin.pos_finanzas_backend.models.HistorialPrecios;
import com.posfin.pos_finanzas_backend.dtos.DetallesOrdenesDeVentasDTO;
import com.posfin.pos_finanzas_backend.repositories.DetallesOrdenesDeVentasRepository;
import com.posfin.pos_finanzas_backend.repositories.OrdenesDeVentasRepository;
import com.posfin.pos_finanzas_backend.repositories.ProductosRepository;
import com.posfin.pos_finanzas_backend.repositories.HistorialPreciosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Map;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/detalles-ordenes-de-ventas")
public class DetallesOrdenesDeVentasController {

    @Autowired
    private DetallesOrdenesDeVentasRepository detallesOrdenesDeVentasRepository;

    @Autowired
    private OrdenesDeVentasRepository ordenesDeVentasRepository;

    @Autowired
    private ProductosRepository productosRepository;

    @Autowired
    private HistorialPreciosRepository historialPreciosRepository;

    @GetMapping
    public List<DetallesOrdenesDeVentasDTO> getAllDetallesOrdenesDeVentas() {
        List<DetallesOrdenesDeVentas> detalles = detallesOrdenesDeVentasRepository.findAll();
        List<DetallesOrdenesDeVentasDTO> detallesDTO = new ArrayList<>();

        for (DetallesOrdenesDeVentas detalle : detalles) {
            DetallesOrdenesDeVentasDTO dto = new DetallesOrdenesDeVentasDTO();
            dto.setId(detalle.getId());
            dto.setCantidadPz(detalle.getCantidadPz());
            dto.setCantidadKg(detalle.getCantidadKg());

            // Mapear relaciones aplanadas
            dto.setOrdenDeVentaId(detalle.getOrdenDeVenta().getId());
            dto.setProductoId(detalle.getProducto().getId());
            dto.setProductoNombre(detalle.getProducto().getNombre());
            dto.setHistorialPrecioId(detalle.getHistorialPrecio().getId());
            dto.setPrecio(detalle.getHistorialPrecio().getPrecio());

            detallesDTO.add(dto);
        }

        return detallesDTO;
    }

    @GetMapping("/{id}")
    public ResponseEntity<DetallesOrdenesDeVentasDTO> getDetalleOrdenDeVentaById(@PathVariable String id) {
        Optional<DetallesOrdenesDeVentas> optionalDetalle = detallesOrdenesDeVentasRepository.findById(id);

        if (!optionalDetalle.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        DetallesOrdenesDeVentas detalle = optionalDetalle.get();
        DetallesOrdenesDeVentasDTO dto = new DetallesOrdenesDeVentasDTO();
        dto.setId(detalle.getId());
        dto.setCantidadPz(detalle.getCantidadPz());
        dto.setCantidadKg(detalle.getCantidadKg());

        // Mapear relaciones aplanadas
        dto.setOrdenDeVentaId(detalle.getOrdenDeVenta().getId());
        dto.setProductoId(detalle.getProducto().getId());
        dto.setProductoNombre(detalle.getProducto().getNombre());
        dto.setHistorialPrecioId(detalle.getHistorialPrecio().getId());
        dto.setPrecio(detalle.getHistorialPrecio().getPrecio());

        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<DetallesOrdenesDeVentasDTO> createDetalleOrdenDeVenta(
            @RequestBody Map<String, Object> requestBody) {
        try {
            // Obtener datos del request
            String ordenDeVentaId = (String) requestBody.get("ordenDeVentaId");
            String productoId = (String) requestBody.get("productoId");
            String historialPrecioId = (String) requestBody.get("historialPrecioId");
            BigDecimal cantidadPz = new BigDecimal(requestBody.get("cantidadPz").toString());
            BigDecimal cantidadKg = new BigDecimal(requestBody.get("cantidadKg").toString());

            // Buscar las entidades relacionadas
            Optional<OrdenesDeVentas> ordenDeVenta = ordenesDeVentasRepository.findById(ordenDeVentaId);
            Optional<Productos> producto = productosRepository.findById(productoId);
            Optional<HistorialPrecios> historialPrecio = historialPreciosRepository.findById(historialPrecioId);

            if (!ordenDeVenta.isPresent() || !producto.isPresent() || !historialPrecio.isPresent()) {
                return ResponseEntity.badRequest().build();
            }

            // Crear el detalle
            DetallesOrdenesDeVentas detalle = new DetallesOrdenesDeVentas();
            detalle.setOrdenDeVenta(ordenDeVenta.get());
            detalle.setProducto(producto.get());
            detalle.setHistorialPrecio(historialPrecio.get());
            detalle.setCantidadPz(cantidadPz);
            detalle.setCantidadKg(cantidadKg);

            // Guardar
            DetallesOrdenesDeVentas savedDetalle = detallesOrdenesDeVentasRepository.save(detalle);

            // Crear DTO para respuesta
            DetallesOrdenesDeVentasDTO dto = new DetallesOrdenesDeVentasDTO();
            dto.setId(savedDetalle.getId());
            dto.setCantidadPz(savedDetalle.getCantidadPz());
            dto.setCantidadKg(savedDetalle.getCantidadKg());
            dto.setOrdenDeVentaId(savedDetalle.getOrdenDeVenta().getId());
            dto.setProductoId(savedDetalle.getProducto().getId());
            dto.setProductoNombre(savedDetalle.getProducto().getNombre());
            dto.setHistorialPrecioId(savedDetalle.getHistorialPrecio().getId());
            dto.setPrecio(savedDetalle.getHistorialPrecio().getPrecio());

            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<DetallesOrdenesDeVentasDTO> updateDetalleOrdenDeVenta(@PathVariable String id,
            @RequestBody Map<String, Object> requestBody) {
        try {
            Optional<DetallesOrdenesDeVentas> optionalDetalle = detallesOrdenesDeVentasRepository.findById(id);
            if (!optionalDetalle.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            DetallesOrdenesDeVentas detalle = optionalDetalle.get();

            // Obtener datos del request
            String ordenDeVentaId = (String) requestBody.get("ordenDeVentaId");
            String productoId = (String) requestBody.get("productoId");
            String historialPrecioId = (String) requestBody.get("historialPrecioId");
            BigDecimal cantidadPz = new BigDecimal(requestBody.get("cantidadPz").toString());
            BigDecimal cantidadKg = new BigDecimal(requestBody.get("cantidadKg").toString());

            // Buscar las entidades relacionadas
            Optional<OrdenesDeVentas> ordenDeVenta = ordenesDeVentasRepository.findById(ordenDeVentaId);
            Optional<Productos> producto = productosRepository.findById(productoId);
            Optional<HistorialPrecios> historialPrecio = historialPreciosRepository.findById(historialPrecioId);

            if (!ordenDeVenta.isPresent() || !producto.isPresent() || !historialPrecio.isPresent()) {
                return ResponseEntity.badRequest().build();
            }

            // Actualizar el detalle
            detalle.setOrdenDeVenta(ordenDeVenta.get());
            detalle.setProducto(producto.get());
            detalle.setHistorialPrecio(historialPrecio.get());
            detalle.setCantidadPz(cantidadPz);
            detalle.setCantidadKg(cantidadKg);

            // Guardar
            DetallesOrdenesDeVentas savedDetalle = detallesOrdenesDeVentasRepository.save(detalle);

            // Crear DTO para respuesta
            DetallesOrdenesDeVentasDTO dto = new DetallesOrdenesDeVentasDTO();
            dto.setId(savedDetalle.getId());
            dto.setCantidadPz(savedDetalle.getCantidadPz());
            dto.setCantidadKg(savedDetalle.getCantidadKg());
            dto.setOrdenDeVentaId(savedDetalle.getOrdenDeVenta().getId());
            dto.setProductoId(savedDetalle.getProducto().getId());
            dto.setProductoNombre(savedDetalle.getProducto().getNombre());
            dto.setHistorialPrecioId(savedDetalle.getHistorialPrecio().getId());
            dto.setPrecio(savedDetalle.getHistorialPrecio().getPrecio());

            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDetalleOrdenDeVenta(@PathVariable String id) {
        if (!detallesOrdenesDeVentasRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        detallesOrdenesDeVentasRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
