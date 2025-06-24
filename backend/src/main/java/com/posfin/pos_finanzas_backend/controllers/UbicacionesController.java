package com.posfin.pos_finanzas_backend.controllers;

import com.posfin.pos_finanzas_backend.models.Ubicaciones;
import com.posfin.pos_finanzas_backend.repositories.UbicacionesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/ubicaciones")
public class UbicacionesController {
    @Autowired
    private UbicacionesRepository ubicacionesRepository;

    @GetMapping
    public List<Ubicaciones> getAllUbicaciones() {
        return ubicacionesRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ubicaciones> getUbicacionById(@PathVariable String id) {
        Optional<Ubicaciones> ubicacion = ubicacionesRepository.findById(id);
        return ubicacion.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Ubicaciones createUbicacion(@RequestBody Ubicaciones ubicacion) {
        return ubicacionesRepository.save(ubicacion);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ubicaciones> updateUbicacion(@PathVariable String id,
            @RequestBody Ubicaciones ubicacionDetails) {
        Optional<Ubicaciones> optionalUbicacion = ubicacionesRepository.findById(id);
        if (!optionalUbicacion.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        Ubicaciones ubicacion = optionalUbicacion.get();
        ubicacion.setNombre(ubicacionDetails.getNombre());
        ubicacion.setUbicacion(ubicacionDetails.getUbicacion());
        return ResponseEntity.ok(ubicacionesRepository.save(ubicacion));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUbicacion(@PathVariable String id) {
        if (!ubicacionesRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        ubicacionesRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
