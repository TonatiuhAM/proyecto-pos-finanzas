package com.posfin.pos_finanzas_backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.posfin.pos_finanzas_backend.dtos.PersonaCreateRequestDTO;
import com.posfin.pos_finanzas_backend.dtos.PersonaResponseDTO;
import com.posfin.pos_finanzas_backend.models.*;
import com.posfin.pos_finanzas_backend.repositories.*;
import com.posfin.pos_finanzas_backend.services.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para PersonaController
 * 
 * Prueba el flujo completo de gestión de personas incluyendo:
 * - Creación de personas (clientes/proveedores)
 * - Consulta de personas por ID, categoría y estado
 * - Actualización de estado de personas
 * - Eliminación (soft delete) de personas
 * - Validación de campos requeridos y opcionales
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Tests de PersonaController (Gestión de Personas)")
class PersonaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PersonasRepository personasRepository;

    @Autowired
    private EstadosRepository estadosRepository;

    @Autowired
    private CategoriaPersonasRepository categoriaPersonasRepository;

    @Autowired
    private UsuariosRepository usuariosRepository;

    @Autowired
    private RolesRepository rolesRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private String jwtToken;
    private Estados estadoActivo;
    private Estados estadoInactivo;
    private CategoriaPersonas categoriaCliente;
    private CategoriaPersonas categoriaProveedor;
    private Usuarios usuarioAdmin;
    private Roles rolAdmin;
    private Personas personaCliente;
    private Personas personaProveedor;

    @BeforeEach
    void setUp() {
        // Limpiar solo personas y usuarios de test usando IDs específicos
        personasRepository.findById("persona-cliente-test").ifPresent(personasRepository::delete);
        personasRepository.findById("persona-proveedor-test").ifPresent(personasRepository::delete);
        personasRepository.findById("persona-inactiva-test").ifPresent(personasRepository::delete);
        
        // Limpiar usuario si existe (para evitar conflictos)
        usuariosRepository.findByNombre("persona-test-admin").ifPresent(u -> {
            usuariosRepository.delete(u);
        });

        // Crear o reutilizar estados
        estadoActivo = estadosRepository.findById("estado-activo-test-persona").orElseGet(() -> {
            Estados estado = new Estados();
            estado.setId("estado-activo-test-persona");
            estado.setEstado("Activo");
            return estadosRepository.save(estado);
        });

        estadoInactivo = estadosRepository.findById("estado-inactivo-test-persona").orElseGet(() -> {
            Estados estado = new Estados();
            estado.setId("estado-inactivo-test-persona");
            estado.setEstado("Inactivo");
            return estadosRepository.save(estado);
        });

        // Crear o reutilizar categorías de personas
        categoriaCliente = categoriaPersonasRepository.findById("cat-cliente-test").orElseGet(() -> {
            CategoriaPersonas categoria = new CategoriaPersonas();
            categoria.setId("cat-cliente-test");
            categoria.setCategoria("Cliente");
            return categoriaPersonasRepository.save(categoria);
        });

        categoriaProveedor = categoriaPersonasRepository.findById("cat-proveedor-test").orElseGet(() -> {
            CategoriaPersonas categoria = new CategoriaPersonas();
            categoria.setId("cat-proveedor-test");
            categoria.setCategoria("Proveedor");
            return categoriaPersonasRepository.save(categoria);
        });

        // Crear o reutilizar rol
        rolAdmin = rolesRepository.findById("rol-admin-test-persona").orElseGet(() -> {
            Roles rol = new Roles();
            rol.setId("rol-admin-test-persona");
            rol.setRoles("Administrador");
            return rolesRepository.save(rol);
        });

        // Crear usuario admin para autenticación
        usuarioAdmin = new Usuarios();
        usuarioAdmin.setId("usuario-admin-test-persona");
        usuarioAdmin.setNombre("persona-test-admin");
        usuarioAdmin.setTelefono("5551234567");
        usuarioAdmin.setContrasena(passwordEncoder.encode("admin123"));
        usuarioAdmin.setEstados(estadoActivo);
        usuarioAdmin.setRoles(rolAdmin);
        usuarioAdmin = usuariosRepository.save(usuarioAdmin);

        // Generar token JWT para requests autenticados
        jwtToken = jwtService.generateToken(usuarioAdmin.getNombre(), usuarioAdmin.getId());

        // Crear personas de prueba
        personaCliente = new Personas();
        personaCliente.setId("persona-cliente-test");
        personaCliente.setNombre("Juan");
        personaCliente.setApellidoPaterno("Pérez");
        personaCliente.setApellidoMaterno("García");
        personaCliente.setRfc("PEGJ850101ABC");
        personaCliente.setEmail("juan@example.com");
        personaCliente.setTelefono("5551111111");
        personaCliente.setCategoriaPersonas(categoriaCliente);
        personaCliente.setEstados(estadoActivo);
        personaCliente = personasRepository.save(personaCliente);

        personaProveedor = new Personas();
        personaProveedor.setId("persona-proveedor-test");
        personaProveedor.setNombre("María");
        personaProveedor.setApellidoPaterno("López");
        personaProveedor.setApellidoMaterno("Martínez");
        personaProveedor.setRfc("LOMM900202XYZ");
        personaProveedor.setEmail("maria@proveedor.com");
        personaProveedor.setTelefono("5552222222");
        personaProveedor.setCategoriaPersonas(categoriaProveedor);
        personaProveedor.setEstados(estadoActivo);
        personaProveedor = personasRepository.save(personaProveedor);
    }

    @Test
    @DisplayName("POST /api/personas - Debe crear persona exitosamente con todos los campos")
    void testCrearPersona_ConTodosLosCampos_DebeRetornar201() throws Exception {
        // Arrange
        PersonaCreateRequestDTO request = new PersonaCreateRequestDTO();
        request.setNombre("Pedro");
        request.setApellidos("Sánchez Ramírez");
        request.setRfc("SARP950303DEF");
        request.setEmail("pedro@example.com");
        request.setTelefono("5553333333");
        request.setIdCategoriaPersona("cat-cliente-test");

        // Act & Assert
        MvcResult result = mockMvc.perform(post("/api/personas")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.nombre").value("Pedro"))
                .andExpect(jsonPath("$.apellidoPaterno").value("Sánchez"))
                .andExpect(jsonPath("$.apellidoMaterno").value("Ramírez"))
                .andExpect(jsonPath("$.rfc").value("SARP950303DEF"))
                .andExpect(jsonPath("$.email").value("pedro@example.com"))
                .andExpect(jsonPath("$.telefono").value("5553333333"))
                .andReturn();

        // Verificar que se guardó en la base de datos
        String responseBody = result.getResponse().getContentAsString();
        PersonaResponseDTO response = objectMapper.readValue(responseBody, PersonaResponseDTO.class);
        
        Personas personaGuardada = personasRepository.findById(response.getId()).orElse(null);
        assertThat(personaGuardada).isNotNull();
        assertThat(personaGuardada.getNombre()).isEqualTo("Pedro");
        assertThat(personaGuardada.getRfc()).isEqualTo("SARP950303DEF");
        
        // Limpiar la persona creada
        personasRepository.deleteById(response.getId());
    }

    @Test
    @DisplayName("POST /api/personas - Debe crear persona sin RFC y email (campos opcionales)")
    void testCrearPersona_SinRfcNiEmail_DebeRetornar201() throws Exception {
        // Arrange
        PersonaCreateRequestDTO request = new PersonaCreateRequestDTO();
        request.setNombre("Ana");
        request.setApellidos("García López");
        request.setTelefono("5554444444");
        request.setIdCategoriaPersona("cat-cliente-test");

        // Act & Assert
        MvcResult result = mockMvc.perform(post("/api/personas")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Ana"))
                .andExpect(jsonPath("$.apellidoPaterno").value("García"))
                .andExpect(jsonPath("$.apellidoMaterno").value("López"))
                .andReturn();
        
        // Limpiar
        String responseBody = result.getResponse().getContentAsString();
        PersonaResponseDTO response = objectMapper.readValue(responseBody, PersonaResponseDTO.class);
        personasRepository.deleteById(response.getId());
    }

    @Test
    @DisplayName("POST /api/personas - Debe rechazar creación sin nombre")
    void testCrearPersona_SinNombre_DebeRetornar400() throws Exception {
        // Arrange
        PersonaCreateRequestDTO request = new PersonaCreateRequestDTO();
        request.setApellidos("Test Test");
        request.setTelefono("5555555555");
        request.setIdCategoriaPersona("cat-cliente-test");

        // Act & Assert
        mockMvc.perform(post("/api/personas")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/personas - Debe obtener todas las personas")
    void testObtenerTodasLasPersonas_DebeRetornarListaCompleta() throws Exception {
        // Verificar que el usuario sigue existiendo y regenerar token si es necesario
        Usuarios usuarioVerificado = usuariosRepository.findById(usuarioAdmin.getId()).orElse(null);
        assertThat(usuarioVerificado).isNotNull();
        assertThat(usuarioVerificado.getNombre()).isEqualTo("persona-test-admin");
        
        // Regenerar token para asegurar validez
        String tokenFresco = jwtService.generateToken(usuarioVerificado.getNombre(), usuarioVerificado.getId());
        
        // Act & Assert
        MvcResult result = mockMvc.perform(get("/api/personas")
                .header("Authorization", "Bearer " + tokenFresco))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        PersonaResponseDTO[] personas = objectMapper.readValue(responseBody, PersonaResponseDTO[].class);
        
        assertThat(personas).hasSizeGreaterThanOrEqualTo(2);
        assertThat(personas).anyMatch(p -> p.getNombre().equals("Juan"));
        assertThat(personas).anyMatch(p -> p.getNombre().equals("María"));
    }

    @Test
    @DisplayName("GET /api/personas/{id} - Debe obtener persona por ID existente")
    void testObtenerPersonaPorId_ConIdExistente_DebeRetornarPersona() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/personas/{id}", personaCliente.getId())
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(personaCliente.getId()))
                .andExpect(jsonPath("$.nombre").value("Juan"))
                .andExpect(jsonPath("$.apellidoPaterno").value("Pérez"))
                .andExpect(jsonPath("$.rfc").value("PEGJ850101ABC"))
                .andExpect(jsonPath("$.email").value("juan@example.com"));
    }

    @Test
    @DisplayName("GET /api/personas/{id} - Debe retornar 404 con ID inexistente")
    void testObtenerPersonaPorId_ConIdInexistente_DebeRetornar404() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/personas/{id}", "id-no-existe")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/personas/categoria/{idCategoria} - Debe obtener personas por categoría")
    void testObtenerPersonasPorCategoria_DebeRetornarPersonasDeCategoria() throws Exception {
        // Act & Assert
        MvcResult result = mockMvc.perform(get("/api/personas/categoria/{idCategoria}", "cat-cliente-test")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        PersonaResponseDTO[] personas = objectMapper.readValue(responseBody, PersonaResponseDTO[].class);
        
        assertThat(personas).isNotEmpty();
        assertThat(personas).anyMatch(p -> p.getNombre().equals("Juan"));
        assertThat(personas).noneMatch(p -> p.getNombre().equals("María")); // María es proveedor
    }

    @Test
    @DisplayName("GET /api/personas/categoria/{idCategoria}/activos - Debe obtener solo personas activas")
    void testObtenerPersonasActivasPorCategoria_DebeRetornarSoloActivos() throws Exception {
        // Arrange - Crear una persona inactiva
        Personas personaInactiva = new Personas();
        personaInactiva.setId("persona-inactiva-test");
        personaInactiva.setNombre("Pedro");
        personaInactiva.setApellidoPaterno("Inactive");
        personaInactiva.setApellidoMaterno("Test");
        personaInactiva.setTelefono("5559999999");
        personaInactiva.setCategoriaPersonas(categoriaCliente);
        personaInactiva.setEstados(estadoInactivo); // INACTIVO
        personasRepository.save(personaInactiva);

        // Act & Assert
        MvcResult result = mockMvc.perform(get("/api/personas/categoria/{idCategoria}/activos", "cat-cliente-test")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        PersonaResponseDTO[] personas = objectMapper.readValue(responseBody, PersonaResponseDTO[].class);

        // Verificar que solo retorna personas activas
        assertThat(personas).isNotEmpty();
        assertThat(personas).allMatch(p -> p.getNombreEstado().equals("Activo"));
        assertThat(personas).anyMatch(p -> p.getNombre().equals("Juan")); // Juan está activo
        assertThat(personas).noneMatch(p -> p.getNombre().equals("Pedro")); // Pedro está inactivo
    }

    @Test
    @DisplayName("PATCH /api/personas/{id}/estado - Debe actualizar estado de persona")
    void testActualizarEstadoPersona_ConEstadoValido_DebeRetornar200() throws Exception {
        // Arrange
        Map<String, String> request = new HashMap<>();
        request.put("estadoNombre", "Inactivo");

        // Act & Assert
        mockMvc.perform(patch("/api/personas/{id}/estado", personaCliente.getId())
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(personaCliente.getId()))
                .andExpect(jsonPath("$.nombreEstado").value("Inactivo"));

        // Verificar que se actualizó en la base de datos
        Personas personaActualizada = personasRepository.findById(personaCliente.getId()).orElse(null);
        assertThat(personaActualizada).isNotNull();
        // Forzar inicialización del proxy lazy cargando el estado directamente
        Estados estadoCargado = estadosRepository.findById(personaActualizada.getEstados().getId()).orElse(null);
        assertThat(estadoCargado).isNotNull();
        assertThat(estadoCargado.getEstado()).isEqualTo("Inactivo");
        
        // Restaurar estado activo
        personaActualizada.setEstados(estadoActivo);
        personasRepository.save(personaActualizada);
    }

    @Test
    @DisplayName("PATCH /api/personas/{id}/estado - Debe rechazar actualización sin estadoNombre")
    void testActualizarEstadoPersona_SinEstadoNombre_DebeRetornar400() throws Exception {
        // Arrange
        Map<String, String> request = new HashMap<>();
        // Falta estadoNombre

        // Act & Assert
        mockMvc.perform(patch("/api/personas/{id}/estado", personaCliente.getId())
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/personas/{id}/estado - Debe retornar 404 con ID inexistente")
    void testActualizarEstadoPersona_ConIdInexistente_DebeRetornar404() throws Exception {
        // Arrange
        Map<String, String> request = new HashMap<>();
        request.put("estadoNombre", "Inactivo");

        // Act & Assert
        mockMvc.perform(patch("/api/personas/{id}/estado", "id-no-existe")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/personas/{id} - Debe eliminar persona exitosamente (soft delete)")
    void testEliminarPersona_ConIdExistente_DebeRetornar204() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/personas/{id}", personaCliente.getId())
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());

        // Verificar que se marcó como inactivo (soft delete)
        Personas personaEliminada = personasRepository.findById(personaCliente.getId()).orElse(null);
        assertThat(personaEliminada).isNotNull();
        // Forzar inicialización del proxy lazy cargando el estado directamente
        Estados estadoCargado = estadosRepository.findById(personaEliminada.getEstados().getId()).orElse(null);
        assertThat(estadoCargado).isNotNull();
        assertThat(estadoCargado.getEstado()).isEqualTo("Inactivo");
        
        // Restaurar estado activo para otros tests
        personaEliminada.setEstados(estadoActivo);
        personasRepository.save(personaEliminada);
    }

    @Test
    @DisplayName("DELETE /api/personas/{id} - Debe retornar 404 con ID inexistente")
    void testEliminarPersona_ConIdInexistente_DebeRetornar404() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/personas/{id}", "id-no-existe")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/personas - Debe rechazar creación sin token de autenticación")
    void testCrearPersona_SinAutenticacion_DebeRetornar401() throws Exception {
        // Arrange
        PersonaCreateRequestDTO request = new PersonaCreateRequestDTO();
        request.setNombre("Test");
        request.setApellidos("Sin Auth");
        request.setTelefono("5550000000");
        request.setIdCategoriaPersona("cat-cliente-test");

        // Act & Assert - Sin header Authorization
        mockMvc.perform(post("/api/personas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
