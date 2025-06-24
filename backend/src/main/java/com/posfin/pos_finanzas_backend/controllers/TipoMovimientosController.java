package com.posfin.pos_finanzas_backend.controllers;

import com.posfin.pos_finanzas_backend.models.TipoMovimientos;
import com.posfin.pos_finanzas_backend.repositories.TIpoMovimientosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tipo-movimientos")
public class TipoMovimientosController {
    @Autowired
    private TIpoMovimientosRepository tipoMovimientosRepository;

    @GetMapping
    public List<TipoMovimientos> getAllTipoMovimientos() {
        return tipoMovimientosRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TipoMovimientos> getTipoMovimientoById(@PathVariable String id) {
        Optional<TipoMovimientos> tipoMovimiento = tipoMovimientosRepository.findById(id);
        return tipoMovimiento.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public TipoMovimientos createTipoMovimiento(@RequestBody TipoMovimientos tipoMovimiento) {
        return tipoMovimientosRepository.save(tipoMovimiento);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TipoMovimientos> updateTipoMovimiento(@PathVariable String id,
            @RequestBody TipoMovimientos tipoMovimientoDetails) {
        Optional<TipoMovimientos> optionalTipoMovimiento = tipoMovimientosRepository.findById(id);
        if (!optionalTipoMovimiento.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        TipoMovimientos tipoMovimiento = optionalTipoMovimiento.get();
        tipoMovimiento.setMovimiento(tipoMovimientoDetails.getMovimiento());
        return ResponseEntity.ok(tipoMovimientosRepository.save(tipoMovimiento));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTipoMovimiento(@PathVariable String id) {
        if (!tipoMovimientosRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        tipoMovimientosRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
