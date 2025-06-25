package com.posfin.pos_finanzas_backend.controllers;

import com.posfin.pos_finanzas_backend.models.HistorialCargosProveedores;
import com.posfin.pos_finanzas_backend.models.Personas;
import com.posfin.pos_finanzas_backend.models.OrdenesDeCompras;
import com.posfin.pos_finanzas_backend.dtos.HistorialCargosProveedoresDTO;
import com.posfin.pos_finanzas_backend.repositories.HistorialCargosProveedoresRepository;
import com.posfin.pos_finanzas_backend.repositories.PersonasRepository;
import com.posfin.pos_finanzas_backend.repositories.OrdenesDeComprasRepository;
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
@RequestMapping("/api/historial-cargos-proveedores")
public class HistorialCargosProveedoresController {

    @Autowired
    private HistorialCargosProveedoresRepository historialCargosProveedoresRepository;

    @Autowired
    private PersonasRepository personasRepository;

    @Autowired
    private OrdenesDeComprasRepository ordenesDeComprasRepository;

    @GetMapping
    public List<HistorialCargosProveedoresDTO> getAllHistorialCargosProveedores() {
        List<HistorialCargosProveedores> historialList = historialCargosProveedoresRepository.findAll();
        List<HistorialCargosProveedoresDTO> historialDTO = new ArrayList<>();

        for (HistorialCargosProveedores historial : historialList) {
            HistorialCargosProveedoresDTO dto = new HistorialCargosProveedoresDTO();
            dto.setId(historial.getId());
            dto.setMontoPagado(historial.getMontoPagado());
            dto.setFecha(historial.getFecha());

            // Mapear relaciones aplanadas
            dto.setPersonaId(historial.getPersona().getId());
            dto.setPersonaNombre(historial.getPersona().getNombre());
            dto.setPersonaApellidoPaterno(historial.getPersona().getApellidoPaterno());
            dto.setPersonaApellidoMaterno(historial.getPersona().getApellidoMaterno());
            dto.setPersonaTelefono(historial.getPersona().getTelefono());
            dto.setPersonaEmail(historial.getPersona().getEmail());

            dto.setOrdenDeCompraId(historial.getOrdenDeCompra().getId());
            dto.setOrdenDeCompraTotalCompra(historial.getOrdenDeCompra().getTotalCompra());
            dto.setOrdenDeCompraFechaOrden(historial.getOrdenDeCompra().getFechaOrden());

            historialDTO.add(dto);
        }

        return historialDTO;
    }

