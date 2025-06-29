package com.posfin.pos_finanzas_backend.controllers;

import com.posfin.pos_finanzas_backend.models.Personas;
import com.posfin.pos_finanzas_backend.models.Estados;
import com.posfin.pos_finanzas_backend.models.CategoriaPersonas;
import com.posfin.pos_finanzas_backend.dtos.PersonasDTO;
import com.posfin.pos_finanzas_backend.repositories.PersonasRepository;
import com.posfin.pos_finanzas_backend.repositories.EstadosRepository;
import com.posfin.pos_finanzas_backend.repositories.CategoriaPersonasRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Map;

@RestController
@RequestMapping("/api/personas")
public class PersonasController {

    @Autowired
    private PersonasRepository personasRepository;

    @Autowired
    private EstadosRepository estadosRepository;

    @Autowired
    private CategoriaPersonasRepository categoriaPersonasRepository;

    @GetMapping
    public List<PersonasDTO> getAllPersonas() {
        return personasRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PersonasDTO> getPersonaById(@PathVariable String id) {
        Optional<Personas> optionalPersona = personasRepository.findById(id);
        return optionalPersona.map(persona -> ResponseEntity.ok(convertToDTO(persona)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PersonasDTO> createPersona(@RequestBody Map<String, Object> requestBody) {
        try {
            // Obtener datos del request
            String nombre = (String) requestBody.get("nombre");
            String apellidoPaterno = (String) requestBody.get("apellidoPaterno");
            String apellidoMaterno = (String) requestBody.get("apellidoMaterno");
            String rfc = (String) requestBody.get("rfc");
            String telefono = (String) requestBody.get("telefono");
            String email = (String) requestBody.get("email");
            String estadosId = (String) requestBody.get("estadosId");
            String categoriaPersonasId = (String) requestBody.get("categoriaPersonasId");

            // Buscar las entidades relacionadas
            Optional<Estados> estado = estadosRepository.findById(estadosId);
            Optional<CategoriaPersonas> categoria = categoriaPersonasRepository.findById(categoriaPersonasId);

            if (!estado.isPresent() || !categoria.isPresent()) {
                return ResponseEntity.badRequest().build();
            }

            // Crear la persona
            Personas persona = new Personas();
            persona.setNombre(nombre);
            persona.setApellidoPaterno(apellidoPaterno);
            persona.setApellidoMaterno(apellidoMaterno);
            persona.setRfc(rfc);
            persona.setTelefono(telefono);
            persona.setEmail(email);
            persona.setEstados(estado.get());
            persona.setCategoriaPersonas(categoria.get());

            // Guardar
            Personas savedPersona = personasRepository.save(persona);

            // Crear DTO para respuesta
            PersonasDTO dto = new PersonasDTO();
            dto.setId(savedPersona.getId());
            dto.setNombre(savedPersona.getNombre());
            dto.setApellidoPaterno(savedPersona.getApellidoPaterno());
            dto.setApellidoMaterno(savedPersona.getApellidoMaterno());
            dto.setRfc(savedPersona.getRfc());
            dto.setTelefono(savedPersona.getTelefono());
            dto.setEmail(savedPersona.getEmail());
            dto.setEstadosId(savedPersona.getEstados().getId());
            dto.setEstadosEstado(savedPersona.getEstados().getEstado());
            dto.setCategoriaPersonasId(savedPersona.getCategoriaPersonas().getId());
            dto.setCategoriaPersonasCategoria(savedPersona.getCategoriaPersonas().getCategoria());

            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<PersonasDTO> updatePersona(@PathVariable String id,
            @RequestBody Map<String, Object> requestBody) {
        try {
            Optional<Personas> optionalPersona = personasRepository.findById(id);
            if (!optionalPersona.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Personas persona = optionalPersona.get();

            // Obtener datos del request
            String nombre = (String) requestBody.get("nombre");
            String apellidoPaterno = (String) requestBody.get("apellidoPaterno");
            String apellidoMaterno = (String) requestBody.get("apellidoMaterno");
            String rfc = (String) requestBody.get("rfc");
            String telefono = (String) requestBody.get("telefono");
            String email = (String) requestBody.get("email");
            String estadosId = (String) requestBody.get("estadosId");
            String categoriaPersonasId = (String) requestBody.get("categoriaPersonasId");

            // Buscar las entidades relacionadas
            Optional<Estados> estado = estadosRepository.findById(estadosId);
            Optional<CategoriaPersonas> categoria = categoriaPersonasRepository.findById(categoriaPersonasId);

            if (!estado.isPresent() || !categoria.isPresent()) {
                return ResponseEntity.badRequest().build();
            }

            // Actualizar la persona
            persona.setNombre(nombre);
            persona.setApellidoPaterno(apellidoPaterno);
            persona.setApellidoMaterno(apellidoMaterno);
            persona.setRfc(rfc);
            persona.setTelefono(telefono);
            persona.setEmail(email);
            persona.setEstados(estado.get());
            persona.setCategoriaPersonas(categoria.get());

            // Guardar
            Personas savedPersona = personasRepository.save(persona);

            // Crear DTO para respuesta
            PersonasDTO dto = new PersonasDTO();
            dto.setId(savedPersona.getId());
            dto.setNombre(savedPersona.getNombre());
            dto.setApellidoPaterno(savedPersona.getApellidoPaterno());
            dto.setApellidoMaterno(savedPersona.getApellidoMaterno());
            dto.setRfc(savedPersona.getRfc());
            dto.setTelefono(savedPersona.getTelefono());
            dto.setEmail(savedPersona.getEmail());
            dto.setEstadosId(savedPersona.getEstados().getId());
            dto.setEstadosEstado(savedPersona.getEstados().getEstado());
            dto.setCategoriaPersonasId(savedPersona.getCategoriaPersonas().getId());
            dto.setCategoriaPersonasCategoria(savedPersona.getCategoriaPersonas().getCategoria());

            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePersona(@PathVariable String id) {
        if (!personasRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        personasRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> partialUpdatePersona(@PathVariable String id, @RequestBody Map<String, Object> updates) {
        // 1. Buscar la entidad existente
        Optional<Personas> personaOptional = personasRepository.findById(id);
        if (personaOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Personas personaExistente = personaOptional.get();

        // 2. Actualizar campos simples
        if (updates.containsKey("nombre")) {
            personaExistente.setNombre((String) updates.get("nombre"));
        }
        if (updates.containsKey("apellidoPaterno")) {
            personaExistente.setApellidoPaterno((String) updates.get("apellidoPaterno"));
        }
        if (updates.containsKey("apellidoMaterno")) {
            personaExistente.setApellidoMaterno((String) updates.get("apellidoMaterno"));
        }
        if (updates.containsKey("rfc")) {
            personaExistente.setRfc((String) updates.get("rfc"));
        }
        if (updates.containsKey("telefono")) {
            personaExistente.setTelefono((String) updates.get("telefono"));
        }
        if (updates.containsKey("email")) {
            personaExistente.setEmail((String) updates.get("email"));
        }

        // 3. Lógica para actualizar la relación con Estados
        if (updates.containsKey("estados")) {
            Map<String, String> estadosMap = (Map<String, String>) updates.get("estados");
            String estadosId = estadosMap.get("id");
            Optional<Estados> estadosOpt = estadosRepository.findById(estadosId);
            if (estadosOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Error: El Estado con ID " + estadosId + " no existe.");
            }
            personaExistente.setEstados(estadosOpt.get());
        }

        // 4. Lógica para actualizar la relación con CategoriaPersonas
        if (updates.containsKey("categoriaPersonas")) {
            Map<String, String> categoriaPersonasMap = (Map<String, String>) updates.get("categoriaPersonas");
            String categoriaPersonasId = categoriaPersonasMap.get("id");
            Optional<CategoriaPersonas> categoriaPersonasOpt = categoriaPersonasRepository
                    .findById(categoriaPersonasId);
            if (categoriaPersonasOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Error: La Categoría de Persona con ID " + categoriaPersonasId + " no existe.");
            }
            personaExistente.setCategoriaPersonas(categoriaPersonasOpt.get());
        }

        // 5. Guardar y devolver
        Personas personaActualizada = personasRepository.save(personaExistente);
        PersonasDTO dto = convertToDTO(personaActualizada);
        return ResponseEntity.ok(dto);
    }

    // Método auxiliar para convertir Personas a PersonasDTO
    private PersonasDTO convertToDTO(Personas persona) {
        PersonasDTO dto = new PersonasDTO();
        dto.setId(persona.getId());
        dto.setNombre(persona.getNombre());
        dto.setApellidoPaterno(persona.getApellidoPaterno());
        dto.setApellidoMaterno(persona.getApellidoMaterno());
        dto.setRfc(persona.getRfc());
        dto.setTelefono(persona.getTelefono());
        dto.setEmail(persona.getEmail());

        // Mapear relación con Estados
        if (persona.getEstados() != null) {
            dto.setEstadosId(persona.getEstados().getId());
            dto.setEstadosEstado(persona.getEstados().getEstado());
        }

        // Mapear relación con CategoriaPersonas
        if (persona.getCategoriaPersonas() != null) {
            dto.setCategoriaPersonasId(persona.getCategoriaPersonas().getId());
            dto.setCategoriaPersonasCategoria(persona.getCategoriaPersonas().getCategoria());
        }

        return dto;
    }
}
