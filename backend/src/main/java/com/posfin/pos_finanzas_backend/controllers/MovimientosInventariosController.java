package com.posfin.pos_finanzas_backend.controllers;

import com.posfin.pos_finanzas_backend.models.MovimientosInventarios;
import com.posfin.pos_finanzas_backend.models.Productos;
import com.posfin.pos_finanzas_backend.models.Ubicaciones;
import com.posfin.pos_finanzas_backend.models.TipoMovimientos;
import com.posfin.pos_finanzas_backend.models.Usuarios;
import com.posfin.pos_finanzas_backend.dtos.MovimientosInventariosDTO;
import com.posfin.pos_finanzas_backend.repositories.MovimientosInventariosRepository;
import com.posfin.pos_finanzas_backend.repositories.ProductosRepository;
import com.posfin.pos_finanzas_backend.repositories.UbicacionesRepository;
import com.posfin.pos_finanzas_backend.repositories.TIpoMovimientosRepository;
import com.posfin.pos_finanzas_backend.repositories.UsuariosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Map;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/movimientos-inventarios")
public class MovimientosInventariosController {

    @Autowired
    private MovimientosInventariosRepository movimientosInventariosRepository;

    @Autowired
    private ProductosRepository productosRepository;

    @Autowired
    private UbicacionesRepository ubicacionesRepository;

    @Autowired
    private TIpoMovimientosRepository tipoMovimientosRepository;

    @Autowired
    private UsuariosRepository usuariosRepository;

