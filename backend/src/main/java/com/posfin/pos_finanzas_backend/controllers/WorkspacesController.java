package com.posfin.pos_finanzas_backend.controllers;

import com.posfin.pos_finanzas_backend.models.Workspaces;
import com.posfin.pos_finanzas_backend.dtos.WorkspaceStatusDTO;
import com.posfin.pos_finanzas_backend.dtos.WorkspacesDTO;
import com.posfin.pos_finanzas_backend.dtos.TicketVentaDTO;
import com.posfin.pos_finanzas_backend.dtos.FinalizarVentaRequestDTO;
import com.posfin.pos_finanzas_backend.dtos.VentaFinalizadaResponseDTO;
import com.posfin.pos_finanzas_backend.dtos.OrdenesDeVentasDTO;
import com.posfin.pos_finanzas_backend.repositories.WorkspacesRepository;
import com.posfin.pos_finanzas_backend.repositories.OrdenesWorkspaceRepository;
import com.posfin.pos_finanzas_backend.services.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/workspaces")
public class WorkspacesController {
    @Autowired
    private WorkspacesRepository workspacesRepository;

    @Autowired
    private OrdenesWorkspaceRepository ordenesWorkspaceRepository;

    @Autowired
    private VentaService ventaService;

    @GetMapping
    public List<WorkspacesDTO> getAllWorkspaces() {
        return workspacesRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkspacesDTO> getWorkspaceById(@PathVariable String id) {
        Optional<Workspaces> workspace = workspacesRepository.findById(id);
        return workspace.map(w -> ResponseEntity.ok(convertToDTO(w)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<WorkspacesDTO> createWorkspace(@RequestBody Map<String, Object> requestBody) {
        try {
            String nombre = (String) requestBody.get("nombre");
            Boolean permanente = requestBody.containsKey("permanente") ? (Boolean) requestBody.get("permanente")
                    : false;

            Workspaces workspace = new Workspaces();
            workspace.setNombre(nombre);
            workspace.setPermanente(permanente);

            Workspaces savedWorkspace = workspacesRepository.save(workspace);
            return ResponseEntity.ok(convertToDTO(savedWorkspace));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkspacesDTO> updateWorkspace(@PathVariable String id,
            @RequestBody Map<String, Object> requestBody) {
        if (!workspacesRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        try {
            String nombre = (String) requestBody.get("nombre");
            Boolean permanente = requestBody.containsKey("permanente") ? (Boolean) requestBody.get("permanente")
                    : false;

            Workspaces workspace = new Workspaces();
            workspace.setId(id);
            workspace.setNombre(nombre);
            workspace.setPermanente(permanente);

            Workspaces updatedWorkspace = workspacesRepository.save(workspace);
            return ResponseEntity.ok(convertToDTO(updatedWorkspace));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkspace(@PathVariable String id) {
        if (!workspacesRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        workspacesRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> partialUpdateWorkspace(@PathVariable String id, @RequestBody Map<String, Object> updates) {
        // 1. Buscar la entidad existente
        Optional<Workspaces> workspaceOptional = workspacesRepository.findById(id);
        if (workspaceOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Workspaces workspaceExistente = workspaceOptional.get();

        // 2. Actualizar campos simples
        if (updates.containsKey("nombre")) {
            workspaceExistente.setNombre((String) updates.get("nombre"));
        }
        if (updates.containsKey("permanente")) {
            workspaceExistente.setPermanente((Boolean) updates.get("permanente"));
        }

        // 3. Guardar y devolver
        Workspaces workspaceActualizado = workspacesRepository.save(workspaceExistente);
        return ResponseEntity.ok(convertToDTO(workspaceActualizado));
    }

    @GetMapping("/status")
    public List<WorkspaceStatusDTO> getAllWorkspacesWithStatus() {
        System.out.println("🏢 [WORKSPACE-DEBUG] === INICIANDO getAllWorkspacesWithStatus ===");
        
        try {
            // Obtener información de seguridad
            org.springframework.security.core.Authentication auth = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            System.out.println("🏢 [WORKSPACE-DEBUG] Usuario autenticado: " + 
                (auth != null ? auth.getName() : "null"));
            System.out.println("🏢 [WORKSPACE-DEBUG] Authorities: " + 
                (auth != null ? auth.getAuthorities() : "null"));
            System.out.println("🏢 [WORKSPACE-DEBUG] Is authenticated: " + 
                (auth != null ? auth.isAuthenticated() : "false"));
            
            System.out.println("🏢 [WORKSPACE-DEBUG] Obteniendo lista de workspaces...");
            List<Workspaces> workspaces = workspacesRepository.findAll();
            System.out.println("🏢 [WORKSPACE-DEBUG] Workspaces encontrados: " + workspaces.size());

            List<WorkspaceStatusDTO> result = workspaces.stream().map(workspace -> {
                try {
                    System.out.println("🏢 [WORKSPACE-DEBUG] Procesando workspace: " + workspace.getId() + " - " + workspace.getNombre());
                    
                    // Contar órdenes asociadas a este workspace
                    long cantidadOrdenes = ordenesWorkspaceRepository.countByWorkspaceId(workspace.getId());
                    System.out.println("🏢 [WORKSPACE-DEBUG] Ordenes para workspace " + workspace.getId() + ": " + cantidadOrdenes);

                    String estado;
                    if (cantidadOrdenes == 0) {
                        estado = "disponible";
                    } else if (workspace.getSolicitudCuenta() != null && workspace.getSolicitudCuenta()) {
                        // Si tiene órdenes Y solicita cuenta
                        estado = "cuenta";
                    } else if (cantidadOrdenes > 0) {
                        // Si solo tiene órdenes pero no solicita cuenta
                        estado = "ocupado";
                    } else {
                        estado = "disponible";
                    }
                    
                    System.out.println("🏢 [WORKSPACE-DEBUG] Estado calculado para " + workspace.getId() + ": " + estado);

                    WorkspaceStatusDTO dto = new WorkspaceStatusDTO(
                            workspace.getId(),
                            workspace.getNombre(),
                            estado,
                            (int) cantidadOrdenes,
                            workspace.getPermanente());
                    
                    System.out.println("🏢 [WORKSPACE-DEBUG] DTO creado exitosamente para: " + workspace.getId());
                    return dto;
                    
                } catch (Exception e) {
                    System.err.println("🏢 [WORKSPACE-ERROR] Error procesando workspace " + workspace.getId() + ": " + e.getMessage());
                    e.printStackTrace();
                    throw new RuntimeException("Error procesando workspace " + workspace.getId(), e);
                }
            }).collect(Collectors.toList());
            
            System.out.println("🏢 [WORKSPACE-DEBUG] === COMPLETADO getAllWorkspacesWithStatus - Retornando " + result.size() + " elementos ===");
            return result;
            
        } catch (Exception e) {
            System.err.println("🏢 [WORKSPACE-ERROR] === ERROR GENERAL en getAllWorkspacesWithStatus ===");
            System.err.println("🏢 [WORKSPACE-ERROR] Mensaje: " + e.getMessage());
            System.err.println("🏢 [WORKSPACE-ERROR] Tipo: " + e.getClass().getSimpleName());
            e.printStackTrace();
            throw new RuntimeException("Error obteniendo workspaces con estado", e);
        }
    }

    /**
     * Cambia el estado de solicitud de cuenta de un workspace.
     * Endpoint: PATCH /api/workspaces/{id}/solicitar-cuenta
     */
    @PatchMapping("/{id}/solicitar-cuenta")
    public ResponseEntity<WorkspaceStatusDTO> cambiarSolicitudCuenta(@PathVariable String id,
            @RequestBody Map<String, Object> requestBody) {
        Optional<Workspaces> workspaceOpt = workspacesRepository.findById(id);
        if (workspaceOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Workspaces workspace = workspaceOpt.get();

        // Obtener el valor de solicitudCuenta del request body
        Boolean solicitudCuenta = (Boolean) requestBody.get("solicitudCuenta");
        if (solicitudCuenta == null) {
            return ResponseEntity.badRequest().build();
        }

        workspace.setSolicitudCuenta(solicitudCuenta);
        workspacesRepository.save(workspace);

        // Recalcular el estado y devolver el DTO actualizado
        long cantidadOrdenes = ordenesWorkspaceRepository.countByWorkspaceId(workspace.getId());

        String estado;
        if (cantidadOrdenes == 0) {
            estado = "disponible";
        } else if (workspace.getSolicitudCuenta() != null && workspace.getSolicitudCuenta()) {
            estado = "cuenta";
        } else if (cantidadOrdenes > 0) {
            estado = "ocupado";
        } else {
            estado = "disponible";
        }

        WorkspaceStatusDTO response = new WorkspaceStatusDTO(
                workspace.getId(),
                workspace.getNombre(),
                estado,
                (int) cantidadOrdenes,
                workspace.getPermanente());

        return ResponseEntity.ok(response);
    }

    // ===== ENDPOINTS ESPECÍFICOS PARA PUNTO DE VENTA =====

    /**
     * Obtiene todas las órdenes de un workspace específico.
     */
    @GetMapping("/{id}/ordenes")
    public ResponseEntity<List<com.posfin.pos_finanzas_backend.dtos.OrdenesWorkspaceDTO>> getOrdenesByWorkspace(
            @PathVariable String id) {
        // Verificar que el workspace existe
        if (!workspacesRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        // Obtener órdenes del workspace
        List<com.posfin.pos_finanzas_backend.models.OrdenesWorkspace> ordenes = ordenesWorkspaceRepository
                .findByWorkspaceId(id);

        // Convertir a DTOs
        List<com.posfin.pos_finanzas_backend.dtos.OrdenesWorkspaceDTO> ordenesDTO = ordenes.stream()
                .map(this::convertOrdenToDTO)
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(ordenesDTO);
    }

    /**
     * Limpia todas las órdenes de un workspace específico.
     */
    @DeleteMapping("/{id}/ordenes")
    public ResponseEntity<Void> limpiarOrdenesWorkspace(@PathVariable String id) {
        // Verificar que el workspace existe
        if (!workspacesRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        // Obtener y eliminar todas las órdenes del workspace
        List<com.posfin.pos_finanzas_backend.models.OrdenesWorkspace> ordenes = ordenesWorkspaceRepository
                .findByWorkspaceId(id);
        ordenesWorkspaceRepository.deleteAll(ordenes);

        return ResponseEntity.noContent().build();
    }

    /**
     * Genera un ticket de venta para un workspace con estado "cuenta".
     * Endpoint: GET /api/workspaces/{id}/ticket
     */
    @GetMapping("/{id}/ticket")
    public ResponseEntity<TicketVentaDTO> generarTicketVenta(@PathVariable String id) {
        // Verificar que el workspace existe
        Optional<Workspaces> workspaceOpt = workspacesRepository.findById(id);
        if (workspaceOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Workspaces workspace = workspaceOpt.get();

        // Verificar que el workspace tiene órdenes
        List<com.posfin.pos_finanzas_backend.models.OrdenesWorkspace> ordenes = ordenesWorkspaceRepository
                .findByWorkspaceId(id);

        if (ordenes.isEmpty()) {
            return ResponseEntity.badRequest().build(); // No hay productos para facturar
        }

        // Convertir órdenes a productos del ticket
        List<TicketVentaDTO.ProductoTicketDTO> productosTicket = new ArrayList<>();
        BigDecimal totalGeneral = BigDecimal.ZERO;

        for (com.posfin.pos_finanzas_backend.models.OrdenesWorkspace orden : ordenes) {
            // Calcular total por producto (precio * cantidad)
            BigDecimal cantidadTotal = (orden.getCantidadPz() != null ? orden.getCantidadPz() : BigDecimal.ZERO)
                    .add(orden.getCantidadKg() != null ? orden.getCantidadKg() : BigDecimal.ZERO);
            BigDecimal totalPorProducto = orden.getHistorialPrecio().getPrecio().multiply(cantidadTotal);

            TicketVentaDTO.ProductoTicketDTO productoTicket = new TicketVentaDTO.ProductoTicketDTO(
                    orden.getProducto().getId(),
                    orden.getProducto().getNombre(),
                    orden.getCantidadPz(),
                    orden.getCantidadKg(),
                    orden.getHistorialPrecio().getPrecio(),
                    totalPorProducto);

            productosTicket.add(productoTicket);
            totalGeneral = totalGeneral.add(totalPorProducto);
        }

        // Crear el ticket de venta
        TicketVentaDTO ticket = new TicketVentaDTO(
                workspace.getId(),
                workspace.getNombre(),
                productosTicket,
                totalGeneral,
                ordenes.size());

        return ResponseEntity.ok(ticket);
    }

    /**
     * Finaliza la venta de un workspace y convierte las órdenes temporales en una
     * venta permanente.
     * Endpoint: POST /api/workspaces/{id}/finalizar-venta
     */
    @PostMapping("/{id}/finalizar-venta")
    public ResponseEntity<VentaFinalizadaResponseDTO> finalizarVentaWorkspace(@PathVariable String id,
            @RequestBody FinalizarVentaRequestDTO request) {
        try {
            // Verificar que el workspace existe
            Optional<Workspaces> workspaceOpt = workspacesRepository.findById(id);
            if (workspaceOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Workspaces workspace = workspaceOpt.get();

            // Procesar la venta usando el servicio existente
            OrdenesDeVentasDTO ventaCreada = ventaService.procesarVentaDesdeWorkspace(
                    id,
                    request.getClienteId(),
                    request.getUsuarioId(),
                    request.getMetodoPagoId());

            // Verificar si el workspace es temporal o permanente para manejar
            // apropiadamente
            if (workspace.getPermanente()) {
                // Workspace permanente: solo limpiar estado de solicitud de cuenta
                workspace.setSolicitudCuenta(false);
                workspacesRepository.save(workspace);
            } else {
                // Workspace temporal: eliminar completamente después de procesar venta
                workspacesRepository.delete(workspace);
            }

            // Crear respuesta de confirmación
            VentaFinalizadaResponseDTO response = new VentaFinalizadaResponseDTO(
                    ventaCreada.getId(),
                    workspace.getNombre(),
                    ventaCreada.getTotalVenta(),
                    ventaCreada.getFechaOrden(),
                    ventaCreada.getMetodoPagoNombre(),
                    ventaCreada.getUsuarioNombre(),
                    ventaCreada.getPersonaNombre(),
                    0, // cantidadProductos - podríamos calcularlo si es necesario
                    "Venta procesada exitosamente");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // En caso de error, crear respuesta de error
            VentaFinalizadaResponseDTO errorResponse = new VentaFinalizadaResponseDTO();
            errorResponse.setMensaje("Error al procesar la venta: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // Método auxiliar para convertir Workspaces a WorkspacesDTO
    private WorkspacesDTO convertToDTO(Workspaces workspace) {
        WorkspacesDTO dto = new WorkspacesDTO();
        dto.setId(workspace.getId());
        dto.setNombre(workspace.getNombre());
        dto.setPermanente(workspace.getPermanente());
        return dto;
    }

    // Método auxiliar para convertir OrdenesWorkspace a DTO
    private com.posfin.pos_finanzas_backend.dtos.OrdenesWorkspaceDTO convertOrdenToDTO(
            com.posfin.pos_finanzas_backend.models.OrdenesWorkspace orden) {
        com.posfin.pos_finanzas_backend.dtos.OrdenesWorkspaceDTO dto = new com.posfin.pos_finanzas_backend.dtos.OrdenesWorkspaceDTO();

        dto.setId(orden.getId());
        dto.setCantidadPz(orden.getCantidadPz());
        dto.setCantidadKg(orden.getCantidadKg());

        // Mapear workspace
        dto.setWorkspaceId(orden.getWorkspace().getId());
        dto.setWorkspaceNombre(orden.getWorkspace().getNombre());

        // Mapear producto
        dto.setProductoId(orden.getProducto().getId());
        dto.setProductoNombre(orden.getProducto().getNombre());

        // Mapear precio
        dto.setHistorialPrecioId(orden.getHistorialPrecio().getId());
        dto.setPrecio(orden.getHistorialPrecio().getPrecio());

        return dto;
    }
}
