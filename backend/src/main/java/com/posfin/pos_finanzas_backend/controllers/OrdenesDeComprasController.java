package com.posfin.pos_finanzas_backend.controllers;

import com.posfin.pos_finanzas_backend.models.OrdenesDeCompras;
import com.posfin.pos_finanzas_backend.models.Personas;
import com.posfin.pos_finanzas_backend.models.Estados;
import com.posfin.pos_finanzas_backend.dtos.OrdenesDeComprasDTO;
import com.posfin.pos_finanzas_backend.repositories.OrdenesDeComprasRepository;
import com.posfin.pos_finanzas_backend.repositories.PersonasRepository;
import com.posfin.pos_finanzas_backend.repositories.EstadosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Map;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/ordenes-de-compras")
public class OrdenesDeComprasController {

    @Autowired
    private OrdenesDeComprasRepository ordenesDeComprasRepository;

    @Autowired
    private PersonasRepository personasRepository;

    @Autowired
    private EstadosRepository estadosRepository;

    @GetMapping
    public List<OrdenesDeComprasDTO> getAllOrdenesDeCompras() {
        List<OrdenesDeCompras> ordenes = ordenesDeComprasRepository.findAll();
        List<OrdenesDeComprasDTO> ordenesDTO = new ArrayList<>();

        for (OrdenesDeCompras orden : ordenes) {
            OrdenesDeComprasDTO dto = new OrdenesDeComprasDTO();
            dto.setId(orden.getId());
            dto.setFechaOrden(orden.getFechaOrden());
            dto.setTotalCompra(orden.getTotalCompra());

            // Mapear relaciones aplanadas
            dto.setPersonaId(orden.getPersona().getId());
            dto.setPersonaNombre(orden.getPersona().getNombre());
            dto.setPersonaApellidoPaterno(orden.getPersona().getApellidoPaterno());
            dto.setPersonaApellidoMaterno(orden.getPersona().getApellidoMaterno());
            dto.setPersonaTelefono(orden.getPersona().getTelefono());
            dto.setPersonaEmail(orden.getPersona().getEmail());

            dto.setEstadoId(orden.getEstado().getId());
            dto.setEstadoNombre(orden.getEstado().getEstado());

            ordenesDTO.add(dto);
        }

        return ordenesDTO;
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdenesDeComprasDTO> getOrdenDeCompraById(@PathVariable String id) {
        Optional<OrdenesDeCompras> optionalOrden = ordenesDeComprasRepository.findById(id);

        if (!optionalOrden.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        OrdenesDeCompras orden = optionalOrden.get();
        OrdenesDeComprasDTO dto = new OrdenesDeComprasDTO();
        dto.setId(orden.getId());
        dto.setFechaOrden(orden.getFechaOrden());
        dto.setTotalCompra(orden.getTotalCompra());

        // Mapear relaciones aplanadas
        dto.setPersonaId(orden.getPersona().getId());
        dto.setPersonaNombre(orden.getPersona().getNombre());
        dto.setPersonaApellidoPaterno(orden.getPersona().getApellidoPaterno());
        dto.setPersonaApellidoMaterno(orden.getPersona().getApellidoMaterno());
        dto.setPersonaTelefono(orden.getPersona().getTelefono());
        dto.setPersonaEmail(orden.getPersona().getEmail());

        dto.setEstadoId(orden.getEstado().getId());
        dto.setEstadoNombre(orden.getEstado().getEstado());

        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<OrdenesDeComprasDTO> createOrdenDeCompra(@RequestBody Map<String, Object> requestBody) {
        try {
            // Obtener datos del request
            String personaId = (String) requestBody.get("personaId");
            String estadoId = (String) requestBody.get("estadoId");
            BigDecimal totalCompra = new BigDecimal(requestBody.get("totalCompra").toString());
            OffsetDateTime fechaOrden = OffsetDateTime.now(); // Se establece la fecha actual

            // Buscar las entidades relacionadas
            Optional<Personas> persona = personasRepository.findById(personaId);
            Optional<Estados> estado = estadosRepository.findById(estadoId);

            if (!persona.isPresent() || !estado.isPresent()) {
                return ResponseEntity.badRequest().build();
            }

            // Crear la orden
            OrdenesDeCompras orden = new OrdenesDeCompras();
            orden.setPersona(persona.get());
            orden.setEstado(estado.get());
            orden.setTotalCompra(totalCompra);
            orden.setFechaOrden(fechaOrden);

            // Guardar
            OrdenesDeCompras savedOrden = ordenesDeComprasRepository.save(orden);

            // Crear DTO para respuesta
            OrdenesDeComprasDTO dto = new OrdenesDeComprasDTO();
            dto.setId(savedOrden.getId());
            dto.setFechaOrden(savedOrden.getFechaOrden());
            dto.setTotalCompra(savedOrden.getTotalCompra());
            dto.setPersonaId(savedOrden.getPersona().getId());
            dto.setPersonaNombre(savedOrden.getPersona().getNombre());
            dto.setPersonaApellidoPaterno(savedOrden.getPersona().getApellidoPaterno());
            dto.setPersonaApellidoMaterno(savedOrden.getPersona().getApellidoMaterno());
            dto.setPersonaTelefono(savedOrden.getPersona().getTelefono());
            dto.setPersonaEmail(savedOrden.getPersona().getEmail());
            dto.setEstadoId(savedOrden.getEstado().getId());
            dto.setEstadoNombre(savedOrden.getEstado().getEstado());

            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrdenesDeComprasDTO> updateOrdenDeCompra(@PathVariable String id,
            @RequestBody Map<String, Object> requestBody) {
        try {
            Optional<OrdenesDeCompras> optionalOrden = ordenesDeComprasRepository.findById(id);
            if (!optionalOrden.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            OrdenesDeCompras orden = optionalOrden.get();

            // Obtener datos del request
            String personaId = (String) requestBody.get("personaId");
            String estadoId = (String) requestBody.get("estadoId");
            BigDecimal totalCompra = new BigDecimal(requestBody.get("totalCompra").toString());

            // Buscar las entidades relacionadas
            Optional<Personas> persona = personasRepository.findById(personaId);
            Optional<Estados> estado = estadosRepository.findById(estadoId);

            if (!persona.isPresent() || !estado.isPresent()) {
                return ResponseEntity.badRequest().build();
            }

            // Actualizar la orden
            orden.setPersona(persona.get());
            orden.setEstado(estado.get());
            orden.setTotalCompra(totalCompra);
            // Nota: fechaOrden no se actualiza para mantener la fecha original

            // Guardar
            OrdenesDeCompras savedOrden = ordenesDeComprasRepository.save(orden);

            // Crear DTO para respuesta
            OrdenesDeComprasDTO dto = new OrdenesDeComprasDTO();
            dto.setId(savedOrden.getId());
            dto.setFechaOrden(savedOrden.getFechaOrden());
            dto.setTotalCompra(savedOrden.getTotalCompra());
            dto.setPersonaId(savedOrden.getPersona().getId());
            dto.setPersonaNombre(savedOrden.getPersona().getNombre());
            dto.setPersonaApellidoPaterno(savedOrden.getPersona().getApellidoPaterno());
            dto.setPersonaApellidoMaterno(savedOrden.getPersona().getApellidoMaterno());
            dto.setPersonaTelefono(savedOrden.getPersona().getTelefono());
            dto.setPersonaEmail(savedOrden.getPersona().getEmail());
            dto.setEstadoId(savedOrden.getEstado().getId());
            dto.setEstadoNombre(savedOrden.getEstado().getEstado());

            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrdenDeCompra(@PathVariable String id) {
        if (!ordenesDeComprasRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        ordenesDeComprasRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
