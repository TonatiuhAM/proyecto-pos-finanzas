package com.posfin.pos_finanzas_backend.controllers;

import com.posfin.pos_finanzas_backend.models.Productos;
import com.posfin.pos_finanzas_backend.repositories.ProductosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/productos") // Todas las rutas aquí empezarán con /api/productos
public class ProductosController {
    @Autowired
    private ProductosRepository productosRepository;

    @GetMapping
    public List<Productos> getAllProductos() {
        return productosRepository.findAll();
    }

    @GetMapping("/productos/{id}")
    public ResponseEntity<Productos> getProductoById(@PathVariable String id) {
        Optional<Productos> producto = productosRepository.findById(id);
        return producto.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Productos createProducto(@RequestBody Productos producto) {
        return productosRepository.save(producto);
    }

    @PutMapping("/productos/{id}")
    public ResponseEntity<Productos> updateProducto(@PathVariable String id, @RequestBody Productos productoDetails) {
        Optional<Productos> optionalProducto = productosRepository.findById(id);
        if (!optionalProducto.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        Productos producto = optionalProducto.get();
        producto.setNombre(productoDetails.getNombre());
        producto.setCategoriaProductosId(productoDetails.getCategoriaProductosId());
        producto.setProveedorId(productoDetails.getProveedorId());
        producto.setEstadosId(productoDetails.getEstadosId());
        return ResponseEntity.ok(productosRepository.save(producto));
    }

    @DeleteMapping("/productos/{id}")
    public ResponseEntity<Void> deleteProducto(@PathVariable String id) {
        if (!productosRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        productosRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
