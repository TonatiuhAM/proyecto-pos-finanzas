package com.posfin.pos_finanzas_backend.controllers;

import com.posfin.pos_finanzas_backend.models.OrdenesDeVentas;
import com.posfin.pos_finanzas_backend.models.Personas;
import com.posfin.pos_finanzas_backend.models.Usuarios;
import com.posfin.pos_finanzas_backend.models.MetodosPago;
import com.posfin.pos_finanzas_backend.models.DetallesOrdenesDeVentas;
import com.posfin.pos_finanzas_backend.dtos.OrdenesDeVentasDTO;
import com.posfin.pos_finanzas_backend.repositories.OrdenesDeVentasRepository;
import com.posfin.pos_finanzas_backend.repositories.PersonasRepository;
import com.posfin.pos_finanzas_backend.repositories.UsuariosRepository;
import com.posfin.pos_finanzas_backend.repositories.MetodosPagoRepository;
import com.posfin.pos_finanzas_backend.repositories.DetallesOrdenesDeVentasRepository;
import com.posfin.pos_finanzas_backend.services.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/ordenes-de-ventas")
public class OrdenesDeVentasController {

    @Autowired
    private OrdenesDeVentasRepository ordenesDeVentasRepository;

    @Autowired
    private PersonasRepository personasRepository;

    @Autowired
    private UsuariosRepository usuariosRepository;

    @Autowired
    private MetodosPagoRepository metodosPagoRepository;

    @Autowired
    private DetallesOrdenesDeVentasRepository detallesOrdenesDeVentasRepository;

    @Autowired
    private VentaService ventaService;

