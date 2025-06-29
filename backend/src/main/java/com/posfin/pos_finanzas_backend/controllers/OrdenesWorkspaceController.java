package com.posfin.pos_finanzas_backend.controllers;

import com.posfin.pos_finanzas_backend.models.OrdenesWorkspace;
import com.posfin.pos_finanzas_backend.models.Workspaces;
import com.posfin.pos_finanzas_backend.models.Productos;
import com.posfin.pos_finanzas_backend.models.HistorialPrecios;
import com.posfin.pos_finanzas_backend.dtos.OrdenesWorkspaceDTO;
import com.posfin.pos_finanzas_backend.repositories.OrdenesWorkspaceRepository;
import com.posfin.pos_finanzas_backend.repositories.WorkspacesRepository;
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
@RequestMapping("/api/ordenes-workspace")
public class OrdenesWorkspaceController {

    @Autowired
    private OrdenesWorkspaceRepository ordenesWorkspaceRepository;

    @Autowired
    private WorkspacesRepository workspacesRepository;

    @Autowired
    private ProductosRepository productosRepository;

    @Autowired
    private HistorialPreciosRepository historialPreciosRepository;

    @GetMapping
    public List<OrdenesWorkspaceDTO> getAllOrdenesWorkspace() {
        List<OrdenesWorkspace> ordenes = ordenesWorkspaceRepository.findAll();
        List<OrdenesWorkspaceDTO> ordenesDTO = new ArrayList<>();

        for (OrdenesWorkspace orden : ordenes) {
            OrdenesWorkspaceDTO dto = new OrdenesWorkspaceDTO();
            dto.setId(orden.getId());
            dto.setCantidadPz(orden.getCantidadPz());
            dto.setCantidadKg(orden.getCantidadKg());

            // Mapear relaciones aplanadas
            dto.setWorkspaceId(orden.getWorkspace().getId());
            dto.setWorkspaceNombre(orden.getWorkspace().getNombre());

            dto.setProductoId(orden.getProducto().getId());
            dto.setProductoNombre(orden.getProducto().getNombre());

            dto.setHistorialPrecioId(orden.getHistorialPrecio().getId());
            dto.setPrecio(orden.getHistorialPrecio().getPrecio());

            ordenesDTO.add(dto);
        }

        return ordenesDTO;
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdenesWorkspaceDTO> getOrdenWorkspaceById(@PathVariable String id) {
        Optional<OrdenesWorkspace> optionalOrden = ordenesWorkspaceRepository.findById(id);

        if (!optionalOrden.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        OrdenesWorkspace orden = optionalOrden.get();
        OrdenesWorkspaceDTO dto = new OrdenesWorkspaceDTO();
        dto.setId(orden.getId());
        dto.setCantidadPz(orden.getCantidadPz());
        dto.setCantidadKg(orden.getCantidadKg());

        // Mapear relaciones aplanadas
        dto.setWorkspaceId(orden.getWorkspace().getId());
        dto.setWorkspaceNombre(orden.getWorkspace().getNombre());

        dto.setProductoId(orden.getProducto().getId());
        dto.setProductoNombre(orden.getProducto().getNombre());

        dto.setHistorialPrecioId(orden.getHistorialPrecio().getId());
        dto.setPrecio(orden.getHistorialPrecio().getPrecio());

        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<OrdenesWorkspaceDTO> createOrdenWorkspace(@RequestBody Map<String, Object> requestBody) {
        try {
            // Obtener datos del request
            String workspaceId = (String) requestBody.get("workspaceId");
            String productoId = (String) requestBody.get("productoId");
            String historialPrecioId = (String) requestBody.get("historialPrecioId");
            BigDecimal cantidadPz = new BigDecimal(requestBody.get("cantidadPz").toString());
            BigDecimal cantidadKg = new BigDecimal(requestBody.get("cantidadKg").toString());

            // Buscar las entidades relacionadas
            Optional<Workspaces> workspace = workspacesRepository.findById(workspaceId);
            Optional<Productos> producto = productosRepository.findById(productoId);
            Optional<HistorialPrecios> historialPrecio = historialPreciosRepository.findById(historialPrecioId);

            if (!workspace.isPresent() || !producto.isPresent() || !historialPrecio.isPresent()) {
                return ResponseEntity.badRequest().build();
            }

            // Crear la orden
            OrdenesWorkspace orden = new OrdenesWorkspace();
            orden.setWorkspace(workspace.get());
            orden.setProducto(producto.get());
            orden.setHistorialPrecio(historialPrecio.get());
            orden.setCantidadPz(cantidadPz);
            orden.setCantidadKg(cantidadKg);

            // Guardar
            OrdenesWorkspace savedOrden = ordenesWorkspaceRepository.save(orden);

            // Crear DTO para respuesta
            OrdenesWorkspaceDTO dto = new OrdenesWorkspaceDTO();
            dto.setId(savedOrden.getId());
            dto.setCantidadPz(savedOrden.getCantidadPz());
            dto.setCantidadKg(savedOrden.getCantidadKg());
            dto.setWorkspaceId(savedOrden.getWorkspace().getId());
            dto.setWorkspaceNombre(savedOrden.getWorkspace().getNombre());
            dto.setProductoId(savedOrden.getProducto().getId());
            dto.setProductoNombre(savedOrden.getProducto().getNombre());
            dto.setHistorialPrecioId(savedOrden.getHistorialPrecio().getId());
            dto.setPrecio(savedOrden.getHistorialPrecio().getPrecio());

            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrdenesWorkspaceDTO> updateOrdenWorkspace(@PathVariable String id,
            @RequestBody Map<String, Object> requestBody) {
        try {
            Optional<OrdenesWorkspace> optionalOrden = ordenesWorkspaceRepository.findById(id);
            if (!optionalOrden.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            OrdenesWorkspace orden = optionalOrden.get();

            // Obtener datos del request
            String workspaceId = (String) requestBody.get("workspaceId");
            String productoId = (String) requestBody.get("productoId");
            String historialPrecioId = (String) requestBody.get("historialPrecioId");
            BigDecimal cantidadPz = new BigDecimal(requestBody.get("cantidadPz").toString());
            BigDecimal cantidadKg = new BigDecimal(requestBody.get("cantidadKg").toString());

            // Buscar las entidades relacionadas
            Optional<Workspaces> workspace = workspacesRepository.findById(workspaceId);
            Optional<Productos> producto = productosRepository.findById(productoId);
            Optional<HistorialPrecios> historialPrecio = historialPreciosRepository.findById(historialPrecioId);

            if (!workspace.isPresent() || !producto.isPresent() || !historialPrecio.isPresent()) {
                return ResponseEntity.badRequest().build();
            }

            // Actualizar la orden
            orden.setWorkspace(workspace.get());
            orden.setProducto(producto.get());
            orden.setHistorialPrecio(historialPrecio.get());
            orden.setCantidadPz(cantidadPz);
            orden.setCantidadKg(cantidadKg);

            // Guardar
            OrdenesWorkspace savedOrden = ordenesWorkspaceRepository.save(orden);

            // Crear DTO para respuesta
            OrdenesWorkspaceDTO dto = new OrdenesWorkspaceDTO();
            dto.setId(savedOrden.getId());
            dto.setCantidadPz(savedOrden.getCantidadPz());
            dto.setCantidadKg(savedOrden.getCantidadKg());
            dto.setWorkspaceId(savedOrden.getWorkspace().getId());
            dto.setWorkspaceNombre(savedOrden.getWorkspace().getNombre());
            dto.setProductoId(savedOrden.getProducto().getId());
            dto.setProductoNombre(savedOrden.getProducto().getNombre());
            dto.setHistorialPrecioId(savedOrden.getHistorialPrecio().getId());
            dto.setPrecio(savedOrden.getHistorialPrecio().getPrecio());

            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrdenWorkspace(@PathVariable String id) {
        if (!ordenesWorkspaceRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        ordenesWorkspaceRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> partialUpdateOrdenWorkspace(@PathVariable String id,
            @RequestBody Map<String, Object> updates) {
        // 1. Buscar la entidad existente
        Optional<OrdenesWorkspace> ordenOptional = ordenesWorkspaceRepository.findById(id);
        if (ordenOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        OrdenesWorkspace ordenExistente = ordenOptional.get();

        // 2. Actualizar campos simples
        if (updates.containsKey("cantidadPz")) {
            Number cantidadPzValue = (Number) updates.get("cantidadPz");
            ordenExistente.setCantidadPz(new BigDecimal(cantidadPzValue.toString()));
        }
        if (updates.containsKey("cantidadKg")) {
            Number cantidadKgValue = (Number) updates.get("cantidadKg");
            ordenExistente.setCantidadKg(new BigDecimal(cantidadKgValue.toString()));
        }

        // 3. Lógica para actualizar la relación con Workspaces
        if (updates.containsKey("workspace")) {
            Map<String, String> workspaceMap = (Map<String, String>) updates.get("workspace");
            String workspaceId = workspaceMap.get("id");
            Optional<Workspaces> workspaceOpt = workspacesRepository.findById(workspaceId);
            if (workspaceOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Error: El Workspace con ID " + workspaceId + " no existe.");
            }
            ordenExistente.setWorkspace(workspaceOpt.get());
        }

        // 4. Lógica para actualizar la relación con Productos
        if (updates.containsKey("producto")) {
            Map<String, String> productoMap = (Map<String, String>) updates.get("producto");
            String productoId = productoMap.get("id");
            Optional<Productos> productoOpt = productosRepository.findById(productoId);
            if (productoOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Error: El Producto con ID " + productoId + " no existe.");
            }
            ordenExistente.setProducto(productoOpt.get());
        }

        // 5. Lógica para actualizar la relación con HistorialPrecios
        if (updates.containsKey("historialPrecio")) {
            Map<String, String> historialPrecioMap = (Map<String, String>) updates.get("historialPrecio");
            String historialPrecioId = historialPrecioMap.get("id");
            Optional<HistorialPrecios> historialPrecioOpt = historialPreciosRepository.findById(historialPrecioId);
            if (historialPrecioOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Error: El Historial de Precio con ID " + historialPrecioId + " no existe.");
            }
            ordenExistente.setHistorialPrecio(historialPrecioOpt.get());
        }

        // 6. Guardar y devolver
        OrdenesWorkspace ordenActualizada = ordenesWorkspaceRepository.save(ordenExistente);

        // Crear DTO para respuesta
        OrdenesWorkspaceDTO dto = new OrdenesWorkspaceDTO();
        dto.setId(ordenActualizada.getId());
        dto.setCantidadPz(ordenActualizada.getCantidadPz());
        dto.setCantidadKg(ordenActualizada.getCantidadKg());
        dto.setWorkspaceId(ordenActualizada.getWorkspace().getId());
        dto.setWorkspaceNombre(ordenActualizada.getWorkspace().getNombre());
        dto.setProductoId(ordenActualizada.getProducto().getId());
        dto.setProductoNombre(ordenActualizada.getProducto().getNombre());
        dto.setHistorialPrecioId(ordenActualizada.getHistorialPrecio().getId());
        dto.setPrecio(ordenActualizada.getHistorialPrecio().getPrecio());

        return ResponseEntity.ok(dto);
    }
}
