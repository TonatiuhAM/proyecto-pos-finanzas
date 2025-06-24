package com.posfin.pos_finanzas_backend.controllers;

import com.posfin.pos_finanzas_backend.models.MetodosPago;
import com.posfin.pos_finanzas_backend.repositories.MetodosPagoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/metodos_pago")
public class MetodosPagoController {
    @Autowired
    private MetodosPagoRepository metodosPagoRepository;

    @GetMapping
    public List<MetodosPago> getAllMetodosPago() {
        return metodosPagoRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MetodosPago> getMetodoPagoById(@PathVariable String id) {
        Optional<MetodosPago> metodoPago = metodosPagoRepository.findById(id);
        return metodoPago.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public MetodosPago createMetodoPago(@RequestBody MetodosPago metodoPago) {
        return metodosPagoRepository.save(metodoPago);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MetodosPago> updateMetodoPago(@PathVariable String id,
            @RequestBody MetodosPago metodoPagoDetails) {
        Optional<MetodosPago> optionalMetodoPago = metodosPagoRepository.findById(id);
        if (!optionalMetodoPago.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        MetodosPago metodoPago = optionalMetodoPago.get();
        metodoPago.setMetodoPago(metodoPagoDetails.getMetodoPago());
        return ResponseEntity.ok(metodosPagoRepository.save(metodoPago));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMetodoPago(@PathVariable String id) {
        if (!metodosPagoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        metodosPagoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
