package com.posfin.pos_finanzas_backend.controllers;

import com.posfin.pos_finanzas_backend.models.Roles;
import com.posfin.pos_finanzas_backend.repositories.RolesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/roles")
public class RolesController {
    @Autowired
    private RolesRepository rolesRepository;

    @GetMapping
    public List<Roles> getAllRoles() {
        return rolesRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Roles> getRoleById(@PathVariable String id) {
        Optional<Roles> role = rolesRepository.findById(id);
        return role.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Roles createRole(@RequestBody Roles role) {
        return rolesRepository.save(role);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Roles> updateRole(@PathVariable String id, @RequestBody Roles roleDetails) {
        Optional<Roles> optionalRole = rolesRepository.findById(id);
        if (!optionalRole.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        Roles role = optionalRole.get();
        role.setRoles(roleDetails.getRoles());
        return ResponseEntity.ok(rolesRepository.save(role));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable String id) {
        if (!rolesRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        rolesRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> partialUpdateRole(@PathVariable String id, @RequestBody Map<String, Object> updates) {
        // 1. Buscar la entidad existente
        Optional<Roles> roleOptional = rolesRepository.findById(id);
        if (roleOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Roles roleExistente = roleOptional.get();

        // 2. Actualizar campos simples
        if (updates.containsKey("roles")) {
            roleExistente.setRoles((String) updates.get("roles"));
        }

        // 3. Guardar y devolver
        Roles roleActualizado = rolesRepository.save(roleExistente);
        return ResponseEntity.ok(roleActualizado);
    }
}
