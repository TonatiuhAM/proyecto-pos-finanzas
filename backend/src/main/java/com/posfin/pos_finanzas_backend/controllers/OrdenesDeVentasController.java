package com.posfin.pos_finanzas_backend.controllers;

import com.posfin.pos_finanzas_backend.models.OrdenesDeVentas;
import com.posfin.pos_finanzas_backend.models.Personas;
import com.posfin.pos_finanzas_backend.models.Usuarios;
import com.posfin.pos_finanzas_backend.models.MetodosPago;
import com.posfin.pos_finanzas_backend.dtos.OrdenesDeVentasDTO;
import com.posfin.pos_finanzas_backend.repositories.OrdenesDeVentasRepository;
import com.posfin.pos_finanzas_backend.repositories.PersonasRepository;
import com.posfin.pos_finanzas_backend.repositories.UsuariosRepository;
import com.posfin.pos_finanzas_backend.repositories.MetodosPagoRepository;
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
@RequestMapping("/api/ordenes-de-ventas")
public class OrdenesDeVentasController {

    @Autowired
    private OrdenesDeVentasRepository ordenesDeVentasRepository;

    @Autowired
    private PersonasRepository personasRepository;

    @Autowired
    private UsuariosRepository usuariosRepository;

    @Autowired
    private MetodosPagoRepository metodosPagoRepository;