    @GetMapping("/{id}")
    public ResponseEntity<HistorialCargosProveedoresDTO> getHistorialCargoProveedorById(@PathVariable String id) {
        Optional<HistorialCargosProveedores> optionalHistorial = historialCargosProveedoresRepository.findById(id);

        if (!optionalHistorial.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        HistorialCargosProveedores historial = optionalHistorial.get();
        HistorialCargosProveedoresDTO dto = new HistorialCargosProveedoresDTO();
        dto.setId(historial.getId());
        dto.setMontoPagado(historial.getMontoPagado());
        dto.setFecha(historial.getFecha());

        // Mapear relaciones aplanadas
        dto.setPersonaId(historial.getPersona().getId());
        dto.setPersonaNombre(historial.getPersona().getNombre());
        dto.setPersonaApellidoPaterno(historial.getPersona().getApellidoPaterno());
        dto.setPersonaApellidoMaterno(historial.getPersona().getApellidoMaterno());
        dto.setPersonaTelefono(historial.getPersona().getTelefono());
        dto.setPersonaEmail(historial.getPersona().getEmail());

        dto.setOrdenDeCompraId(historial.getOrdenDeCompra().getId());
        dto.setOrdenDeCompraTotalCompra(historial.getOrdenDeCompra().getTotalCompra());
        dto.setOrdenDeCompraFechaOrden(historial.getOrdenDeCompra().getFechaOrden());

        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<HistorialCargosProveedoresDTO> createHistorialCargoProveedor(
            @RequestBody Map<String, Object> requestBody) {
        try {
            // Obtener datos del request
            String personaId = (String) requestBody.get("personaId");
            String ordenDeCompraId = (String) requestBody.get("ordenDeCompraId");
            BigDecimal montoPagado = new BigDecimal(requestBody.get("montoPagado").toString());
            OffsetDateTime fecha = OffsetDateTime.now(); // Se establece la fecha actual

            // Buscar las entidades relacionadas
            Optional<Personas> persona = personasRepository.findById(personaId);
            Optional<OrdenesDeCompras> ordenDeCompra = ordenesDeComprasRepository.findById(ordenDeCompraId);

            if (!persona.isPresent() || !ordenDeCompra.isPresent()) {
                return ResponseEntity.badRequest().build();
            }

            // Crear el historial
            HistorialCargosProveedores historial = new HistorialCargosProveedores();
            historial.setPersona(persona.get());
            historial.setOrdenDeCompra(ordenDeCompra.get());
            historial.setMontoPagado(montoPagado);
            historial.setFecha(fecha);

            // Guardar
            HistorialCargosProveedores savedHistorial = historialCargosProveedoresRepository.save(historial);

            // Crear DTO para respuesta
            HistorialCargosProveedoresDTO dto = new HistorialCargosProveedoresDTO();
            dto.setId(savedHistorial.getId());
            dto.setMontoPagado(savedHistorial.getMontoPagado());
            dto.setFecha(savedHistorial.getFecha());
            dto.setPersonaId(savedHistorial.getPersona().getId());
            dto.setPersonaNombre(savedHistorial.getPersona().getNombre());
            dto.setPersonaApellidoPaterno(savedHistorial.getPersona().getApellidoPaterno());
            dto.setPersonaApellidoMaterno(savedHistorial.getPersona().getApellidoMaterno());
            dto.setPersonaTelefono(savedHistorial.getPersona().getTelefono());
            dto.setPersonaEmail(savedHistorial.getPersona().getEmail());
            dto.setOrdenDeCompraId(savedHistorial.getOrdenDeCompra().getId());
            dto.setOrdenDeCompraTotalCompra(savedHistorial.getOrdenDeCompra().getTotalCompra());
            dto.setOrdenDeCompraFechaOrden(savedHistorial.getOrdenDeCompra().getFechaOrden());

            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<HistorialCargosProveedoresDTO> updateHistorialCargoProveedor(@PathVariable String id,
            @RequestBody Map<String, Object> requestBody) {
        try {
            Optional<HistorialCargosProveedores> optionalHistorial = historialCargosProveedoresRepository.findById(id);
            if (!optionalHistorial.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            HistorialCargosProveedores historial = optionalHistorial.get();

            // Obtener datos del request
            String personaId = (String) requestBody.get("personaId");
            String ordenDeCompraId = (String) requestBody.get("ordenDeCompraId");
            BigDecimal montoPagado = new BigDecimal(requestBody.get("montoPagado").toString());

            // Buscar las entidades relacionadas
            Optional<Personas> persona = personasRepository.findById(personaId);
            Optional<OrdenesDeCompras> ordenDeCompra = ordenesDeComprasRepository.findById(ordenDeCompraId);

            if (!persona.isPresent() || !ordenDeCompra.isPresent()) {
                return ResponseEntity.badRequest().build();
            }

            // Actualizar el historial
            historial.setPersona(persona.get());
            historial.setOrdenDeCompra(ordenDeCompra.get());
            historial.setMontoPagado(montoPagado);
            // Nota: fecha no se actualiza para mantener la fecha original

            // Guardar
            HistorialCargosProveedores savedHistorial = historialCargosProveedoresRepository.save(historial);

            // Crear DTO para respuesta
            HistorialCargosProveedoresDTO dto = new HistorialCargosProveedoresDTO();
            dto.setId(savedHistorial.getId());
            dto.setMontoPagado(savedHistorial.getMontoPagado());
            dto.setFecha(savedHistorial.getFecha());
            dto.setPersonaId(savedHistorial.getPersona().getId());
            dto.setPersonaNombre(savedHistorial.getPersona().getNombre());
            dto.setPersonaApellidoPaterno(savedHistorial.getPersona().getApellidoPaterno());
            dto.setPersonaApellidoMaterno(savedHistorial.getPersona().getApellidoMaterno());
            dto.setPersonaTelefono(savedHistorial.getPersona().getTelefono());
            dto.setPersonaEmail(savedHistorial.getPersona().getEmail());
            dto.setOrdenDeCompraId(savedHistorial.getOrdenDeCompra().getId());
            dto.setOrdenDeCompraTotalCompra(savedHistorial.getOrdenDeCompra().getTotalCompra());
            dto.setOrdenDeCompraFechaOrden(savedHistorial.getOrdenDeCompra().getFechaOrden());

            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHistorialCargoProveedor(@PathVariable String id) {
        if (!historialCargosProveedoresRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        historialCargosProveedoresRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
