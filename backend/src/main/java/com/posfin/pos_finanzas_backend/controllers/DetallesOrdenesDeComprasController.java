package com.posfin.pos_finanzas_backend.controllers;

import com.posfin.pos_finanzas_backend.models.DetallesOrdenesDeCompras;
import com.posfin.pos_finanzas_backend.models.OrdenesDeCompras;
import com.posfin.pos_finanzas_backend.models.Productos;
import com.posfin.pos_finanzas_backend.models.HistorialCostos;
import com.posfin.pos_finanzas_backend.dtos.DetallesOrdenesDeComprasDTO;
import com.posfin.pos_finanzas_backend.repositories.DetallesOrdenesDeComprasRepository;
import com.posfin.pos_finanzas_backend.repositories.OrdenesDeComprasRepository;
import com.posfin.pos_finanzas_backend.repositories.ProductosRepository;
import com.posfin.pos_finanzas_backend.repositories.HistorialCostosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Map;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/detalles-ordenes-de-compras")
public class DetallesOrdenesDeComprasController {

    @Autowired
    private DetallesOrdenesDeComprasRepository detallesOrdenesDeComprasRepository;

    @Autowired
    private OrdenesDeComprasRepository ordenesDeComprasRepository;

    @Autowired
    private ProductosRepository productosRepository;

    @Autowired
    private HistorialCostosRepository historialCostosRepository;

    @GetMapping
    public List<DetallesOrdenesDeComprasDTO> getAllDetallesOrdenesDeCompras() {
        List<DetallesOrdenesDeCompras> detalles = detallesOrdenesDeComprasRepository.findAll();
        List<DetallesOrdenesDeComprasDTO> detallesDTO = new ArrayList<>();

        for (DetallesOrdenesDeCompras detalle : detalles) {
            DetallesOrdenesDeComprasDTO dto = new DetallesOrdenesDeComprasDTO();
            dto.setId(detalle.getId());
            dto.setCantidadPz(detalle.getCantidadPz());
            dto.setCantidadKg(detalle.getCantidadKg());

            // Mapear relaciones aplanadas
            dto.setOrdenDeCompraId(detalle.getOrdenDeCompra().getId());
            dto.setProductoId(detalle.getProducto().getId());
            dto.setProductoNombre(detalle.getProducto().getNombre());
            dto.setHistorialCostoId(detalle.getHistorialCosto().getId());
            dto.setCosto(detalle.getHistorialCosto().getCosto());

            detallesDTO.add(dto);
        }

        return detallesDTO;
    }

