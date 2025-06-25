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
}
