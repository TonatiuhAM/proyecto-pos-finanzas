package com.posfin.pos_finanzas_backend.controllers;

import com.posfin.pos_finanzas_backend.models.Productos;
import com.posfin.pos_finanzas_backend.models.CategoriasProductos;
import com.posfin.pos_finanzas_backend.models.Personas;
import com.posfin.pos_finanzas_backend.models.Estados;
import com.posfin.pos_finanzas_backend.dtos.ProductosDTO;
import com.posfin.pos_finanzas_backend.repositories.ProductosRepository;
import com.posfin.pos_finanzas_backend.repositories.CategoriasProductosRepository;
import com.posfin.pos_finanzas_backend.repositories.PersonasRepository;
import com.posfin.pos_finanzas_backend.repositories.EstadosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Map;

@RestController
@RequestMapping("/api/productos")
public class ProductosController {

    @Autowired
    private ProductosRepository productosRepository;

    @Autowired
    private CategoriasProductosRepository categoriasProductosRepository;

    @Autowired
    private PersonasRepository personasRepository;

    @Autowired
    private EstadosRepository estadosRepository;

    @GetMapping
    public List<ProductosDTO> getAllProductos() {
        return productosRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductosDTO> getProductoById(@PathVariable String id) {
        Optional<Productos> optionalProducto = productosRepository.findById(id);
        return optionalProducto.map(producto -> ResponseEntity.ok(convertToDTO(producto)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ProductosDTO> createProducto(@RequestBody Map<String, Object> requestBody) {
        try {
            // Obtener datos del request
            String nombre = (String) requestBody.get("nombre");
            String categoriasProductosId = (String) requestBody.get("categoriasProductosId");
            String proveedorId = (String) requestBody.get("proveedorId");
            String estadosId = (String) requestBody.get("estadosId");

            // Buscar las entidades relacionadas
            Optional<CategoriasProductos> categoria = categoriasProductosRepository.findById(categoriasProductosId);
            Optional<Personas> proveedor = personasRepository.findById(proveedorId);
            Optional<Estados> estado = estadosRepository.findById(estadosId);

            if (!categoria.isPresent() || !proveedor.isPresent() || !estado.isPresent()) {
                return ResponseEntity.badRequest().build();
            }

            // Crear el producto
            Productos producto = new Productos();
            producto.setNombre(nombre);
            producto.setCategoriasProductos(categoria.get());
            producto.setProveedor(proveedor.get());
            producto.setEstados(estado.get());

            // Guardar
            Productos savedProducto = productosRepository.save(producto);

            return ResponseEntity.ok(convertToDTO(savedProducto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductosDTO> updateProducto(@PathVariable String id,
            @RequestBody Map<String, Object> requestBody) {
        try {
            Optional<Productos> optionalProducto = productosRepository.findById(id);
            if (!optionalProducto.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Productos producto = optionalProducto.get();

            // Obtener datos del request
            String nombre = (String) requestBody.get("nombre");
            String categoriasProductosId = (String) requestBody.get("categoriasProductosId");
            String proveedorId = (String) requestBody.get("proveedorId");
            String estadosId = (String) requestBody.get("estadosId");

            // Buscar las entidades relacionadas
            Optional<CategoriasProductos> categoria = categoriasProductosRepository.findById(categoriasProductosId);
            Optional<Personas> proveedor = personasRepository.findById(proveedorId);
            Optional<Estados> estado = estadosRepository.findById(estadosId);

            if (!categoria.isPresent() || !proveedor.isPresent() || !estado.isPresent()) {
                return ResponseEntity.badRequest().build();
            }

            // Actualizar el producto
            producto.setNombre(nombre);
            producto.setCategoriasProductos(categoria.get());
            producto.setProveedor(proveedor.get());
            producto.setEstados(estado.get());

            // Guardar
            Productos savedProducto = productosRepository.save(producto);

            return ResponseEntity.ok(convertToDTO(savedProducto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProducto(@PathVariable String id) {
        if (!productosRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        productosRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Método auxiliar para convertir Productos a ProductosDTO
    private ProductosDTO convertToDTO(Productos producto) {
        ProductosDTO dto = new ProductosDTO();
        dto.setId(producto.getId());
        dto.setNombre(producto.getNombre());

        // Mapear relación con CategoriasProductos
        if (producto.getCategoriasProductos() != null) {
            dto.setCategoriasProductosId(producto.getCategoriasProductos().getId());
            dto.setCategoriasProductosCategoria(producto.getCategoriasProductos().getCategoria());
        }

        // Mapear relación con Proveedor (Personas)
        if (producto.getProveedor() != null) {
            dto.setProveedorId(producto.getProveedor().getId());
            dto.setProveedorNombre(producto.getProveedor().getNombre());
            dto.setProveedorApellidoPaterno(producto.getProveedor().getApellidoPaterno());
            dto.setProveedorApellidoMaterno(producto.getProveedor().getApellidoMaterno());
        }

        // Mapear relación con Estados
        if (producto.getEstados() != null) {
            dto.setEstadosId(producto.getEstados().getId());
            dto.setEstadosEstado(producto.getEstados().getEstado());
        }

        return dto;
    }
}
