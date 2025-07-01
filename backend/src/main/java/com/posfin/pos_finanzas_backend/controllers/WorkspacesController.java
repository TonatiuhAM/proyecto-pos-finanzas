package com.posfin.pos_finanzas_backend.controllers;

import com.posfin.pos_finanzas_backend.models.Workspaces;
import com.posfin.pos_finanzas_backend.dtos.WorkspaceStatusDTO;
import com.posfin.pos_finanzas_backend.dtos.WorkspacesDTO;
import com.posfin.pos_finanzas_backend.repositories.WorkspacesRepository;
import com.posfin.pos_finanzas_backend.repositories.OrdenesWorkspaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/workspaces")
public class WorkspacesController {
    @Autowired
    private WorkspacesRepository workspacesRepository;

    @Autowired
    private OrdenesWorkspaceRepository ordenesWorkspaceRepository;

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
        List<Workspaces> workspaces = workspacesRepository.findAll();

        return workspaces.stream().map(workspace -> {
            // Contar órdenes asociadas a este workspace
            long cantidadOrdenes = ordenesWorkspaceRepository.countByWorkspaceId(workspace.getId());

            String estado;
            if (cantidadOrdenes == 0) {
                estado = "disponible";
            } else if (cantidadOrdenes > 0) {
                // Aquí podrías agregar lógica más compleja para determinar si es "ocupado" o
                // "cuenta"
                // Por ahora, simplemente usaremos "ocupado" si hay órdenes
                estado = "ocupado";
            } else {
                estado = "disponible";
            }

            return new WorkspaceStatusDTO(
                    workspace.getId(),
                    workspace.getNombre(),
                    estado,
                    (int) cantidadOrdenes);
        }).collect(Collectors.toList());
    }

    // Método auxiliar para convertir Workspaces a WorkspacesDTO
    private WorkspacesDTO convertToDTO(Workspaces workspace) {
        WorkspacesDTO dto = new WorkspacesDTO();
        dto.setId(workspace.getId());
        dto.setNombre(workspace.getNombre());
        dto.setPermanente(workspace.getPermanente());
        return dto;
    }
}
