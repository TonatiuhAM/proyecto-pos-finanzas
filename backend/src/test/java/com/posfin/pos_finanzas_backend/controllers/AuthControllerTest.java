package com.posfin.pos_finanzas_backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.posfin.pos_finanzas_backend.dtos.LoginResponseDTO;
import com.posfin.pos_finanzas_backend.models.Estados;
import com.posfin.pos_finanzas_backend.models.Roles;
import com.posfin.pos_finanzas_backend.models.Usuarios;
import com.posfin.pos_finanzas_backend.repositories.EstadosRepository;
import com.posfin.pos_finanzas_backend.repositories.RolesRepository;
import com.posfin.pos_finanzas_backend.repositories.UsuariosRepository;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para AuthController
 * 
 * Prueba el flujo completo de autenticación incluyendo:
 * - Login exitoso con credenciales válidas
 * - Login fallido con credenciales incorrectas
 * - Validación de campos requeridos
 * - Generación de token JWT
 * - Validación de estado del usuario
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Tests de AuthController (Autenticación)")
class AuthControllerTest {

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

    private Estados estadoActivo;
    private Estados estadoInactivo;
    private Roles rolAdmin;
    private Roles rolVendedor;
    private Usuarios usuarioActivo;
    private Usuarios usuarioInactivo;

    @BeforeEach
    void setUp() {
        // Limpiar solo usuarios de test (no eliminar todo por foreign keys)
        usuariosRepository.findByNombre("testuser").ifPresent(usuariosRepository::delete);
        usuariosRepository.findByNombre("inactiveuser").ifPresent(usuariosRepository::delete);
        usuariosRepository.findByNombre("legacyuser").ifPresent(usuariosRepository::delete);

        // Crear o reutilizar estados
        estadoActivo = estadosRepository.findById("estado-activo-test").orElseGet(() -> {
            Estados estado = new Estados();
            estado.setId("estado-activo-test");
            estado.setEstado("Activo");
            return estadosRepository.save(estado);
        });

        estadoInactivo = estadosRepository.findById("estado-inactivo-test").orElseGet(() -> {
            Estados estado = new Estados();
            estado.setId("estado-inactivo-test");
            estado.setEstado("Inactivo");
            return estadosRepository.save(estado);
        });

        // Crear o reutilizar roles
        rolAdmin = rolesRepository.findById("rol-admin-test").orElseGet(() -> {
            Roles rol = new Roles();
            rol.setId("rol-admin-test");
            rol.setRoles("Administrador");
            return rolesRepository.save(rol);
        });

        rolVendedor = rolesRepository.findById("rol-vendedor-test").orElseGet(() -> {
            Roles rol = new Roles();
            rol.setId("rol-vendedor-test");
            rol.setRoles("Vendedor");
            return rolesRepository.save(rol);
        });

        // Crear usuario activo con contraseña hasheada
        usuarioActivo = new Usuarios();
        usuarioActivo.setId("usuario-activo-test");
        usuarioActivo.setNombre("testuser");
        usuarioActivo.setTelefono("555-1234");
        usuarioActivo.setContrasena(passwordEncoder.encode("password123"));
        usuarioActivo.setEstados(estadoActivo);
        usuarioActivo.setRoles(rolAdmin);
        usuarioActivo = usuariosRepository.save(usuarioActivo);

        // Crear usuario inactivo
        usuarioInactivo = new Usuarios();
        usuarioInactivo.setId("usuario-inactivo-test");
        usuarioInactivo.setNombre("inactiveuser");
        usuarioInactivo.setTelefono("555-5678");
        usuarioInactivo.setContrasena(passwordEncoder.encode("password456"));
        usuarioInactivo.setEstados(estadoInactivo);
        usuarioInactivo.setRoles(rolVendedor);
        usuarioInactivo = usuariosRepository.save(usuarioInactivo);
    }

    @Test
    @DisplayName("POST /api/auth/login - Debe realizar login exitoso con credenciales válidas")
    void testLogin_ConCredencialesValidas_DebeRetornarTokenYDatosUsuario() throws Exception {
        // Arrange
        Map<String, String> credentials = new HashMap<>();
        credentials.put("nombre", "testuser");
        credentials.put("contrasena", "password123");

        // Act & Assert
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.usuario").value("testuser"))
                .andExpect(jsonPath("$.rolNombre").value("Administrador"))
                .andExpect(jsonPath("$.rolId").value("rol-admin-test"))
                .andExpect(jsonPath("$.expiresIn").value(86400000L))
                .andReturn();

        // Verificar que el token JWT es válido
        String responseBody = result.getResponse().getContentAsString();
        LoginResponseDTO response = objectMapper.readValue(responseBody, LoginResponseDTO.class);
        
        assertThat(response.getToken()).isNotNull();
        assertThat(response.getToken()).isNotEmpty();
        
        // Validar el token con JwtService
        String username = jwtService.extractUsername(response.getToken());
        String userId = jwtService.extractUserId(response.getToken());
        
        assertThat(username).isEqualTo("testuser");
        assertThat(userId).isEqualTo("usuario-activo-test");
        assertThat(jwtService.validateToken(response.getToken(), "testuser")).isTrue();
    }

    @Test
    @DisplayName("POST /api/auth/login - Debe rechazar login con contraseña incorrecta")
    void testLogin_ConContrasenaIncorrecta_DebeRetornar400() throws Exception {
        // Arrange
        Map<String, String> credentials = new HashMap<>();
        credentials.put("nombre", "testuser");
        credentials.put("contrasena", "wrongpassword");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Contraseña incorrecta"));
    }

