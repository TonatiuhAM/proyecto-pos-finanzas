package com.posfin.pos_finanzas_backend.controllers;

import com.posfin.pos_finanzas_backend.models.HistorialPagosClientes;
import com.posfin.pos_finanzas_backend.models.Personas;
import com.posfin.pos_finanzas_backend.models.OrdenesDeVentas;
import com.posfin.pos_finanzas_backend.dtos.HistorialPagosClientesDTO;
import com.posfin.pos_finanzas_backend.repositories.HistorialPagosClientesRepository;
import com.posfin.pos_finanzas_backend.repositories.PersonasRepository;
import com.posfin.pos_finanzas_backend.repositories.OrdenesDeVentasRepository;
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
@RequestMapping("/api/historial-pagos-clientes")
public class HistorialPagosClientesController {

    @Autowired
    private HistorialPagosClientesRepository historialPagosClientesRepository;

    @Autowired
    private PersonasRepository personasRepository;

    @Autowired
    private OrdenesDeVentasRepository ordenesDeVentasRepository;

    @GetMapping
    public List<HistorialPagosClientesDTO> getAllHistorialPagosClientes() {
        List<HistorialPagosClientes> historialList = historialPagosClientesRepository.findAll();
        List<HistorialPagosClientesDTO> historialDTO = new ArrayList<>();

        for (HistorialPagosClientes historial : historialList) {
            HistorialPagosClientesDTO dto = new HistorialPagosClientesDTO();
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

            dto.setOrdenDeVentaId(historial.getOrdenDeVenta().getId());
            dto.setOrdenDeVentaTotalVenta(historial.getOrdenDeVenta().getTotalVenta());
            dto.setOrdenDeVentaFechaOrden(historial.getOrdenDeVenta().getFechaOrden());

            historialDTO.add(dto);
        }

        return historialDTO;
    }

    @GetMapping("/{id}")
    public ResponseEntity<HistorialPagosClientesDTO> getHistorialPagoClienteById(@PathVariable String id) {
        Optional<HistorialPagosClientes> optionalHistorial = historialPagosClientesRepository.findById(id);

        if (!optionalHistorial.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        HistorialPagosClientes historial = optionalHistorial.get();
        HistorialPagosClientesDTO dto = new HistorialPagosClientesDTO();
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

        dto.setOrdenDeVentaId(historial.getOrdenDeVenta().getId());
        dto.setOrdenDeVentaTotalVenta(historial.getOrdenDeVenta().getTotalVenta());
        dto.setOrdenDeVentaFechaOrden(historial.getOrdenDeVenta().getFechaOrden());

        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<HistorialPagosClientesDTO> createHistorialPagoCliente(
            @RequestBody Map<String, Object> requestBody) {
        try {
            // Obtener datos del request
            String personaId = (String) requestBody.get("personaId");
            String ordenDeVentaId = (String) requestBody.get("ordenDeVentaId");
            BigDecimal montoPagado = new BigDecimal(requestBody.get("montoPagado").toString());
            OffsetDateTime fecha = OffsetDateTime.now(); // Se establece la fecha actual

            // Buscar las entidades relacionadas
            Optional<Personas> persona = personasRepository.findById(personaId);
            Optional<OrdenesDeVentas> ordenDeVenta = ordenesDeVentasRepository.findById(ordenDeVentaId);

            if (!persona.isPresent() || !ordenDeVenta.isPresent()) {
                return ResponseEntity.badRequest().build();
            }

            // Crear el historial
            HistorialPagosClientes historial = new HistorialPagosClientes();
            historial.setPersona(persona.get());
            historial.setOrdenDeVenta(ordenDeVenta.get());
            historial.setMontoPagado(montoPagado);
            historial.setFecha(fecha);

            // Guardar
            HistorialPagosClientes savedHistorial = historialPagosClientesRepository.save(historial);

            // Crear DTO para respuesta
            HistorialPagosClientesDTO dto = new HistorialPagosClientesDTO();
            dto.setId(savedHistorial.getId());
            dto.setMontoPagado(savedHistorial.getMontoPagado());
            dto.setFecha(savedHistorial.getFecha());
            dto.setPersonaId(savedHistorial.getPersona().getId());
            dto.setPersonaNombre(savedHistorial.getPersona().getNombre());
            dto.setPersonaApellidoPaterno(savedHistorial.getPersona().getApellidoPaterno());
            dto.setPersonaApellidoMaterno(savedHistorial.getPersona().getApellidoMaterno());
            dto.setPersonaTelefono(savedHistorial.getPersona().getTelefono());
            dto.setPersonaEmail(savedHistorial.getPersona().getEmail());
            dto.setOrdenDeVentaId(savedHistorial.getOrdenDeVenta().getId());
            dto.setOrdenDeVentaTotalVenta(savedHistorial.getOrdenDeVenta().getTotalVenta());
            dto.setOrdenDeVentaFechaOrden(savedHistorial.getOrdenDeVenta().getFechaOrden());

            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<HistorialPagosClientesDTO> updateHistorialPagoCliente(@PathVariable String id,
            @RequestBody Map<String, Object> requestBody) {
        try {
            Optional<HistorialPagosClientes> optionalHistorial = historialPagosClientesRepository.findById(id);
            if (!optionalHistorial.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            HistorialPagosClientes historial = optionalHistorial.get();

            // Obtener datos del request
            String personaId = (String) requestBody.get("personaId");
            String ordenDeVentaId = (String) requestBody.get("ordenDeVentaId");
            BigDecimal montoPagado = new BigDecimal(requestBody.get("montoPagado").toString());

            // Buscar las entidades relacionadas
            Optional<Personas> persona = personasRepository.findById(personaId);
            Optional<OrdenesDeVentas> ordenDeVenta = ordenesDeVentasRepository.findById(ordenDeVentaId);

            if (!persona.isPresent() || !ordenDeVenta.isPresent()) {
                return ResponseEntity.badRequest().build();
            }

            // Actualizar el historial
            historial.setPersona(persona.get());
            historial.setOrdenDeVenta(ordenDeVenta.get());
            historial.setMontoPagado(montoPagado);
            // Nota: fecha no se actualiza para mantener la fecha original

            // Guardar
            HistorialPagosClientes savedHistorial = historialPagosClientesRepository.save(historial);

            // Crear DTO para respuesta
            HistorialPagosClientesDTO dto = new HistorialPagosClientesDTO();
            dto.setId(savedHistorial.getId());
            dto.setMontoPagado(savedHistorial.getMontoPagado());
            dto.setFecha(savedHistorial.getFecha());
            dto.setPersonaId(savedHistorial.getPersona().getId());
            dto.setPersonaNombre(savedHistorial.getPersona().getNombre());
            dto.setPersonaApellidoPaterno(savedHistorial.getPersona().getApellidoPaterno());
            dto.setPersonaApellidoMaterno(savedHistorial.getPersona().getApellidoMaterno());
            dto.setPersonaTelefono(savedHistorial.getPersona().getTelefono());
            dto.setPersonaEmail(savedHistorial.getPersona().getEmail());
            dto.setOrdenDeVentaId(savedHistorial.getOrdenDeVenta().getId());
            dto.setOrdenDeVentaTotalVenta(savedHistorial.getOrdenDeVenta().getTotalVenta());
            dto.setOrdenDeVentaFechaOrden(savedHistorial.getOrdenDeVenta().getFechaOrden());

            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHistorialPagoCliente(@PathVariable String id) {
        if (!historialPagosClientesRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        historialPagosClientesRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