    @GetMapping
    public List<OrdenesDeVentasDTO> getAllOrdenesDeVentas() {
        List<OrdenesDeVentas> ordenes = ordenesDeVentasRepository.findAll();
        List<OrdenesDeVentasDTO> ordenesDTO = new ArrayList<>();

        for (OrdenesDeVentas orden : ordenes) {
            OrdenesDeVentasDTO dto = new OrdenesDeVentasDTO();
            dto.setId(orden.getId());
            dto.setFechaOrden(orden.getFechaOrden());
            dto.setTotalVenta(orden.getTotalVenta());

            // Mapear relaciones aplanadas
            dto.setPersonaId(orden.getPersona().getId());
            dto.setPersonaNombre(orden.getPersona().getNombre());
            dto.setPersonaApellidoPaterno(orden.getPersona().getApellidoPaterno());
            dto.setPersonaApellidoMaterno(orden.getPersona().getApellidoMaterno());
            dto.setPersonaTelefono(orden.getPersona().getTelefono());
            dto.setPersonaEmail(orden.getPersona().getEmail());

            dto.setUsuarioId(orden.getUsuario().getId());
            dto.setUsuarioNombre(orden.getUsuario().getNombre());
            dto.setUsuarioTelefono(orden.getUsuario().getTelefono());

            dto.setMetodoPagoId(orden.getMetodoPago().getId());
            dto.setMetodoPagoNombre(orden.getMetodoPago().getMetodoPago());

            ordenesDTO.add(dto);
        }

        return ordenesDTO;
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdenesDeVentasDTO> getOrdenDeVentaById(@PathVariable String id) {
        Optional<OrdenesDeVentas> optionalOrden = ordenesDeVentasRepository.findById(id);

        if (!optionalOrden.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        OrdenesDeVentas orden = optionalOrden.get();
        OrdenesDeVentasDTO dto = new OrdenesDeVentasDTO();
        dto.setId(orden.getId());
        dto.setFechaOrden(orden.getFechaOrden());
        dto.setTotalVenta(orden.getTotalVenta());

        // Mapear relaciones aplanadas
        dto.setPersonaId(orden.getPersona().getId());
        dto.setPersonaNombre(orden.getPersona().getNombre());
        dto.setPersonaApellidoPaterno(orden.getPersona().getApellidoPaterno());
        dto.setPersonaApellidoMaterno(orden.getPersona().getApellidoMaterno());
        dto.setPersonaTelefono(orden.getPersona().getTelefono());
        dto.setPersonaEmail(orden.getPersona().getEmail());

        dto.setUsuarioId(orden.getUsuario().getId());
        dto.setUsuarioNombre(orden.getUsuario().getNombre());
        dto.setUsuarioTelefono(orden.getUsuario().getTelefono());

        dto.setMetodoPagoId(orden.getMetodoPago().getId());
        dto.setMetodoPagoNombre(orden.getMetodoPago().getMetodoPago());

        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<OrdenesDeVentasDTO> createOrdenDeVenta(@RequestBody Map<String, Object> requestBody) {
        try {
            // Obtener datos del request
            String personaId = (String) requestBody.get("personaId");
            String usuarioId = (String) requestBody.get("usuarioId");
            String metodoPagoId = (String) requestBody.get("metodoPagoId");
            BigDecimal totalVenta = new BigDecimal(requestBody.get("totalVenta").toString());
            OffsetDateTime fechaOrden = OffsetDateTime.now(); // Se establece la fecha actual

            // Buscar las entidades relacionadas
            Optional<Personas> persona = personasRepository.findById(personaId);
            Optional<Usuarios> usuario = usuariosRepository.findById(usuarioId);
            Optional<MetodosPago> metodoPago = metodosPagoRepository.findById(metodoPagoId);

            if (!persona.isPresent() || !usuario.isPresent() || !metodoPago.isPresent()) {
                return ResponseEntity.badRequest().build();
            }

            // Crear la orden
            OrdenesDeVentas orden = new OrdenesDeVentas();
            orden.setPersona(persona.get());
            orden.setUsuario(usuario.get());
            orden.setMetodoPago(metodoPago.get());
            orden.setTotalVenta(totalVenta);
            orden.setFechaOrden(fechaOrden);

            // Guardar
            OrdenesDeVentas savedOrden = ordenesDeVentasRepository.save(orden);

            // Crear DTO para respuesta
            OrdenesDeVentasDTO dto = new OrdenesDeVentasDTO();
            dto.setId(savedOrden.getId());
            dto.setFechaOrden(savedOrden.getFechaOrden());
            dto.setTotalVenta(savedOrden.getTotalVenta());
            dto.setPersonaId(savedOrden.getPersona().getId());
            dto.setPersonaNombre(savedOrden.getPersona().getNombre());
            dto.setPersonaApellidoPaterno(savedOrden.getPersona().getApellidoPaterno());
            dto.setPersonaApellidoMaterno(savedOrden.getPersona().getApellidoMaterno());
            dto.setPersonaTelefono(savedOrden.getPersona().getTelefono());
            dto.setPersonaEmail(savedOrden.getPersona().getEmail());
            dto.setUsuarioId(savedOrden.getUsuario().getId());
            dto.setUsuarioNombre(savedOrden.getUsuario().getNombre());
            dto.setUsuarioTelefono(savedOrden.getUsuario().getTelefono());
            dto.setMetodoPagoId(savedOrden.getMetodoPago().getId());
            dto.setMetodoPagoNombre(savedOrden.getMetodoPago().getMetodoPago());

            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrdenesDeVentasDTO> updateOrdenDeVenta(@PathVariable String id,
            @RequestBody Map<String, Object> requestBody) {
        try {
            Optional<OrdenesDeVentas> optionalOrden = ordenesDeVentasRepository.findById(id);
            if (!optionalOrden.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            OrdenesDeVentas orden = optionalOrden.get();

            // Obtener datos del request
            String personaId = (String) requestBody.get("personaId");
            String usuarioId = (String) requestBody.get("usuarioId");
            String metodoPagoId = (String) requestBody.get("metodoPagoId");
            BigDecimal totalVenta = new BigDecimal(requestBody.get("totalVenta").toString());

            // Buscar las entidades relacionadas
            Optional<Personas> persona = personasRepository.findById(personaId);
            Optional<Usuarios> usuario = usuariosRepository.findById(usuarioId);
            Optional<MetodosPago> metodoPago = metodosPagoRepository.findById(metodoPagoId);

            if (!persona.isPresent() || !usuario.isPresent() || !metodoPago.isPresent()) {
                return ResponseEntity.badRequest().build();
            }

            // Actualizar la orden
            orden.setPersona(persona.get());
            orden.setUsuario(usuario.get());
            orden.setMetodoPago(metodoPago.get());
            orden.setTotalVenta(totalVenta);
            // Nota: fechaOrden no se actualiza para mantener la fecha original

            // Guardar
            OrdenesDeVentas savedOrden = ordenesDeVentasRepository.save(orden);

            // Crear DTO para respuesta
            OrdenesDeVentasDTO dto = new OrdenesDeVentasDTO();
            dto.setId(savedOrden.getId());
            dto.setFechaOrden(savedOrden.getFechaOrden());
            dto.setTotalVenta(savedOrden.getTotalVenta());
            dto.setPersonaId(savedOrden.getPersona().getId());
            dto.setPersonaNombre(savedOrden.getPersona().getNombre());
            dto.setPersonaApellidoPaterno(savedOrden.getPersona().getApellidoPaterno());
            dto.setPersonaApellidoMaterno(savedOrden.getPersona().getApellidoMaterno());
            dto.setPersonaTelefono(savedOrden.getPersona().getTelefono());
            dto.setPersonaEmail(savedOrden.getPersona().getEmail());
            dto.setUsuarioId(savedOrden.getUsuario().getId());
            dto.setUsuarioNombre(savedOrden.getUsuario().getNombre());
            dto.setUsuarioTelefono(savedOrden.getUsuario().getTelefono());
            dto.setMetodoPagoId(savedOrden.getMetodoPago().getId());
            dto.setMetodoPagoNombre(savedOrden.getMetodoPago().getMetodoPago());

            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrdenDeVenta(@PathVariable String id) {
        if (!ordenesDeVentasRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        ordenesDeVentasRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> partialUpdateOrdenDeVenta(@PathVariable String id,
            @RequestBody Map<String, Object> updates) {
        // 1. Buscar la entidad existente
        Optional<OrdenesDeVentas> ordenOptional = ordenesDeVentasRepository.findById(id);
        if (ordenOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        OrdenesDeVentas ordenExistente = ordenOptional.get();

        // 2. Actualizar campos simples
        if (updates.containsKey("totalVenta")) {
            Number totalVentaValue = (Number) updates.get("totalVenta");
            ordenExistente.setTotalVenta(new BigDecimal(totalVentaValue.toString()));
        }

        // 3. Lógica para actualizar la relación con Personas
        if (updates.containsKey("persona")) {
            Map<String, String> personaMap = (Map<String, String>) updates.get("persona");
            String personaId = personaMap.get("id");
            Optional<Personas> personaOpt = personasRepository.findById(personaId);
            if (personaOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Error: La Persona con ID " + personaId + " no existe.");
            }
            ordenExistente.setPersona(personaOpt.get());
        }

        // 4. Lógica para actualizar la relación con Usuarios
        if (updates.containsKey("usuario")) {
            Map<String, String> usuarioMap = (Map<String, String>) updates.get("usuario");
            String usuarioId = usuarioMap.get("id");
            Optional<Usuarios> usuarioOpt = usuariosRepository.findById(usuarioId);
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Error: El Usuario con ID " + usuarioId + " no existe.");
            }
            ordenExistente.setUsuario(usuarioOpt.get());
        }

        // 5. Lógica para actualizar la relación con MetodosPago
        if (updates.containsKey("metodoPago")) {
            Map<String, String> metodoPagoMap = (Map<String, String>) updates.get("metodoPago");
            String metodoPagoId = metodoPagoMap.get("id");
            Optional<MetodosPago> metodoPagoOpt = metodosPagoRepository.findById(metodoPagoId);
            if (metodoPagoOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Error: El Método de Pago con ID " + metodoPagoId + " no existe.");
            }
            ordenExistente.setMetodoPago(metodoPagoOpt.get());
        }

        // 6. Guardar y devolver
        OrdenesDeVentas ordenActualizada = ordenesDeVentasRepository.save(ordenExistente);

        // Crear DTO para respuesta
        OrdenesDeVentasDTO dto = new OrdenesDeVentasDTO();
        dto.setId(ordenActualizada.getId());
        dto.setFechaOrden(ordenActualizada.getFechaOrden());
        dto.setTotalVenta(ordenActualizada.getTotalVenta());
        dto.setPersonaId(ordenActualizada.getPersona().getId());
        dto.setPersonaNombre(ordenActualizada.getPersona().getNombre());
        dto.setPersonaApellidoPaterno(ordenActualizada.getPersona().getApellidoPaterno());
        dto.setPersonaApellidoMaterno(ordenActualizada.getPersona().getApellidoMaterno());
        dto.setPersonaTelefono(ordenActualizada.getPersona().getTelefono());
        dto.setPersonaEmail(ordenActualizada.getPersona().getEmail());
        dto.setUsuarioId(ordenActualizada.getUsuario().getId());
        dto.setUsuarioNombre(ordenActualizada.getUsuario().getNombre());
        dto.setUsuarioTelefono(ordenActualizada.getUsuario().getTelefono());
        dto.setMetodoPagoId(ordenActualizada.getMetodoPago().getId());
        dto.setMetodoPagoNombre(ordenActualizada.getMetodoPago().getMetodoPago());

        return ResponseEntity.ok(dto);
    }

    // ===== ENDPOINT ESPECIAL PARA MACHINE LEARNING =====

    /**
     * Obtiene el historial de ventas en el formato requerido por la API de Machine Learning.
     * Endpoint: GET /api/ordenes-de-ventas/historial-ml
     */
    @GetMapping("/historial-ml")
    public ResponseEntity<List<Map<String, Object>>> getHistorialVentasParaML(
            @RequestParam(required = false) String fechaDesde,
            @RequestParam(required = false) Integer limite) {
        try {
            // Configurar límite por defecto
            if (limite == null || limite <= 0) {
                limite = 1000;
            }

            // Obtener todas las órdenes de ventas (o aplicar filtro de fecha si se proporciona)
            List<OrdenesDeVentas> ordenes;
            if (fechaDesde != null && !fechaDesde.trim().isEmpty()) {
                OffsetDateTime fechaLimite = OffsetDateTime.parse(fechaDesde + "T00:00:00Z");
                ordenes = ordenesDeVentasRepository.findByFechaOrdenGreaterThanEqualOrderByFechaOrdenDesc(fechaLimite);
            } else {
                ordenes = ordenesDeVentasRepository.findAllByOrderByFechaOrdenDesc();
            }

            // Limitar resultados
            if (ordenes.size() > limite) {
                ordenes = ordenes.subList(0, limite);
            }

            List<Map<String, Object>> historialML = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            // Procesar cada orden de venta
            for (OrdenesDeVentas orden : ordenes) {
                // Obtener todos los detalles de esta orden
                List<DetallesOrdenesDeVentas> detalles = detallesOrdenesDeVentasRepository.findByOrdenDeVenta(orden);

                // Crear un registro por cada producto vendido
                for (DetallesOrdenesDeVentas detalle : detalles) {
                    Map<String, Object> registroML = new HashMap<>();
                    
                    // Datos requeridos por la API ML
                    registroML.put("fecha_orden", orden.getFechaOrden().format(formatter));
                    registroML.put("productos_id", detalle.getProducto().getId());
                    registroML.put("cantidad_pz", detalle.getCantidadPz() != null ? detalle.getCantidadPz().intValue() : 0);
                    
                    // Precios desde el historial de precios
                    if (detalle.getHistorialPrecio() != null) {
                        registroML.put("precio_venta", detalle.getHistorialPrecio().getPrecio().doubleValue());
                        // HistorialPrecios no tiene costo, usar precio como referencia
                        registroML.put("costo_compra", detalle.getHistorialPrecio().getPrecio().doubleValue() * 0.7); // Estimación 70%
                    } else {
                        registroML.put("precio_venta", 0.0);
                        registroML.put("costo_compra", 0.0);
                    }
                    
                    // Información adicional útil para ML (opcional)
                    registroML.put("producto_nombre", detalle.getProducto().getNombre());
                    if (detalle.getProducto().getCategoriasProductos() != null) {
                        registroML.put("categoria", detalle.getProducto().getCategoriasProductos().getCategoria());
                    } else {
                        registroML.put("categoria", "Sin categoría");
                    }

                    historialML.add(registroML);
                }
            }

            return ResponseEntity.ok(historialML);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ===== ENDPOINT ESPECIAL PARA PROCESAR VENTA DESDE WORKSPACE =====

    /**
     * Procesa una venta completa desde las órdenes de workspace.
     * Endpoint: POST /api/workspaces/{workspaceId}/finalizar-venta
     */
    @PostMapping("/workspaces/{workspaceId}/finalizar-venta")
    public ResponseEntity<?> finalizarVentaDesdeWorkspace(
            @PathVariable String workspaceId,
            @RequestBody Map<String, Object> requestBody) {
        try {
            // Obtener parámetros del request (algunos opcionales)
            String clienteId = (String) requestBody.get("clienteId"); // Puede ser null
            String usuarioId = (String) requestBody.get("usuarioId");
            String metodoPagoId = (String) requestBody.get("metodoPagoId");

            // Validar parámetros requeridos
            if (usuarioId == null || usuarioId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Error: El ID del usuario es requerido");
            }
            if (metodoPagoId == null || metodoPagoId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Error: El ID del método de pago es requerido");
            }

            // Si no se proporciona cliente, usar el cliente por defecto
            if (clienteId == null || clienteId.trim().isEmpty()) {
                clienteId = ventaService.obtenerClientePorDefecto();
            }

            // Procesar la venta
            OrdenesDeVentasDTO ventaProcesada = ventaService.procesarVentaDesdeWorkspace(
                    workspaceId, clienteId, usuarioId, metodoPagoId);

            return ResponseEntity.ok(ventaProcesada);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body("Error de validación: " + e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body("Error de estado: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error interno del servidor: " + e.getMessage());
        }
    }

    // ===== ENDPOINT PARA DASHBOARD - ÓRDENES RECIENTES =====

    /**
     * Obtiene las órdenes de venta más recientes para mostrar en el dashboard.
     * Endpoint: GET /api/ordenes-de-ventas/recientes
     */
    @GetMapping("/recientes")
    public ResponseEntity<List<Map<String, Object>>> getOrdenesRecientes(
            @RequestParam(required = false, defaultValue = "5") Integer limite) {
        try {
            // Configurar límite seguro
            if (limite == null || limite <= 0 || limite > 50) {
                limite = 5;
            }

            // Obtener las órdenes más recientes
            List<OrdenesDeVentas> ordenesRecientes = ordenesDeVentasRepository.findAllByOrderByFechaOrdenDesc();
            
            // Limitar resultados
            if (ordenesRecientes.size() > limite) {
                ordenesRecientes = ordenesRecientes.subList(0, limite);
            }

            List<Map<String, Object>> ordenesDTO = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");

            // Procesar cada orden
            for (OrdenesDeVentas orden : ordenesRecientes) {
                Map<String, Object> ordenDTO = new HashMap<>();
                
                // Datos básicos de la orden
                ordenDTO.put("id", orden.getId());
                ordenDTO.put("fechaVenta", orden.getFechaOrden().format(formatter));
                ordenDTO.put("total", orden.getTotalVenta().doubleValue());
                
                // Información del usuario que realizó la venta
                ordenDTO.put("usuarioNombre", orden.getUsuario().getNombre());
                
                // Mesa/ubicación (simulada - podríamos obtener esto de algún campo adicional)
                // Por ahora, generar información de mesa de forma dinámica
                String mesa = generarMesaSimulada(orden.getId());
                ordenDTO.put("mesa", mesa);
                
                // Estado de la orden (fijo por ahora)
                ordenDTO.put("estado", "Completada");

                ordenesDTO.add(ordenDTO);
            }

            return ResponseEntity.ok(ordenesDTO);

        } catch (Exception e) {
            // En caso de error, devolver lista vacía para no romper el frontend
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    /**
     * Método auxiliar para generar información de mesa simulada basada en el ID de la orden.
     * En el futuro, esto podría venir de un campo real en la base de datos.
     */
    private String generarMesaSimulada(String ordenId) {
        // Usar el último carácter del ID para determinar el tipo de ubicación
        int ultimoCaracter = ordenId.charAt(ordenId.length() - 1) % 10;
        
        switch (ultimoCaracter) {
            case 0:
            case 1:
                return "Delivery";
            case 2:
            case 3:
                return "Barra";
            case 4:
                return "Mesa 1";
            case 5:
                return "Mesa 2";
            case 6:
                return "Mesa 3";
            case 7:
                return "Mesa 4";
            case 8:
                return "Mesa 5";
            case 9:
                return "Mesa 6";
            default:
                return "Mesa General";
        }
    }

    // ===== ENDPOINT PARA ESTADÍSTICAS DEL DASHBOARD =====

    /**
     * Obtiene estadísticas básicas para el dashboard.
     * Endpoint: GET /api/ordenes-de-ventas/estadisticas-dashboard
     */
    @GetMapping("/estadisticas-dashboard")
    public ResponseEntity<Map<String, Object>> getEstadisticasDashboard() {
        try {
            Map<String, Object> estadisticas = new HashMap<>();
            
            // Obtener fecha de hoy
            OffsetDateTime inicioHoy = OffsetDateTime.now().toLocalDate().atStartOfDay().atOffset(OffsetDateTime.now().getOffset());
            OffsetDateTime finHoy = inicioHoy.plusDays(1);
            
            // Estadísticas del día
            List<OrdenesDeVentas> ventasHoy = ordenesDeVentasRepository.findByFechaOrdenBetween(inicioHoy, finHoy);
            
            int totalVentasHoy = ventasHoy.size();
            double totalIngresosHoy = ventasHoy.stream()
                .mapToDouble(orden -> orden.getTotalVenta().doubleValue())
                .sum();
            
            double promedioTicket = totalVentasHoy > 0 ? totalIngresosHoy / totalVentasHoy : 0.0;
            
            // Clientes únicos atendidos (contando personas distintas)
            long clientesAtendidos = ventasHoy.stream()
                .map(orden -> orden.getPersona().getId())
                .distinct()
                .count();
            
            estadisticas.put("ventasHoy", totalVentasHoy);
            estadisticas.put("totalHoy", Math.round(totalIngresosHoy * 100.0) / 100.0); // Redondear a 2 decimales
            estadisticas.put("clientesAtendidos", (int) clientesAtendidos);
            estadisticas.put("promedioTicket", Math.round(promedioTicket * 100.0) / 100.0); // Redondear a 2 decimales
            
            return ResponseEntity.ok(estadisticas);
            
        } catch (Exception e) {
            // En caso de error, devolver estadísticas por defecto
            Map<String, Object> estadisticasDefault = new HashMap<>();
            estadisticasDefault.put("ventasHoy", 0);
            estadisticasDefault.put("totalHoy", 0.0);
            estadisticasDefault.put("clientesAtendidos", 0);
            estadisticasDefault.put("promedioTicket", 0.0);
            
            return ResponseEntity.ok(estadisticasDefault);
        }
    }
}
