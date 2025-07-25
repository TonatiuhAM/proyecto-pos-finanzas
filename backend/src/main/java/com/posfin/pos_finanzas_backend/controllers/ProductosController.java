package com.posfin.pos_finanzas_backend.controllers;

import com.posfin.pos_finanzas_backend.models.Productos;
import com.posfin.pos_finanzas_backend.models.CategoriasProductos;
import com.posfin.pos_finanzas_backend.models.Personas;
import com.posfin.pos_finanzas_backend.models.Estados;
import com.posfin.pos_finanzas_backend.models.HistorialPrecios;
import com.posfin.pos_finanzas_backend.models.HistorialCostos;
import com.posfin.pos_finanzas_backend.dtos.ProductosDTO;
import com.posfin.pos_finanzas_backend.dtos.ProductoCreacionDTO;
import com.posfin.pos_finanzas_backend.repositories.ProductosRepository;
import com.posfin.pos_finanzas_backend.repositories.CategoriasProductosRepository;
import com.posfin.pos_finanzas_backend.repositories.PersonasRepository;
import com.posfin.pos_finanzas_backend.repositories.EstadosRepository;
import com.posfin.pos_finanzas_backend.repositories.HistorialPreciosRepository;
import com.posfin.pos_finanzas_backend.repositories.HistorialCostosRepository;
import com.posfin.pos_finanzas_backend.services.ProductoService;
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

    @Autowired
    private ProductoService productoService;

    @Autowired
    private HistorialPreciosRepository historialPreciosRepository;

    @Autowired
    private HistorialCostosRepository historialCostosRepository;

    @GetMapping
    public List<ProductosDTO> getAllProductos() {
        return productosRepository.findAll().stream()
                .map(productoService::convertToDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductosDTO> getProductoById(@PathVariable String id) {
        Optional<Productos> optionalProducto = productosRepository.findById(id);
        return optionalProducto.map(producto -> ResponseEntity.ok(productoService.convertToDTO(producto)))
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

            return ResponseEntity.ok(productoService.convertToDTO(savedProducto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/completo")
    public ResponseEntity<?> createProductoCompleto(@RequestBody ProductoCreacionDTO productoCreacionDTO) {
        try {
            ProductosDTO nuevoProducto = productoService.crearProductoCompleto(productoCreacionDTO);
            return ResponseEntity.ok(nuevoProducto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al crear el producto: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProducto(@PathVariable String id,
            @RequestBody Map<String, Object> requestBody) {
        try {
            Optional<Productos> optionalProducto = productosRepository.findById(id);
            if (!optionalProducto.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Productos producto = optionalProducto.get();

            // Actualizar nombre si viene en el request
            if (requestBody.containsKey("nombre")) {
                String nombre = (String) requestBody.get("nombre");
                producto.setNombre(nombre);
            }

            // Actualizar categoría solo si viene en el request
            if (requestBody.containsKey("categoriasProductosId")) {
                String categoriasProductosId = (String) requestBody.get("categoriasProductosId");
                if (categoriasProductosId != null) {
                    Optional<CategoriasProductos> categoria = categoriasProductosRepository
                            .findById(categoriasProductosId);
                    if (!categoria.isPresent()) {
                        return ResponseEntity.badRequest().body("Categoría no encontrada: " + categoriasProductosId);
                    }
                    producto.setCategoriasProductos(categoria.get());
                }
            }

            // Actualizar proveedor solo si viene en el request
            if (requestBody.containsKey("proveedorId")) {
                String proveedorId = (String) requestBody.get("proveedorId");
                if (proveedorId != null) {
                    Optional<Personas> proveedor = personasRepository.findById(proveedorId);
                    if (!proveedor.isPresent()) {
                        return ResponseEntity.badRequest().body("Proveedor no encontrado: " + proveedorId);
                    }
                    producto.setProveedor(proveedor.get());
                }
            }

            // Guardar producto
            Productos savedProducto = productosRepository.save(producto);

            // Actualizar precios si han cambiado
            if (requestBody.containsKey("precioVenta") || requestBody.containsKey("precioVentaActual")) {
                Double precioVentaActual = null;
                if (requestBody.get("precioVenta") != null) {
                    precioVentaActual = ((Number) requestBody.get("precioVenta")).doubleValue();
                } else if (requestBody.get("precioVentaActual") != null) {
                    precioVentaActual = ((Number) requestBody.get("precioVentaActual")).doubleValue();
                }

                if (precioVentaActual != null) {
                    // Verificar si el precio ha cambiado
                    Optional<HistorialPrecios> precioActualOpt = historialPreciosRepository
                            .findLatestByProducto(savedProducto);
                    boolean precioHaCambiado = !precioActualOpt.isPresent() ||
                            !precioActualOpt.get().getPrecio().equals(java.math.BigDecimal.valueOf(precioVentaActual));

                    if (precioHaCambiado) {
                        HistorialPrecios nuevoPrecio = new HistorialPrecios();
                        nuevoPrecio.setProductos(savedProducto);
                        nuevoPrecio.setPrecio(java.math.BigDecimal.valueOf(precioVentaActual));
                        nuevoPrecio.setFechaDeRegistro(java.time.ZonedDateTime.now());
                        historialPreciosRepository.save(nuevoPrecio);
                    }
                }
            }

            if (requestBody.containsKey("precioCompra") || requestBody.containsKey("precioCompraActual")) {
                Double precioCompraActual = null;
                if (requestBody.get("precioCompra") != null) {
                    precioCompraActual = ((Number) requestBody.get("precioCompra")).doubleValue();
                } else if (requestBody.get("precioCompraActual") != null) {
                    precioCompraActual = ((Number) requestBody.get("precioCompraActual")).doubleValue();
                }

                if (precioCompraActual != null) {
                    // Verificar si el costo ha cambiado
                    Optional<HistorialCostos> costoActualOpt = historialCostosRepository
                            .findLatestByProducto(savedProducto);
                    boolean costoHaCambiado = !costoActualOpt.isPresent() ||
                            !costoActualOpt.get().getCosto().equals(java.math.BigDecimal.valueOf(precioCompraActual));

                    if (costoHaCambiado) {
                        HistorialCostos nuevoCosto = new HistorialCostos();
                        nuevoCosto.setProductos(savedProducto);
                        nuevoCosto.setCosto(java.math.BigDecimal.valueOf(precioCompraActual));
                        nuevoCosto.setFechaDeRegistro(java.time.ZonedDateTime.now());
                        historialCostosRepository.save(nuevoCosto);
                    }
                }
            }

            return ResponseEntity.ok(productoService.convertToDTO(savedProducto));
        } catch (Exception e) {
            e.printStackTrace(); // Para debug
            return ResponseEntity.badRequest().body("Error al actualizar producto: " + e.getMessage());
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

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<?> desactivarProducto(@PathVariable String id) {
        try {
            ProductosDTO productoDesactivado = productoService.desactivarProducto(id);
            return ResponseEntity.ok(productoDesactivado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al desactivar el producto: " + e.getMessage());
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> partialUpdateProducto(@PathVariable String id, @RequestBody Map<String, Object> updates) {
        // 1. Buscar la entidad existente
        Optional<Productos> productoOptional = productosRepository.findById(id);
        if (productoOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Productos productoExistente = productoOptional.get();

        // 2. Actualizar campos simples
        if (updates.containsKey("nombre")) {
            productoExistente.setNombre((String) updates.get("nombre"));
        }

        // 3. Lógica para actualizar la relación con CategoriasProductos
        if (updates.containsKey("categoriasProductos")) {
            Map<String, String> categoriasProductosMap = (Map<String, String>) updates.get("categoriasProductos");
            String categoriasProductosId = categoriasProductosMap.get("id");
            Optional<CategoriasProductos> categoriasProductosOpt = categoriasProductosRepository
                    .findById(categoriasProductosId);
            if (categoriasProductosOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Error: La Categoría de Producto con ID " + categoriasProductosId + " no existe.");
            }
            productoExistente.setCategoriasProductos(categoriasProductosOpt.get());
        }

        // 4. Lógica para actualizar la relación con Proveedor (Personas)
        if (updates.containsKey("proveedor")) {
            Map<String, String> proveedorMap = (Map<String, String>) updates.get("proveedor");
            String proveedorId = proveedorMap.get("id");
            Optional<Personas> proveedorOpt = personasRepository.findById(proveedorId);
            if (proveedorOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Error: El Proveedor con ID " + proveedorId + " no existe.");
            }
            productoExistente.setProveedor(proveedorOpt.get());
        }

        // 5. Lógica para actualizar la relación con Estados
        if (updates.containsKey("estados")) {
            Map<String, String> estadosMap = (Map<String, String>) updates.get("estados");
            String estadosId = estadosMap.get("id");
            Optional<Estados> estadosOpt = estadosRepository.findById(estadosId);
            if (estadosOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Error: El Estado con ID " + estadosId + " no existe.");
            }
            productoExistente.setEstados(estadosOpt.get());
        }

        // 6. Guardar y devolver
        Productos productoActualizado = productosRepository.save(productoExistente);
        ProductosDTO dto = productoService.convertToDTO(productoActualizado);
        return ResponseEntity.ok(dto);
    }
}
