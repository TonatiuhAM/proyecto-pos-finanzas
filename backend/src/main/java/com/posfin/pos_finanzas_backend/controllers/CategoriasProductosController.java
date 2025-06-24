package com.posfin.pos_finanzas_backend.controllers;

import com.posfin.pos_finanzas_backend.models.CategoriasProductos;
import com.posfin.pos_finanzas_backend.repositories.CategoriasProductosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/categorias-productos")
public class CategoriasProductosController {
    @Autowired
    private CategoriasProductosRepository categoriasProductosRepository;

    @GetMapping
    public List<CategoriasProductos> getAllCategoriasProductos() {
        return categoriasProductosRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriasProductos> getCategoriaProductoById(@PathVariable String id) {
        Optional<CategoriasProductos> categoriaProducto = categoriasProductosRepository.findById(id);
        return categoriaProducto.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public CategoriasProductos createCategoriaProducto(@RequestBody CategoriasProductos categoriaProducto) {
        return categoriasProductosRepository.save(categoriaProducto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriasProductos> updateCategoriaProducto(@PathVariable String id,
            @RequestBody CategoriasProductos categoriaProductoDetails) {
        Optional<CategoriasProductos> optionalCategoriaProducto = categoriasProductosRepository.findById(id);
        if (!optionalCategoriaProducto.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        CategoriasProductos categoriaProducto = optionalCategoriaProducto.get();
        categoriaProducto.setCategoria(categoriaProductoDetails.getCategoria());
        return ResponseEntity.ok(categoriasProductosRepository.save(categoriaProducto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategoriaProducto(@PathVariable String id) {
        if (!categoriasProductosRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        categoriasProductosRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