    @GetMapping("/{id}")
    public ResponseEntity<DetallesOrdenesDeComprasDTO> getDetalleOrdenDeCompraById(@PathVariable String id) {
        Optional<DetallesOrdenesDeCompras> optionalDetalle = detallesOrdenesDeComprasRepository.findById(id);

        if (!optionalDetalle.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        DetallesOrdenesDeCompras detalle = optionalDetalle.get();
        DetallesOrdenesDeComprasDTO dto = new DetallesOrdenesDeComprasDTO();
        dto.setId(detalle.getId());
        dto.setCantidadPz(detalle.getCantidadPz());
        dto.setCantidadKg(detalle.getCantidadKg());

        // Mapear relaciones aplanadas
        dto.setOrdenDeCompraId(detalle.getOrdenDeCompra().getId());
        dto.setProductoId(detalle.getProducto().getId());
        dto.setProductoNombre(detalle.getProducto().getNombre());
        dto.setHistorialCostoId(detalle.getHistorialCosto().getId());
        dto.setCosto(detalle.getHistorialCosto().getCosto());

        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<DetallesOrdenesDeComprasDTO> createDetalleOrdenDeCompra(
            @RequestBody Map<String, Object> requestBody) {
        try {
            // Obtener datos del request
            String ordenDeCompraId = (String) requestBody.get("ordenDeCompraId");
            String productoId = (String) requestBody.get("productoId");
            String historialCostoId = (String) requestBody.get("historialCostoId");
            BigDecimal cantidadPz = new BigDecimal(requestBody.get("cantidadPz").toString());
            BigDecimal cantidadKg = new BigDecimal(requestBody.get("cantidadKg").toString());

            // Buscar las entidades relacionadas
            Optional<OrdenesDeCompras> ordenDeCompra = ordenesDeComprasRepository.findById(ordenDeCompraId);
            Optional<Productos> producto = productosRepository.findById(productoId);
            Optional<HistorialCostos> historialCosto = historialCostosRepository.findById(historialCostoId);

            if (!ordenDeCompra.isPresent() || !producto.isPresent() || !historialCosto.isPresent()) {
                return ResponseEntity.badRequest().build();
            }

            // Crear el detalle
            DetallesOrdenesDeCompras detalle = new DetallesOrdenesDeCompras();
            detalle.setOrdenDeCompra(ordenDeCompra.get());
            detalle.setProducto(producto.get());
            detalle.setHistorialCosto(historialCosto.get());
            detalle.setCantidadPz(cantidadPz);
            detalle.setCantidadKg(cantidadKg);

            // Guardar
            DetallesOrdenesDeCompras savedDetalle = detallesOrdenesDeComprasRepository.save(detalle);

            // Crear DTO para respuesta
            DetallesOrdenesDeComprasDTO dto = new DetallesOrdenesDeComprasDTO();
            dto.setId(savedDetalle.getId());
            dto.setCantidadPz(savedDetalle.getCantidadPz());
            dto.setCantidadKg(savedDetalle.getCantidadKg());
            dto.setOrdenDeCompraId(savedDetalle.getOrdenDeCompra().getId());
            dto.setProductoId(savedDetalle.getProducto().getId());
            dto.setProductoNombre(savedDetalle.getProducto().getNombre());
            dto.setHistorialCostoId(savedDetalle.getHistorialCosto().getId());
            dto.setCosto(savedDetalle.getHistorialCosto().getCosto());

            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<DetallesOrdenesDeComprasDTO> updateDetalleOrdenDeCompra(@PathVariable String id,
            @RequestBody Map<String, Object> requestBody) {
        try {
            Optional<DetallesOrdenesDeCompras> optionalDetalle = detallesOrdenesDeComprasRepository.findById(id);
            if (!optionalDetalle.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            DetallesOrdenesDeCompras detalle = optionalDetalle.get();

            // Obtener datos del request
            String ordenDeCompraId = (String) requestBody.get("ordenDeCompraId");
            String productoId = (String) requestBody.get("productoId");
            String historialCostoId = (String) requestBody.get("historialCostoId");
            BigDecimal cantidadPz = new BigDecimal(requestBody.get("cantidadPz").toString());
            BigDecimal cantidadKg = new BigDecimal(requestBody.get("cantidadKg").toString());

            // Buscar las entidades relacionadas
            Optional<OrdenesDeCompras> ordenDeCompra = ordenesDeComprasRepository.findById(ordenDeCompraId);
            Optional<Productos> producto = productosRepository.findById(productoId);
            Optional<HistorialCostos> historialCosto = historialCostosRepository.findById(historialCostoId);

            if (!ordenDeCompra.isPresent() || !producto.isPresent() || !historialCosto.isPresent()) {
                return ResponseEntity.badRequest().build();
            }

            // Actualizar el detalle
            detalle.setOrdenDeCompra(ordenDeCompra.get());
            detalle.setProducto(producto.get());
            detalle.setHistorialCosto(historialCosto.get());
            detalle.setCantidadPz(cantidadPz);
            detalle.setCantidadKg(cantidadKg);

            // Guardar
            DetallesOrdenesDeCompras savedDetalle = detallesOrdenesDeComprasRepository.save(detalle);

            // Crear DTO para respuesta
            DetallesOrdenesDeComprasDTO dto = new DetallesOrdenesDeComprasDTO();
            dto.setId(savedDetalle.getId());
            dto.setCantidadPz(savedDetalle.getCantidadPz());
            dto.setCantidadKg(savedDetalle.getCantidadKg());
            dto.setOrdenDeCompraId(savedDetalle.getOrdenDeCompra().getId());
            dto.setProductoId(savedDetalle.getProducto().getId());
            dto.setProductoNombre(savedDetalle.getProducto().getNombre());
            dto.setHistorialCostoId(savedDetalle.getHistorialCosto().getId());
            dto.setCosto(savedDetalle.getHistorialCosto().getCosto());

            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDetalleOrdenDeCompra(@PathVariable String id) {
        if (!detallesOrdenesDeComprasRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        detallesOrdenesDeComprasRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