    @GetMapping
    public List<OrdenesDeVentasDTO> getAllOrdenesDeVentas() {
        List<OrdenesDeVentas> ordenes = ordenesDeVentasRepository.findAll();
        List<OrdenesDeVentasDTO> ordenesDTO = new ArrayList<>();

        for (OrdenesDeVentas orden : ordenes) {
            OrdenesDeVentasDTO dto = new OrdenesDeVentasDTO();
            dto.setId(orden.getId());
            dto.setFechaOrden(orden.getFechaOrden());
            dto.setTotalVenta(orden.getTotalVenta());

            // Mapear relaciones aplanadas
            dto.setPersonaId(orden.getPersona().getId());
            dto.setPersonaNombre(orden.getPersona().getNombre());
            dto.setPersonaApellidoPaterno(orden.getPersona().getApellidoPaterno());
            dto.setPersonaApellidoMaterno(orden.getPersona().getApellidoMaterno());
            dto.setPersonaTelefono(orden.getPersona().getTelefono());
            dto.setPersonaEmail(orden.getPersona().getEmail());

            dto.setUsuarioId(orden.getUsuario().getId());
            dto.setUsuarioNombre(orden.getUsuario().getNombre());
            dto.setUsuarioTelefono(orden.getUsuario().getTelefono());

            dto.setMetodoPagoId(orden.getMetodoPago().getId());
            dto.setMetodoPagoNombre(orden.getMetodoPago().getMetodoPago());

            ordenesDTO.add(dto);
        }

        return ordenesDTO;
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdenesDeVentasDTO> getOrdenDeVentaById(@PathVariable String id) {
        Optional<OrdenesDeVentas> optionalOrden = ordenesDeVentasRepository.findById(id);

        if (!optionalOrden.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        OrdenesDeVentas orden = optionalOrden.get();
        OrdenesDeVentasDTO dto = new OrdenesDeVentasDTO();
        dto.setId(orden.getId());
        dto.setFechaOrden(orden.getFechaOrden());
        dto.setTotalVenta(orden.getTotalVenta());

        // Mapear relaciones aplanadas
        dto.setPersonaId(orden.getPersona().getId());
        dto.setPersonaNombre(orden.getPersona().getNombre());
        dto.setPersonaApellidoPaterno(orden.getPersona().getApellidoPaterno());
        dto.setPersonaApellidoMaterno(orden.getPersona().getApellidoMaterno());
        dto.setPersonaTelefono(orden.getPersona().getTelefono());
        dto.setPersonaEmail(orden.getPersona().getEmail());

        dto.setUsuarioId(orden.getUsuario().getId());
        dto.setUsuarioNombre(orden.getUsuario().getNombre());
        dto.setUsuarioTelefono(orden.getUsuario().getTelefono());

        dto.setMetodoPagoId(orden.getMetodoPago().getId());
        dto.setMetodoPagoNombre(orden.getMetodoPago().getMetodoPago());

        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<OrdenesDeVentasDTO> createOrdenDeVenta(@RequestBody Map<String, Object> requestBody) {
        try {
            // Obtener datos del request
            String personaId = (String) requestBody.get("personaId");
            String usuarioId = (String) requestBody.get("usuarioId");
            String metodoPagoId = (String) requestBody.get("metodoPagoId");
            BigDecimal totalVenta = new BigDecimal(requestBody.get("totalVenta").toString());
            OffsetDateTime fechaOrden = OffsetDateTime.now(); // Se establece la fecha actual

            // Buscar las entidades relacionadas
            Optional<Personas> persona = personasRepository.findById(personaId);
            Optional<Usuarios> usuario = usuariosRepository.findById(usuarioId);
            Optional<MetodosPago> metodoPago = metodosPagoRepository.findById(metodoPagoId);

            if (!persona.isPresent() || !usuario.isPresent() || !metodoPago.isPresent()) {
                return ResponseEntity.badRequest().build();
            }

            // Crear la orden
            OrdenesDeVentas orden = new OrdenesDeVentas();
            orden.setPersona(persona.get());
            orden.setUsuario(usuario.get());
            orden.setMetodoPago(metodoPago.get());
            orden.setTotalVenta(totalVenta);
            orden.setFechaOrden(fechaOrden);

            // Guardar
            OrdenesDeVentas savedOrden = ordenesDeVentasRepository.save(orden);

            // Crear DTO para respuesta
            OrdenesDeVentasDTO dto = new OrdenesDeVentasDTO();
            dto.setId(savedOrden.getId());
            dto.setFechaOrden(savedOrden.getFechaOrden());
            dto.setTotalVenta(savedOrden.getTotalVenta());
            dto.setPersonaId(savedOrden.getPersona().getId());
            dto.setPersonaNombre(savedOrden.getPersona().getNombre());
            dto.setPersonaApellidoPaterno(savedOrden.getPersona().getApellidoPaterno());
            dto.setPersonaApellidoMaterno(savedOrden.getPersona().getApellidoMaterno());
            dto.setPersonaTelefono(savedOrden.getPersona().getTelefono());
            dto.setPersonaEmail(savedOrden.getPersona().getEmail());
            dto.setUsuarioId(savedOrden.getUsuario().getId());
            dto.setUsuarioNombre(savedOrden.getUsuario().getNombre());
            dto.setUsuarioTelefono(savedOrden.getUsuario().getTelefono());
            dto.setMetodoPagoId(savedOrden.getMetodoPago().getId());
            dto.setMetodoPagoNombre(savedOrden.getMetodoPago().getMetodoPago());

            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrdenesDeVentasDTO> updateOrdenDeVenta(@PathVariable String id,
            @RequestBody Map<String, Object> requestBody) {
        try {
            Optional<OrdenesDeVentas> optionalOrden = ordenesDeVentasRepository.findById(id);
            if (!optionalOrden.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            OrdenesDeVentas orden = optionalOrden.get();

            // Obtener datos del request
            String personaId = (String) requestBody.get("personaId");
            String usuarioId = (String) requestBody.get("usuarioId");
            String metodoPagoId = (String) requestBody.get("metodoPagoId");
            BigDecimal totalVenta = new BigDecimal(requestBody.get("totalVenta").toString());

            // Buscar las entidades relacionadas
            Optional<Personas> persona = personasRepository.findById(personaId);
            Optional<Usuarios> usuario = usuariosRepository.findById(usuarioId);
            Optional<MetodosPago> metodoPago = metodosPagoRepository.findById(metodoPagoId);

            if (!persona.isPresent() || !usuario.isPresent() || !metodoPago.isPresent()) {
                return ResponseEntity.badRequest().build();
            }

            // Actualizar la orden
            orden.setPersona(persona.get());
            orden.setUsuario(usuario.get());
            orden.setMetodoPago(metodoPago.get());
            orden.setTotalVenta(totalVenta);
            // Nota: fechaOrden no se actualiza para mantener la fecha original

            // Guardar
            OrdenesDeVentas savedOrden = ordenesDeVentasRepository.save(orden);

            // Crear DTO para respuesta
            OrdenesDeVentasDTO dto = new OrdenesDeVentasDTO();
            dto.setId(savedOrden.getId());
            dto.setFechaOrden(savedOrden.getFechaOrden());
            dto.setTotalVenta(savedOrden.getTotalVenta());
            dto.setPersonaId(savedOrden.getPersona().getId());
            dto.setPersonaNombre(savedOrden.getPersona().getNombre());
            dto.setPersonaApellidoPaterno(savedOrden.getPersona().getApellidoPaterno());
            dto.setPersonaApellidoMaterno(savedOrden.getPersona().getApellidoMaterno());
            dto.setPersonaTelefono(savedOrden.getPersona().getTelefono());
            dto.setPersonaEmail(savedOrden.getPersona().getEmail());
            dto.setUsuarioId(savedOrden.getUsuario().getId());
            dto.setUsuarioNombre(savedOrden.getUsuario().getNombre());
            dto.setUsuarioTelefono(savedOrden.getUsuario().getTelefono());
            dto.setMetodoPagoId(savedOrden.getMetodoPago().getId());
            dto.setMetodoPagoNombre(savedOrden.getMetodoPago().getMetodoPago());

            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrdenDeVenta(@PathVariable String id) {
        if (!ordenesDeVentasRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        ordenesDeVentasRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