    @Test
    @DisplayName("POST /api/auth/login - Debe rechazar login con usuario inexistente")
    void testLogin_ConUsuarioInexistente_DebeRetornar400() throws Exception {
        // Arrange
        Map<String, String> credentials = new HashMap<>();
        credentials.put("nombre", "noexiste");
        credentials.put("contrasena", "password123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Usuario no encontrado"));
    }

    @Test
    @DisplayName("POST /api/auth/login - Debe rechazar login con usuario inactivo")
    void testLogin_ConUsuarioInactivo_DebeRetornar400() throws Exception {
        // Arrange
        Map<String, String> credentials = new HashMap<>();
        credentials.put("nombre", "inactiveuser");
        credentials.put("contrasena", "password456");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Usuario inactivo"));
    }

    @Test
    @DisplayName("POST /api/auth/login - Debe rechazar login sin nombre de usuario")
    void testLogin_SinNombreUsuario_DebeRetornar400() throws Exception {
        // Arrange
        Map<String, String> credentials = new HashMap<>();
        credentials.put("contrasena", "password123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Nombre de usuario y contraseña son requeridos"));
    }

    @Test
    @DisplayName("POST /api/auth/login - Debe rechazar login sin contraseña")
    void testLogin_SinContrasena_DebeRetornar400() throws Exception {
        // Arrange
        Map<String, String> credentials = new HashMap<>();
        credentials.put("nombre", "testuser");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Nombre de usuario y contraseña son requeridos"));
    }

    @Test
    @DisplayName("POST /api/auth/login - Debe rechazar login con body vacío")
    void testLogin_ConBodyVacio_DebeRetornar400() throws Exception {
        // Arrange
        Map<String, String> credentials = new HashMap<>();

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Nombre de usuario y contraseña son requeridos"));
    }

    @Test
    @DisplayName("POST /api/auth/login - Debe soportar contraseñas legacy en texto plano")
    void testLogin_ConContrasenaTextoPlano_DebeRealizarLoginExitoso() throws Exception {
        // Arrange - Crear usuario con contraseña sin hashear (legacy)
        Usuarios usuarioLegacy = new Usuarios();
        usuarioLegacy.setId("usuario-legacy-test");
        usuarioLegacy.setNombre("legacyuser");
        usuarioLegacy.setTelefono("555-9999");
        usuarioLegacy.setContrasena("plainpassword"); // Sin hashear
        usuarioLegacy.setEstados(estadoActivo);
        usuarioLegacy.setRoles(rolVendedor);
        usuariosRepository.save(usuarioLegacy);

        Map<String, String> credentials = new HashMap<>();
        credentials.put("nombre", "legacyuser");
        credentials.put("contrasena", "plainpassword");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.usuario").value("legacyuser"))
                .andExpect(jsonPath("$.rolNombre").value("Vendedor"));
    }

    @Test
    @DisplayName("POST /api/auth/create-admin - Debe crear usuario admin si no existe")
    void testCreateAdmin_SinAdminExistente_DebeCrearAdmin() throws Exception {
        // Arrange - Eliminar cualquier usuario "admin" existente
        usuariosRepository.findByNombre("admin").ifPresent(usuariosRepository::delete);

        // Act & Assert
        mockMvc.perform(post("/api/auth/create-admin"))
                .andExpect(status().isOk())
                .andExpect(content().string("Usuario admin creado exitosamente"));

        // Verificar que el admin se creó correctamente
        Usuarios adminCreado = usuariosRepository.findByNombre("admin").orElse(null);
        assertThat(adminCreado).isNotNull();
        assertThat(adminCreado.getNombre()).isEqualTo("admin");
        assertThat(adminCreado.getTelefono()).isEqualTo("555-0000");
        assertThat(passwordEncoder.matches("admin", adminCreado.getContrasena())).isTrue();
    }

    @Test
    @DisplayName("POST /api/auth/create-admin - Debe rechazar si admin ya existe")
    void testCreateAdmin_ConAdminExistente_DebeRetornarMensaje() throws Exception {
        // Arrange - Crear usuario admin
        Usuarios admin = new Usuarios();
        admin.setId("admin-existente-test");
        admin.setNombre("admin");
        admin.setTelefono("555-0000");
        admin.setContrasena(passwordEncoder.encode("admin"));
        admin.setEstados(estadoActivo);
        admin.setRoles(rolAdmin);
        usuariosRepository.save(admin);

        // Act & Assert
        mockMvc.perform(post("/api/auth/create-admin"))
                .andExpect(status().isOk())
                .andExpect(content().string("Usuario admin ya existe"));
    }

    @Test
    @DisplayName("POST /api/auth/login - Debe incluir información del rol en la respuesta")
    void testLogin_DebeIncluirInformacionDelRol() throws Exception {
        // Arrange
        Map<String, String> credentials = new HashMap<>();
        credentials.put("nombre", "testuser");
        credentials.put("contrasena", "password123");

        // Act & Assert
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        LoginResponseDTO response = objectMapper.readValue(responseBody, LoginResponseDTO.class);

        assertThat(response.getRolNombre()).isEqualTo("Administrador");
        assertThat(response.getRolId()).isEqualTo("rol-admin-test");
        assertThat(response.getUsuario()).isEqualTo("testuser");
        assertThat(response.getExpiresIn()).isEqualTo(86400000L); // 24 horas
    }

    @Test
    @DisplayName("POST /api/auth/login - El token generado debe contener userId en los claims")
    void testLogin_TokenDebeContenerUserId() throws Exception {
        // Arrange
        Map<String, String> credentials = new HashMap<>();
        credentials.put("nombre", "testuser");
        credentials.put("contrasena", "password123");

        // Act
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        LoginResponseDTO response = objectMapper.readValue(responseBody, LoginResponseDTO.class);

        // Assert - Verificar claims del token
        String userId = jwtService.extractUserId(response.getToken());
        assertThat(userId).isEqualTo("usuario-activo-test");
    }
}
