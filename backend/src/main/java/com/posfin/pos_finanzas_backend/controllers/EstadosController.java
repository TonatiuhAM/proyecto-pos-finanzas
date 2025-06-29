package com.posfin.pos_finanzas_backend.controllers;

import com.posfin.pos_finanzas_backend.models.Estados;
import com.posfin.pos_finanzas_backend.repositories.EstadosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/estados")
public class EstadosController {
    @Autowired
    private EstadosRepository estadosRepository;

    @GetMapping
    public List<Estados> getAllEstados() {
        return estadosRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Estados> getEstadoById(@PathVariable String id) {
        Optional<Estados> estado = estadosRepository.findById(id);
        return estado.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Estados createEstado(@RequestBody Estados estado) {
        return estadosRepository.save(estado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Estados> updateEstado(@PathVariable String id, @RequestBody Estados estadoDetails) {
        Optional<Estados> optionalEstado = estadosRepository.findById(id);
        if (!optionalEstado.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        Estados estado = optionalEstado.get();
        estado.setEstado(estadoDetails.getEstado());
        return ResponseEntity.ok(estadosRepository.save(estado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEstado(@PathVariable String id) {
        if (!estadosRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        estadosRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> partialUpdateEstado(@PathVariable String id, @RequestBody Map<String, Object> updates) {
        // 1. Buscar la entidad existente
        Optional<Estados> estadoOptional = estadosRepository.findById(id);
        if (estadoOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Estados estadoExistente = estadoOptional.get();

        // 2. Actualizar campos simples
        if (updates.containsKey("estado")) {
            estadoExistente.setEstado((String) updates.get("estado"));
        }

        // 3. Guardar y devolver
        Estados estadoActualizado = estadosRepository.save(estadoExistente);
        return ResponseEntity.ok(estadoActualizado);
    }
}
