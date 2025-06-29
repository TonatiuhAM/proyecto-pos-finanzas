package com.posfin.pos_finanzas_backend.controllers;

import com.posfin.pos_finanzas_backend.models.Workspaces;
import com.posfin.pos_finanzas_backend.repositories.WorkspacesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/workspaces")
public class WorkspacesController {
    @Autowired
    private WorkspacesRepository workspacesRepository;

    @GetMapping
    public List<Workspaces> getAllWorkspaces() {
        return workspacesRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Workspaces> getWorkspaceById(@PathVariable String id) {
        Optional<Workspaces> workspace = workspacesRepository.findById(id);
        return workspace.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Workspaces createWorkspace(@RequestBody Workspaces workspace) {
        return workspacesRepository.save(workspace);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Workspaces> updateWorkspace(@PathVariable String id, @RequestBody Workspaces workspace) {
        if (!workspacesRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        workspace.setId(id);
        Workspaces updatedWorkspace = workspacesRepository.save(workspace);
        return ResponseEntity.ok(updatedWorkspace);
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

        // 3. Guardar y devolver
        Workspaces workspaceActualizado = workspacesRepository.save(workspaceExistente);
        return ResponseEntity.ok(workspaceActualizado);
    }
}
