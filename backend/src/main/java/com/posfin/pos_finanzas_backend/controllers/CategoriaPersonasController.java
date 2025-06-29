package com.posfin.pos_finanzas_backend.controllers;

import com.posfin.pos_finanzas_backend.models.CategoriaPersonas;
import com.posfin.pos_finanzas_backend.repositories.CategoriaPersonasRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Map;

@RestController
@RequestMapping("/api/categoria_personas")
public class CategoriaPersonasController {
    @Autowired
    private CategoriaPersonasRepository categoriaPersonasRepository;

    @GetMapping
    public List<CategoriaPersonas> getAllCategoriasPersonas() {
        return categoriaPersonasRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaPersonas> getCategoriaPersonaById(@PathVariable String id) {
        Optional<CategoriaPersonas> categoriaPersona = categoriaPersonasRepository.findById(id);
        return categoriaPersona.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public CategoriaPersonas createCategoriaPersona(@RequestBody CategoriaPersonas categoriaPersona) {
        return categoriaPersonasRepository.save(categoriaPersona);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaPersonas> updateCategoriaPersona(@PathVariable String id,
            @RequestBody CategoriaPersonas categoriaPersonaDetails) {
        Optional<CategoriaPersonas> optionalCategoriaPersona = categoriaPersonasRepository.findById(id);
        if (!optionalCategoriaPersona.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        CategoriaPersonas categoriaPersona = optionalCategoriaPersona.get();
        categoriaPersona.setCategoria(categoriaPersonaDetails.getCategoria());
        return ResponseEntity.ok(categoriaPersonasRepository.save(categoriaPersona));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategoriaPersona(@PathVariable String id) {
        if (!categoriaPersonasRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        categoriaPersonasRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> partialUpdateCategoriaPersona(@PathVariable String id,
            @RequestBody Map<String, Object> updates) {
        // 1. Buscar la entidad existente
        Optional<CategoriaPersonas> categoriaPersonaOptional = categoriaPersonasRepository.findById(id);
        if (categoriaPersonaOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        CategoriaPersonas categoriaPersonaExistente = categoriaPersonaOptional.get();

        // 2. Actualizar campos simples
        if (updates.containsKey("categoria")) {
            categoriaPersonaExistente.setCategoria((String) updates.get("categoria"));
        }

        // 3. Guardar y devolver
        CategoriaPersonas categoriaPersonaActualizada = categoriaPersonasRepository.save(categoriaPersonaExistente);
        return ResponseEntity.ok(categoriaPersonaActualizada);
    }
}
