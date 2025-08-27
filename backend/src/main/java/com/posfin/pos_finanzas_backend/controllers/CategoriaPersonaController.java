package com.posfin.pos_finanzas_backend.controllers;

import com.posfin.pos_finanzas_backend.dtos.CategoriaPersonaDTO;
import com.posfin.pos_finanzas_backend.repositories.CategoriaPersonasRepository;
import com.posfin.pos_finanzas_backend.models.CategoriaPersonas;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categorias-personas")
@CrossOrigin(origins = "http://localhost:5173")
public class CategoriaPersonaController {

    @Autowired
    private CategoriaPersonasRepository categoriaPersonasRepository;

    /**
     * Obtener todas las categorías de personas
     */
    @GetMapping
    public ResponseEntity<List<CategoriaPersonaDTO>> obtenerTodasLasCategorias() {
        try {
            List<CategoriaPersonas> categorias = categoriaPersonasRepository.findAll();
            
            List<CategoriaPersonaDTO> categoriasDTO = categorias.stream()
                .map(categoria -> new CategoriaPersonaDTO(
                    categoria.getId(),
                    categoria.getCategoria()
                ))
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(categoriasDTO);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener una categoría por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoriaPersonaDTO> obtenerCategoriaPorId(@PathVariable String id) {
        try {
            return categoriaPersonasRepository.findById(id)
                .map(categoria -> {
                    CategoriaPersonaDTO dto = new CategoriaPersonaDTO(
                        categoria.getId(),
                        categoria.getCategoria()
                    );
                    return ResponseEntity.ok(dto);
                })
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}