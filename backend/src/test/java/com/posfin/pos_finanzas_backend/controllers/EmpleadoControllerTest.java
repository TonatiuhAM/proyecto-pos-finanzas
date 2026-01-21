package com.posfin.pos_finanzas_backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.posfin.pos_finanzas_backend.dtos.EmpleadoCreateRequestDTO;
import com.posfin.pos_finanzas_backend.dtos.EmpleadoEstadoRequestDTO;
import com.posfin.pos_finanzas_backend.dtos.EmpleadoResponseDTO;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para EmpleadoController
 * 
 * Prueba el flujo completo de gestión de empleados incluyendo:
 * - Creación de empleados con roles y contraseñas
 * - Consulta de empleados
 * - Cambio de estado (Activo/Inactivo)
 * - Validación de campos requeridos
 * - Autenticación y autorización
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Tests de EmpleadoController (Gestión de Empleados)")
class EmpleadoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuariosRepository usuariosRepository;

    @Autowired
    private EstadosRepository estadosRepository;

    @Autowired
    private RolesRepository rolesRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private String jwtToken;
    private Estados estadoActivo;
    private Estados estadoInactivo;
    private Roles rolAdmin;
    private Roles rolVendedor;
    private Usuarios usuarioAdmin;
    private Usuarios empleadoVendedor;

    @BeforeEach
    void setUp() {
        // Limpiar solo usuarios de test usando IDs específicos
        usuariosRepository.findById("usuario-admin-test-empleado").ifPresent(usuariosRepository::delete);
        usuariosRepository.findById("empleado-vendedor-test").ifPresent(usuariosRepository::delete);
        usuariosRepository.findById("empleado-inactivo-test").ifPresent(usuariosRepository::delete);
        
        // Limpiar usuario si existe (para evitar conflictos)
        usuariosRepository.findByNombre("empleado-test-admin").ifPresent(u -> {
            usuariosRepository.delete(u);
        });

        // Crear o reutilizar estados
        estadoActivo = estadosRepository.findById("estado-activo-test-empleado").orElseGet(() -> {
            Estados estado = new Estados();
            estado.setId("estado-activo-test-empleado");
            estado.setEstado("Activo");
            return estadosRepository.save(estado);
        });

        estadoInactivo = estadosRepository.findById("estado-inactivo-test-empleado").orElseGet(() -> {
            Estados estado = new Estados();
            estado.setId("estado-inactivo-test-empleado");
            estado.setEstado("Inactivo");
            return estadosRepository.save(estado);
        });

        // Crear o reutilizar roles
        rolAdmin = rolesRepository.findById("rol-admin-test-empleado").orElseGet(() -> {
            Roles rol = new Roles();
            rol.setId("rol-admin-test-empleado");
            rol.setRoles("Administrador");
            return rolesRepository.save(rol);
        });

        rolVendedor = rolesRepository.findById("rol-vendedor-test-empleado").orElseGet(() -> {
            Roles rol = new Roles();
            rol.setId("rol-vendedor-test-empleado");
            rol.setRoles("Vendedor");
            return rolesRepository.save(rol);
        });

        // Crear usuario admin para autenticación
        usuarioAdmin = new Usuarios();
        usuarioAdmin.setId("usuario-admin-test-empleado");
        usuarioAdmin.setNombre("empleado-test-admin");
        usuarioAdmin.setTelefono("5551111111");
        usuarioAdmin.setContrasena(passwordEncoder.encode("admin123"));
        usuarioAdmin.setEstados(estadoActivo);
        usuarioAdmin.setRoles(rolAdmin);
        usuarioAdmin = usuariosRepository.save(usuarioAdmin);

        // Generar token JWT para requests autenticados
        jwtToken = jwtService.generateToken(usuarioAdmin.getNombre(), usuarioAdmin.getId());

        // Crear empleado vendedor de prueba
        empleadoVendedor = new Usuarios();
        empleadoVendedor.setId("empleado-vendedor-test");
        empleadoVendedor.setNombre("Juan Vendedor");
        empleadoVendedor.setTelefono("5552222222");
        empleadoVendedor.setContrasena(passwordEncoder.encode("vendedor123"));
        empleadoVendedor.setEstados(estadoActivo);
        empleadoVendedor.setRoles(rolVendedor);
        empleadoVendedor = usuariosRepository.save(empleadoVendedor);
    }

    @Test
    @DisplayName("GET /api/empleados - Debe obtener todos los empleados")
    void testObtenerTodosLosEmpleados_DebeRetornarLista() throws Exception {
        // Verificar que el usuario sigue existiendo y regenerar token si es necesario
        Usuarios usuarioVerificado = usuariosRepository.findById(usuarioAdmin.getId()).orElse(null);
        assertThat(usuarioVerificado).isNotNull();
        
        // Regenerar token para asegurar validez
        String tokenFresco = jwtService.generateToken(usuarioVerificado.getNombre(), usuarioVerificado.getId());
        
        // Act & Assert
        MvcResult result = mockMvc.perform(get("/api/empleados")
                .header("Authorization", "Bearer " + tokenFresco))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        EmpleadoResponseDTO[] empleados = objectMapper.readValue(responseBody, EmpleadoResponseDTO[].class);
        
        assertThat(empleados).hasSizeGreaterThanOrEqualTo(2);
        assertThat(empleados).anyMatch(e -> e.getNombre().equals("empleado-test-admin"));
        assertThat(empleados).anyMatch(e -> e.getNombre().equals("Juan Vendedor"));
    }

    @Test
    @DisplayName("POST /api/empleados - Debe crear empleado exitosamente con todos los campos")
    void testCrearEmpleado_ConTodosLosCampos_DebeRetornar200() throws Exception {
        // Arrange
        EmpleadoCreateRequestDTO request = new EmpleadoCreateRequestDTO();
        request.setNombre("Pedro Cajero");
        request.setContrasena("cajero123");
        request.setTelefono("5553333333");
        request.setRolId("rol-vendedor-test-empleado");

        // Act & Assert
        MvcResult result = mockMvc.perform(post("/api/empleados")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.nombre").value("Pedro Cajero"))
                .andExpect(jsonPath("$.telefono").value("5553333333"))
                .andExpect(jsonPath("$.rolNombre").value("Vendedor"))
                .andReturn();

        // Verificar que se guardó en la base de datos
        String responseBody = result.getResponse().getContentAsString();
        EmpleadoResponseDTO response = objectMapper.readValue(responseBody, EmpleadoResponseDTO.class);
        
        Usuarios empleadoGuardado = usuariosRepository.findById(response.getId()).orElse(null);
        assertThat(empleadoGuardado).isNotNull();
        assertThat(empleadoGuardado.getNombre()).isEqualTo("Pedro Cajero");
        assertThat(passwordEncoder.matches("cajero123", empleadoGuardado.getContrasena())).isTrue();
        
        // Limpiar el empleado creado
        usuariosRepository.deleteById(response.getId());
    }

    @Test
    @DisplayName("POST /api/empleados - Debe rechazar creación sin nombre")
    void testCrearEmpleado_SinNombre_DebeRetornar400() throws Exception {
        // Arrange
        EmpleadoCreateRequestDTO request = new EmpleadoCreateRequestDTO();
        request.setContrasena("password123");
        request.setTelefono("5554444444");
        request.setRolId("rol-vendedor-test-empleado");
        // Falta nombre

        // Act & Assert
        mockMvc.perform(post("/api/empleados")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("El nombre es requerido"));
    }

    @Test
    @DisplayName("POST /api/empleados - Debe rechazar creación sin contraseña")
    void testCrearEmpleado_SinContrasena_DebeRetornar400() throws Exception {
        // Arrange
        EmpleadoCreateRequestDTO request = new EmpleadoCreateRequestDTO();
        request.setNombre("Test Usuario");
        request.setTelefono("5555555555");
        request.setRolId("rol-vendedor-test-empleado");
        // Falta contraseña

        // Act & Assert
        mockMvc.perform(post("/api/empleados")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("La contraseña es requerida"));
    }

    @Test
    @DisplayName("POST /api/empleados - Debe rechazar creación sin rol")
    void testCrearEmpleado_SinRol_DebeRetornar400() throws Exception {
        // Arrange
        EmpleadoCreateRequestDTO request = new EmpleadoCreateRequestDTO();
        request.setNombre("Test Usuario");
        request.setContrasena("password123");
        request.setTelefono("5556666666");
        // Falta rolId

        // Act & Assert
        mockMvc.perform(post("/api/empleados")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("El rol es requerido"));
    }

    @Test
    @DisplayName("POST /api/empleados - Debe rechazar creación con rol inexistente")
    void testCrearEmpleado_ConRolInexistente_DebeRetornar400O500() throws Exception {
        // Arrange
        EmpleadoCreateRequestDTO request = new EmpleadoCreateRequestDTO();
        request.setNombre("Test Usuario");
        request.setContrasena("password123");
        request.setTelefono("5557777777");
        request.setRolId("rol-no-existe");

        // Act & Assert - Puede retornar 400 (IllegalArgumentException) o 500 (Exception no controlada)
        mockMvc.perform(post("/api/empleados")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("GET /api/empleados/{id} - Debe obtener empleado por ID existente")
    void testObtenerEmpleadoPorId_ConIdExistente_DebeRetornarEmpleado() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/empleados/{id}", empleadoVendedor.getId())
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(empleadoVendedor.getId()))
                .andExpect(jsonPath("$.nombre").value("Juan Vendedor"))
                .andExpect(jsonPath("$.telefono").value("5552222222"))
                .andExpect(jsonPath("$.rolNombre").value("Vendedor"))
                .andExpect(jsonPath("$.estadoNombre").value("Activo"));
    }

    @Test
    @DisplayName("GET /api/empleados/{id} - Debe retornar 400 con ID inexistente")
    void testObtenerEmpleadoPorId_ConIdInexistente_DebeRetornar400() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/empleados/{id}", "id-no-existe")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/empleados/{id}/estado - Debe cambiar estado de empleado a Inactivo")
    void testCambiarEstadoEmpleado_AInactivo_DebeRetornar200() throws Exception {
        // Arrange
        EmpleadoEstadoRequestDTO request = new EmpleadoEstadoRequestDTO();
        request.setEstado("Inactivo");

        // Act & Assert
        mockMvc.perform(put("/api/empleados/{id}/estado", empleadoVendedor.getId())
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(empleadoVendedor.getId()))
                .andExpect(jsonPath("$.estadoNombre").value("Inactivo"));

        // Verificar que se actualizó en la base de datos
        Usuarios empleadoActualizado = usuariosRepository.findById(empleadoVendedor.getId()).orElse(null);
        assertThat(empleadoActualizado).isNotNull();
        
        // Cargar estado explícitamente para evitar lazy loading
        Estados estadoCargado = estadosRepository.findById(empleadoActualizado.getEstados().getId()).orElse(null);
        assertThat(estadoCargado).isNotNull();
        assertThat(estadoCargado.getEstado()).isEqualTo("Inactivo");
        
        // Restaurar estado activo
        empleadoActualizado.setEstados(estadoActivo);
        usuariosRepository.save(empleadoActualizado);
    }

    @Test
    @DisplayName("PUT /api/empleados/{id}/estado - Debe cambiar estado de empleado a Activo")
    void testCambiarEstadoEmpleado_AActivo_DebeRetornar200() throws Exception {
        // Arrange - Crear empleado inactivo
        Usuarios empleadoInactivo = new Usuarios();
        empleadoInactivo.setId("empleado-inactivo-test");
        empleadoInactivo.setNombre("María Inactiva");
        empleadoInactivo.setTelefono("5558888888");
        empleadoInactivo.setContrasena(passwordEncoder.encode("inactiva123"));
        empleadoInactivo.setEstados(estadoInactivo);
        empleadoInactivo.setRoles(rolVendedor);
        empleadoInactivo = usuariosRepository.save(empleadoInactivo);

        EmpleadoEstadoRequestDTO request = new EmpleadoEstadoRequestDTO();
        request.setEstado("Activo");

        // Act & Assert
        mockMvc.perform(put("/api/empleados/{id}/estado", empleadoInactivo.getId())
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(empleadoInactivo.getId()))
                .andExpect(jsonPath("$.estadoNombre").value("Activo"));

        // Verificar que se actualizó
        Usuarios empleadoActualizado = usuariosRepository.findById(empleadoInactivo.getId()).orElse(null);
        assertThat(empleadoActualizado).isNotNull();
        
        Estados estadoCargado = estadosRepository.findById(empleadoActualizado.getEstados().getId()).orElse(null);
        assertThat(estadoCargado).isNotNull();
        assertThat(estadoCargado.getEstado()).isEqualTo("Activo");
    }

    @Test
    @DisplayName("PUT /api/empleados/{id}/estado - Debe rechazar cambio sin estado")
    void testCambiarEstadoEmpleado_SinEstado_DebeRetornar400() throws Exception {
        // Arrange
        EmpleadoEstadoRequestDTO request = new EmpleadoEstadoRequestDTO();
        // Falta estado

        // Act & Assert
        mockMvc.perform(put("/api/empleados/{id}/estado", empleadoVendedor.getId())
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("El estado es requerido"));
    }

    @Test
    @DisplayName("PUT /api/empleados/{id}/estado - Debe rechazar cambio con estado inválido")
    void testCambiarEstadoEmpleado_ConEstadoInvalido_DebeRetornar400() throws Exception {
        // Arrange
        EmpleadoEstadoRequestDTO request = new EmpleadoEstadoRequestDTO();
        request.setEstado("EstadoInvalido");

        // Act & Assert
        mockMvc.perform(put("/api/empleados/{id}/estado", empleadoVendedor.getId())
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Estado inválido. Debe ser 'Activo' o 'Inactivo'"));
    }

    @Test
    @DisplayName("PUT /api/empleados/{id}/estado - Debe retornar error con ID inexistente")
    void testCambiarEstadoEmpleado_ConIdInexistente_DebeRetornarError() throws Exception {
        // Arrange
        EmpleadoEstadoRequestDTO request = new EmpleadoEstadoRequestDTO();
        request.setEstado("Inactivo");

        // Act & Assert - Puede retornar 400 (IllegalArgumentException) o 500 (Exception no controlada)
        mockMvc.perform(put("/api/empleados/{id}/estado", "id-no-existe")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("POST /api/empleados - Debe rechazar creación sin autenticación")
    void testCrearEmpleado_SinAutenticacion_DebeRetornar401() throws Exception {
        // Arrange
        EmpleadoCreateRequestDTO request = new EmpleadoCreateRequestDTO();
        request.setNombre("Test Sin Auth");
        request.setContrasena("password123");
        request.setTelefono("5559999999");
        request.setRolId("rol-vendedor-test-empleado");

        // Act & Assert - Sin header Authorization
        mockMvc.perform(post("/api/empleados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