    @GetMapping
    public List<MovimientosInventariosDTO> getAllMovimientosInventarios() {
        List<MovimientosInventarios> movimientos = movimientosInventariosRepository.findAll();
        List<MovimientosInventariosDTO> movimientosDTO = new ArrayList<>();

        for (MovimientosInventarios movimiento : movimientos) {
            MovimientosInventariosDTO dto = new MovimientosInventariosDTO();
            dto.setId(movimiento.getId());
            dto.setCantidad(movimiento.getCantidad());
            dto.setFechaMovimiento(movimiento.getFechaMovimiento());
            dto.setClaveMovimiento(movimiento.getClaveMovimiento());

            // Mapear relaciones aplanadas
            dto.setProductoId(movimiento.getProducto().getId());
            dto.setProductoNombre(movimiento.getProducto().getNombre());

            dto.setUbicacionId(movimiento.getUbicacion().getId());
            dto.setUbicacionNombre(movimiento.getUbicacion().getNombre());
            dto.setUbicacionDescripcion(movimiento.getUbicacion().getUbicacion());

            dto.setTipoMovimientoId(movimiento.getTipoMovimiento().getId());
            dto.setTipoMovimientoNombre(movimiento.getTipoMovimiento().getMovimiento());

            dto.setUsuarioId(movimiento.getUsuario().getId());
            dto.setUsuarioNombre(movimiento.getUsuario().getNombre());
            dto.setUsuarioTelefono(movimiento.getUsuario().getTelefono());

            movimientosDTO.add(dto);
        }

        return movimientosDTO;
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovimientosInventariosDTO> getMovimientoInventarioById(@PathVariable String id) {
        Optional<MovimientosInventarios> optionalMovimiento = movimientosInventariosRepository.findById(id);

        if (!optionalMovimiento.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        MovimientosInventarios movimiento = optionalMovimiento.get();
        MovimientosInventariosDTO dto = new MovimientosInventariosDTO();
        dto.setId(movimiento.getId());
        dto.setCantidad(movimiento.getCantidad());
        dto.setFechaMovimiento(movimiento.getFechaMovimiento());
        dto.setClaveMovimiento(movimiento.getClaveMovimiento());

        // Mapear relaciones aplanadas
        dto.setProductoId(movimiento.getProducto().getId());
        dto.setProductoNombre(movimiento.getProducto().getNombre());

        dto.setUbicacionId(movimiento.getUbicacion().getId());
        dto.setUbicacionNombre(movimiento.getUbicacion().getNombre());
        dto.setUbicacionDescripcion(movimiento.getUbicacion().getUbicacion());

        dto.setTipoMovimientoId(movimiento.getTipoMovimiento().getId());
        dto.setTipoMovimientoNombre(movimiento.getTipoMovimiento().getMovimiento());

        dto.setUsuarioId(movimiento.getUsuario().getId());
        dto.setUsuarioNombre(movimiento.getUsuario().getNombre());
        dto.setUsuarioTelefono(movimiento.getUsuario().getTelefono());

        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<MovimientosInventariosDTO> createMovimientoInventario(
            @RequestBody Map<String, Object> requestBody) {
        try {
            // Obtener datos del request
            String productoId = (String) requestBody.get("productoId");
            String ubicacionId = (String) requestBody.get("ubicacionId");
            String tipoMovimientoId = (String) requestBody.get("tipoMovimientoId");
            String usuarioId = (String) requestBody.get("usuarioId");
            String claveMovimiento = (String) requestBody.get("claveMovimiento");
            BigDecimal cantidad = new BigDecimal(requestBody.get("cantidad").toString());
            OffsetDateTime fechaMovimiento = OffsetDateTime.now(); // Se establece la fecha actual

            // Buscar las entidades relacionadas
            Optional<Productos> producto = productosRepository.findById(productoId);
            Optional<Ubicaciones> ubicacion = ubicacionesRepository.findById(ubicacionId);
            Optional<TipoMovimientos> tipoMovimiento = tipoMovimientosRepository.findById(tipoMovimientoId);
            Optional<Usuarios> usuario = usuariosRepository.findById(usuarioId);

            if (!producto.isPresent() || !ubicacion.isPresent() || !tipoMovimiento.isPresent()
                    || !usuario.isPresent()) {
                return ResponseEntity.badRequest().build();
            }

            // Crear el movimiento
            MovimientosInventarios movimiento = new MovimientosInventarios();
            movimiento.setProducto(producto.get());
            movimiento.setUbicacion(ubicacion.get());
            movimiento.setTipoMovimiento(tipoMovimiento.get());
            movimiento.setUsuario(usuario.get());
            movimiento.setCantidad(cantidad);
            movimiento.setFechaMovimiento(fechaMovimiento);
            movimiento.setClaveMovimiento(claveMovimiento);

            // Guardar
            MovimientosInventarios savedMovimiento = movimientosInventariosRepository.save(movimiento);

            // Crear DTO para respuesta
            MovimientosInventariosDTO dto = new MovimientosInventariosDTO();
            dto.setId(savedMovimiento.getId());
            dto.setCantidad(savedMovimiento.getCantidad());
            dto.setFechaMovimiento(savedMovimiento.getFechaMovimiento());
            dto.setClaveMovimiento(savedMovimiento.getClaveMovimiento());
            dto.setProductoId(savedMovimiento.getProducto().getId());
            dto.setProductoNombre(savedMovimiento.getProducto().getNombre());
            dto.setUbicacionId(savedMovimiento.getUbicacion().getId());
            dto.setUbicacionNombre(savedMovimiento.getUbicacion().getNombre());
            dto.setUbicacionDescripcion(savedMovimiento.getUbicacion().getUbicacion());
            dto.setTipoMovimientoId(savedMovimiento.getTipoMovimiento().getId());
            dto.setTipoMovimientoNombre(savedMovimiento.getTipoMovimiento().getMovimiento());
            dto.setUsuarioId(savedMovimiento.getUsuario().getId());
            dto.setUsuarioNombre(savedMovimiento.getUsuario().getNombre());
            dto.setUsuarioTelefono(savedMovimiento.getUsuario().getTelefono());

            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<MovimientosInventariosDTO> updateMovimientoInventario(@PathVariable String id,
            @RequestBody Map<String, Object> requestBody) {
        try {
            Optional<MovimientosInventarios> optionalMovimiento = movimientosInventariosRepository.findById(id);
            if (!optionalMovimiento.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            MovimientosInventarios movimiento = optionalMovimiento.get();

            // Obtener datos del request
            String productoId = (String) requestBody.get("productoId");
            String ubicacionId = (String) requestBody.get("ubicacionId");
            String tipoMovimientoId = (String) requestBody.get("tipoMovimientoId");
            String usuarioId = (String) requestBody.get("usuarioId");
            String claveMovimiento = (String) requestBody.get("claveMovimiento");
            BigDecimal cantidad = new BigDecimal(requestBody.get("cantidad").toString());

            // Buscar las entidades relacionadas
            Optional<Productos> producto = productosRepository.findById(productoId);
            Optional<Ubicaciones> ubicacion = ubicacionesRepository.findById(ubicacionId);
            Optional<TipoMovimientos> tipoMovimiento = tipoMovimientosRepository.findById(tipoMovimientoId);
            Optional<Usuarios> usuario = usuariosRepository.findById(usuarioId);

            if (!producto.isPresent() || !ubicacion.isPresent() || !tipoMovimiento.isPresent()
                    || !usuario.isPresent()) {
                return ResponseEntity.badRequest().build();
            }

            // Actualizar el movimiento
            movimiento.setProducto(producto.get());
            movimiento.setUbicacion(ubicacion.get());
            movimiento.setTipoMovimiento(tipoMovimiento.get());
            movimiento.setUsuario(usuario.get());
            movimiento.setCantidad(cantidad);
            movimiento.setClaveMovimiento(claveMovimiento);
            // Nota: fechaMovimiento no se actualiza para mantener la fecha original

            // Guardar
            MovimientosInventarios savedMovimiento = movimientosInventariosRepository.save(movimiento);

            // Crear DTO para respuesta
            MovimientosInventariosDTO dto = new MovimientosInventariosDTO();
            dto.setId(savedMovimiento.getId());
            dto.setCantidad(savedMovimiento.getCantidad());
            dto.setFechaMovimiento(savedMovimiento.getFechaMovimiento());
            dto.setClaveMovimiento(savedMovimiento.getClaveMovimiento());
            dto.setProductoId(savedMovimiento.getProducto().getId());
            dto.setProductoNombre(savedMovimiento.getProducto().getNombre());
            dto.setUbicacionId(savedMovimiento.getUbicacion().getId());
            dto.setUbicacionNombre(savedMovimiento.getUbicacion().getNombre());
            dto.setUbicacionDescripcion(savedMovimiento.getUbicacion().getUbicacion());
            dto.setTipoMovimientoId(savedMovimiento.getTipoMovimiento().getId());
            dto.setTipoMovimientoNombre(savedMovimiento.getTipoMovimiento().getMovimiento());
            dto.setUsuarioId(savedMovimiento.getUsuario().getId());
            dto.setUsuarioNombre(savedMovimiento.getUsuario().getNombre());
            dto.setUsuarioTelefono(savedMovimiento.getUsuario().getTelefono());

            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovimientoInventario(@PathVariable String id) {
        if (!movimientosInventariosRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        movimientosInventariosRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> partialUpdateMovimientoInventario(@PathVariable String id,
            @RequestBody Map<String, Object> updates) {
        // 1. Buscar la entidad existente
        Optional<MovimientosInventarios> movimientoOptional = movimientosInventariosRepository.findById(id);
        if (movimientoOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        MovimientosInventarios movimientoExistente = movimientoOptional.get();

        // 2. Actualizar campos simples
        if (updates.containsKey("cantidad")) {
            Number cantidadValue = (Number) updates.get("cantidad");
            movimientoExistente.setCantidad(new BigDecimal(cantidadValue.toString()));
        }
        if (updates.containsKey("claveMovimiento")) {
            movimientoExistente.setClaveMovimiento((String) updates.get("claveMovimiento"));
        }

        // 3. Lógica para actualizar la relación con Productos
        if (updates.containsKey("producto")) {
            Map<String, String> productoMap = (Map<String, String>) updates.get("producto");
            String productoId = productoMap.get("id");
            Optional<Productos> productoOpt = productosRepository.findById(productoId);
            if (productoOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Error: El Producto con ID " + productoId + " no existe.");
            }
            movimientoExistente.setProducto(productoOpt.get());
        }

        // 4. Lógica para actualizar la relación con Ubicaciones
        if (updates.containsKey("ubicacion")) {
            Map<String, String> ubicacionMap = (Map<String, String>) updates.get("ubicacion");
            String ubicacionId = ubicacionMap.get("id");
            Optional<Ubicaciones> ubicacionOpt = ubicacionesRepository.findById(ubicacionId);
            if (ubicacionOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Error: La Ubicación con ID " + ubicacionId + " no existe.");
            }
            movimientoExistente.setUbicacion(ubicacionOpt.get());
        }

        // 5. Lógica para actualizar la relación con TipoMovimientos
        if (updates.containsKey("tipoMovimiento")) {
            Map<String, String> tipoMovimientoMap = (Map<String, String>) updates.get("tipoMovimiento");
            String tipoMovimientoId = tipoMovimientoMap.get("id");
            Optional<TipoMovimientos> tipoMovimientoOpt = tipoMovimientosRepository.findById(tipoMovimientoId);
            if (tipoMovimientoOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Error: El Tipo de Movimiento con ID " + tipoMovimientoId + " no existe.");
            }
            movimientoExistente.setTipoMovimiento(tipoMovimientoOpt.get());
        }

        // 6. Lógica para actualizar la relación con Usuarios
        if (updates.containsKey("usuario")) {
            Map<String, String> usuarioMap = (Map<String, String>) updates.get("usuario");
            String usuarioId = usuarioMap.get("id");
            Optional<Usuarios> usuarioOpt = usuariosRepository.findById(usuarioId);
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Error: El Usuario con ID " + usuarioId + " no existe.");
            }
            movimientoExistente.setUsuario(usuarioOpt.get());
        }

        // 7. Guardar y devolver
        MovimientosInventarios movimientoActualizado = movimientosInventariosRepository.save(movimientoExistente);

        // Crear DTO para respuesta
        MovimientosInventariosDTO dto = new MovimientosInventariosDTO();
        dto.setId(movimientoActualizado.getId());
        dto.setCantidad(movimientoActualizado.getCantidad());
        dto.setFechaMovimiento(movimientoActualizado.getFechaMovimiento());
        dto.setClaveMovimiento(movimientoActualizado.getClaveMovimiento());
        dto.setProductoId(movimientoActualizado.getProducto().getId());
        dto.setProductoNombre(movimientoActualizado.getProducto().getNombre());
        dto.setUbicacionId(movimientoActualizado.getUbicacion().getId());
        dto.setUbicacionNombre(movimientoActualizado.getUbicacion().getNombre());
        dto.setUbicacionDescripcion(movimientoActualizado.getUbicacion().getUbicacion());
        dto.setTipoMovimientoId(movimientoActualizado.getTipoMovimiento().getId());
        dto.setTipoMovimientoNombre(movimientoActualizado.getTipoMovimiento().getMovimiento());
        dto.setUsuarioId(movimientoActualizado.getUsuario().getId());
        dto.setUsuarioNombre(movimientoActualizado.getUsuario().getNombre());
        dto.setUsuarioTelefono(movimientoActualizado.getUsuario().getTelefono());

        return ResponseEntity.ok(dto);
    }
}
