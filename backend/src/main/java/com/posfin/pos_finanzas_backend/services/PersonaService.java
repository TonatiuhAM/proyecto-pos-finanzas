package com.posfin.pos_finanzas_backend.services;

import com.posfin.pos_finanzas_backend.dtos.PersonaCreateRequestDTO;
import com.posfin.pos_finanzas_backend.dtos.PersonaResponseDTO;
import com.posfin.pos_finanzas_backend.models.*;
import com.posfin.pos_finanzas_backend.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class PersonaService {

    @Autowired
    private PersonasRepository personasRepository;

    @Autowired
    private CategoriaPersonasRepository categoriaPersonasRepository;

    @Autowired
    private EstadosRepository estadosRepository;

    /**
     * Crear una nueva persona
     */
    public PersonaResponseDTO crearPersona(PersonaCreateRequestDTO request) {
        // Validar que la categoría existe
        CategoriaPersonas categoria = categoriaPersonasRepository.findById(request.getIdCategoriaPersona())
            .orElseThrow(() -> new IllegalArgumentException("La categoría de persona no existe"));

        // Obtener el estado "Activo" (nombre = 'Activo')
        Estados estadoActivo = estadosRepository.findByEstado("Activo")
            .orElseThrow(() -> new IllegalArgumentException("No se encontró el estado activo"));

        // Validar que no existe otra persona con el mismo RFC (si se proporciona)
        if (request.getRfc() != null && !request.getRfc().trim().isEmpty()) {
            List<Personas> personasConRfc = personasRepository.findByRfcAndEstados_Estado(request.getRfc(), "Activo");
            if (!personasConRfc.isEmpty()) {
                throw new IllegalArgumentException("Ya existe una persona activa con el RFC: " + request.getRfc());
            }
        }

        // Validar que no existe otra persona con el mismo email (si se proporciona)
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            List<Personas> personasConEmail = personasRepository.findByEmailAndEstados_Estado(request.getEmail(), "Activo");
            if (!personasConEmail.isEmpty()) {
                throw new IllegalArgumentException("Ya existe una persona activa con el email: " + request.getEmail());
            }
        }

        // Crear nueva persona
        Personas nuevaPersona = new Personas();
        nuevaPersona.setNombre(request.getNombre().trim());
        
        // Separar apellidos en paterno y materno si viene como un solo campo "apellidos"
        String apellidos = request.getApellidos().trim();
        String[] partesApellidos = apellidos.split("\\s+", 2);
        nuevaPersona.setApellidoPaterno(partesApellidos[0]);
        if (partesApellidos.length > 1) {
            nuevaPersona.setApellidoMaterno(partesApellidos[1]);
        }
        
        nuevaPersona.setRfc(request.getRfc() != null ? request.getRfc().trim().toUpperCase() : null);
        nuevaPersona.setEmail(request.getEmail() != null ? request.getEmail().trim().toLowerCase() : null);
        nuevaPersona.setTelefono(request.getTelefono() != null ? request.getTelefono().trim() : null);
        nuevaPersona.setCategoriaPersonas(categoria);
        nuevaPersona.setEstados(estadoActivo);

        // Guardar en la base de datos
        Personas personaGuardada = personasRepository.save(nuevaPersona);

        // Convertir a DTO y retornar
        return convertirAPersonaResponseDTO(personaGuardada, categoria, estadoActivo);
    }

    /**
     * Obtener todas las personas por categoría
     */
    @Transactional(readOnly = true)
    public List<PersonaResponseDTO> obtenerPersonasPorCategoria(String idCategoria) {
        // Validar que la categoría existe
        categoriaPersonasRepository.findById(idCategoria)
            .orElseThrow(() -> new IllegalArgumentException("La categoría de persona no existe"));

        List<Personas> personas = personasRepository.findByCategoriaPersonas_IdOrderByNombreAsc(idCategoria);
        
        return personas.stream()
            .map(this::convertirAPersonaResponseDTO)
            .collect(Collectors.toList());
    }

    /**
     * Obtener todas las personas activas por categoría
     */
    @Transactional(readOnly = true)
    public List<PersonaResponseDTO> obtenerPersonasActivasPorCategoria(String idCategoria) {
        // Validar que la categoría existe
        categoriaPersonasRepository.findById(idCategoria)
            .orElseThrow(() -> new IllegalArgumentException("La categoría de persona no existe"));

        List<Personas> personas = personasRepository.findByCategoriaPersonas_IdAndEstados_EstadoOrderByNombreAsc(idCategoria, "Activo");
        
        return personas.stream()
            .map(this::convertirAPersonaResponseDTO)
            .collect(Collectors.toList());
    }

    /**
     * Obtener una persona por ID
     */
    @Transactional(readOnly = true)
    public PersonaResponseDTO obtenerPersonaPorId(String id) {
        Personas persona = personasRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("La persona no existe"));

        return convertirAPersonaResponseDTO(persona);
    }

    /**
     * Actualizar el estado de una persona
     */
    public PersonaResponseDTO actualizarEstadoPersona(String id, String nuevoEstadoNombre) {
        // Validar que la persona existe
        Personas persona = personasRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("La persona no existe"));

        // Validar que el estado existe
        Estados nuevoEstado = estadosRepository.findByEstado(nuevoEstadoNombre)
            .orElseThrow(() -> new IllegalArgumentException("El estado no existe"));

        // Actualizar estado
        persona.setEstados(nuevoEstado);

        Personas personaActualizada = personasRepository.save(persona);
        return convertirAPersonaResponseDTO(personaActualizada);
    }

    /**
     * Eliminar una persona (soft delete - cambiar estado a inactivo)
     */
    public void eliminarPersona(String id) {
        // Validar que la persona existe
        Personas persona = personasRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("La persona no existe"));

        // Obtener estado inactivo
        Estados estadoInactivo = estadosRepository.findByEstado("Inactivo")
            .orElseThrow(() -> new IllegalArgumentException("No se encontró el estado inactivo"));

        // Cambiar estado a inactivo
        persona.setEstados(estadoInactivo);
        personasRepository.save(persona);
    }

    /**
     * Método auxiliar para convertir Persona a PersonaResponseDTO
     */
    private PersonaResponseDTO convertirAPersonaResponseDTO(Personas persona) {
        return convertirAPersonaResponseDTO(persona, persona.getCategoriaPersonas(), persona.getEstados());
    }

    /**
     * Método auxiliar para convertir Persona a PersonaResponseDTO con datos relacionados
     */
    private PersonaResponseDTO convertirAPersonaResponseDTO(Personas persona, CategoriaPersonas categoria, Estados estado) {
        return new PersonaResponseDTO(
            persona.getId(),
            persona.getNombre(),
            persona.getApellidoPaterno(),
            persona.getApellidoMaterno(),
            persona.getRfc(),
            persona.getEmail(),
            persona.getTelefono(),
            categoria.getId(),
            categoria.getCategoria(),
            estado.getId(),
            estado.getEstado()
        );
    }
}