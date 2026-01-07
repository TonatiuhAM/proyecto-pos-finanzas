package com.posfin.pos_finanzas_backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.posfin.pos_finanzas_backend.models.Estados;
import com.posfin.pos_finanzas_backend.models.Roles;
import com.posfin.pos_finanzas_backend.models.Usuarios;
import com.posfin.pos_finanzas_backend.repositories.EstadosRepository;
import com.posfin.pos_finanzas_backend.repositories.RolesRepository;
import com.posfin.pos_finanzas_backend.repositories.UsuariosRepository;
import com.posfin.pos_finanzas_backend.services.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringJUnitExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.containsString;

/**
 * Pruebas de integración para AuthController
 * Valida la autenticación y autorización según ISO/IEC 25010
 * Categorías: Seguridad (Security) y Adecuación Funcional
 */
@ExtendWith(SpringJUnitExtension.class)
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UsuariosRepository usuariosRepository;

    @MockBean
    private EstadosRepository estadosRepository;

    @MockBean
    private RolesRepository rolesRepository;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    // Datos de prueba
    private Usuarios usuarioActivo;
    private Usuarios usuarioInactivo;
    private Estados estadoActivo;
    private Estados estadoInactivo;
    private Roles rolAdmin;
    private final String validToken = "valid.jwt.token";

    @BeforeEach
    void setUp() {
        // Configurar estado activo
        estadoActivo = new Estados();
        estadoActivo.setId("activo-id");
        estadoActivo.setEstado("Activo");

        // Configurar estado inactivo
        estadoInactivo = new Estados();
        estadoInactivo.setId("inactivo-id");
        estadoInactivo.setEstado("Inactivo");

        // Configurar rol admin
        rolAdmin = new Roles();
        rolAdmin.setId("admin-role-id");
        rolAdmin.setRoles("Administrador");

        // Configurar usuario activo
        usuarioActivo = new Usuarios();
        usuarioActivo.setId("user-id-123");
        usuarioActivo.setNombre("testuser");
        usuarioActivo.setTelefono("555-1234");
        usuarioActivo.setContrasena("$2a$10$hashedpassword"); // BCrypt hash simulado
        usuarioActivo.setEstados(estadoActivo);
        usuarioActivo.setRoles(rolAdmin);

        // Configurar usuario inactivo
        usuarioInactivo = new Usuarios();
        usuarioInactivo.setId("inactive-user-id");
        usuarioInactivo.setNombre("inactiveuser");
        usuarioInactivo.setTelefono("555-5678");
        usuarioInactivo.setContrasena("$2a$10$hashedpassword");
        usuarioInactivo.setEstados(estadoInactivo);
        usuarioInactivo.setRoles(rolAdmin);
    }

    @Test
    void testLogin_CredencialesValidas_200() throws Exception {
        // Arrange
        Map<String, String> credentials = new HashMap<>();
        credentials.put("nombre", "testuser");
        credentials.put("contrasena", "password123");

        when(usuariosRepository.findByNombre("testuser"))
                .thenReturn(Optional.of(usuarioActivo));
        when(passwordEncoder.matches("password123", "$2a$10$hashedpassword"))
                .thenReturn(true);
        when(jwtService.generateToken("testuser", "user-id-123"))
                .thenReturn(validToken);

        // Act & Assert
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value(validToken))
                .andExpect(jsonPath("$.usuario").value("testuser"))
                .andExpect(jsonPath("$.rolNombre").value("Administrador"))
                .andExpect(jsonPath("$.rolId").value("admin-role-id"))
                .andExpect(jsonPath("$.expiresIn").value(86400000L)) // 24 horas
                .andReturn();

        // Verificar interacciones
        verify(usuariosRepository).findByNombre("testuser");
        verify(passwordEncoder).matches("password123", "$2a$10$hashedpassword");
        verify(jwtService).generateToken("testuser", "user-id-123");

        // Verificar estructura de respuesta
        String responseContent = result.getResponse().getContentAsString();
        assertTrue(responseContent.contains("token"), "La respuesta debe contener el token");
        assertFalse(responseContent.contains("contrasena"), "La respuesta no debe contener la contraseña");
    }

    @Test
    void testLogin_CredencialesInvalidas_401() throws Exception {
        // Arrange
        Map<String, String> credentials = new HashMap<>();
        credentials.put("nombre", "testuser");
        credentials.put("contrasena", "wrongpassword");

        when(usuariosRepository.findByNombre("testuser"))
                .thenReturn(Optional.of(usuarioActivo));
        when(passwordEncoder.matches("wrongpassword", "$2a$10$hashedpassword"))
                .thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Contraseña incorrecta"));

        // Verificar que no se generó token
        verify(jwtService, never()).generateToken(anyString(), anyString());
    }

    @Test
    void testLogin_UsuarioNoExistente_404() throws Exception {
        // Arrange
        Map<String, String> credentials = new HashMap<>();
        credentials.put("nombre", "nonexistentuser");
        credentials.put("contrasena", "password123");

        when(usuariosRepository.findByNombre("nonexistentuser"))
                .thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Usuario no encontrado"));

        // Verificar que no se verificó contraseña ni se generó token
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generateToken(anyString(), anyString());
    }

    @Test
    void testLogin_UsuarioInactivo_403() throws Exception {
        // Arrange - Usuario inactivo
        Map<String, String> credentials = new HashMap<>();
        credentials.put("nombre", "inactiveuser");
        credentials.put("contrasena", "password123");

        when(usuariosRepository.findByNombre("inactiveuser"))
                .thenReturn(Optional.of(usuarioInactivo));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Usuario inactivo"));

        // Verificar que se validó estado ANTES de verificar contraseña
        verify(usuariosRepository).findByNombre("inactiveuser");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generateToken(anyString(), anyString());
    }

    @Test
    void testLogin_CamposRequeridos_400() throws Exception {
        // Arrange - Casos de campos faltantes
        Map<String, Map<String, String>> testCases = new HashMap<>();
        
        // Sin nombre
        Map<String, String> sinNombre = new HashMap<>();
        sinNombre.put("contrasena", "password123");
        testCases.put("sinNombre", sinNombre);

        // Sin contraseña
        Map<String, String> sinContrasena = new HashMap<>();
        sinContrasena.put("nombre", "testuser");
        testCases.put("sinContrasena", sinContrasena);

        // Campos vacíos
        Map<String, String> camposVacios = new HashMap<>();
        camposVacios.put("nombre", "");
        camposVacios.put("contrasena", "");
        testCases.put("camposVacios", camposVacios);

        // Act & Assert
        for (Map.Entry<String, Map<String, String>> testCase : testCases.entrySet()) {
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testCase.getValue())))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Nombre de usuario y contraseña son requeridos"));
        }

        // Verificar que nunca se accedió al repositorio
        verify(usuariosRepository, never()).findByNombre(anyString());
    }

    @Test
    void testLogin_ContrasenaTextoPlano_Compatibilidad() throws Exception {
        // Arrange - Usuario con contraseña en texto plano (compatibilidad)
        Usuarios usuarioTextoPlano = new Usuarios();
        usuarioTextoPlano.setId("old-user-id");
        usuarioTextoPlano.setNombre("olduser");
        usuarioTextoPlano.setTelefono("555-0000");
        usuarioTextoPlano.setContrasena("plainpassword"); // Sin hash BCrypt
        usuarioTextoPlano.setEstados(estadoActivo);
        usuarioTextoPlano.setRoles(rolAdmin);

        Map<String, String> credentials = new HashMap<>();
        credentials.put("nombre", "olduser");
        credentials.put("contrasena", "plainpassword");

        when(usuariosRepository.findByNombre("olduser"))
                .thenReturn(Optional.of(usuarioTextoPlano));
        when(jwtService.generateToken("olduser", "old-user-id"))
                .thenReturn(validToken);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(validToken))
                .andExpect(jsonPath("$.usuario").value("olduser"));

        // Verificar que no se usó BCrypt para contraseña texto plano
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void testLogin_ErrorManejo_500() throws Exception {
        // Arrange - Simular error en base de datos
        Map<String, String> credentials = new HashMap<>();
        credentials.put("nombre", "testuser");
        credentials.put("contrasena", "password123");

        when(usuariosRepository.findByNombre("testuser"))
                .thenThrow(new RuntimeException("Database connection error"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Error en el login:")));
    }

    @Test
    void testCreateAdmin_UsuarioNoExistente_201() throws Exception {
        // Arrange
        when(usuariosRepository.findByNombre("admin")).thenReturn(Optional.empty());
        when(estadosRepository.findById("activo-id")).thenReturn(Optional.empty());
        when(rolesRepository.findById("admin-role-id")).thenReturn(Optional.empty());
        when(estadosRepository.save(any(Estados.class))).thenReturn(estadoActivo);
        when(rolesRepository.save(any(Roles.class))).thenReturn(rolAdmin);
        when(usuariosRepository.save(any(Usuarios.class))).thenReturn(usuarioActivo);
        when(passwordEncoder.encode("admin")).thenReturn("$2a$10$hashedadminpassword");

        // Act & Assert
        mockMvc.perform(post("/api/auth/create-admin"))
                .andExpect(status().isOk())
                .andExpect(content().string("Usuario admin creado exitosamente"));

        // Verificar interacciones
        verify(usuariosRepository).findByNombre("admin");
        verify(passwordEncoder).encode("admin");
        verify(usuariosRepository).save(any(Usuarios.class));
    }

    @Test
    void testCreateAdmin_UsuarioYaExiste_200() throws Exception {
        // Arrange
        when(usuariosRepository.findByNombre("admin")).thenReturn(Optional.of(usuarioActivo));

        // Act & Assert
        mockMvc.perform(post("/api/auth/create-admin"))
                .andExpect(status().isOk())
                .andExpect(content().string("Usuario admin ya existe"));

        // Verificar que no se creó usuario nuevo
        verify(usuariosRepository, never()).save(any(Usuarios.class));
    }

    @Test
    void testSeguridadFormato_JSON_Obligatorio() throws Exception {
        // Arrange - Enviar datos como form-data en lugar de JSON
        Map<String, String> credentials = new HashMap<>();
        credentials.put("nombre", "testuser");
        credentials.put("contrasena", "password123");

        // Act & Assert - Debe fallar si no es JSON
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("nombre", "testuser")
                .param("contrasena", "password123"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testSeguridadEndpoint_MetodosNoPermitidos() throws Exception {
        // Act & Assert - Solo POST debe estar permitido
        mockMvc.perform(get("/api/auth/login"))
                .andExpect(status().isMethodNotAllowed());

        mockMvc.perform(put("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isMethodNotAllowed());

        mockMvc.perform(delete("/api/auth/login"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void testSeguridadRespuesta_NoLeakInformation() throws Exception {
        // Arrange - Credenciales inválidas
        Map<String, String> credentials = new HashMap<>();
        credentials.put("nombre", "testuser");
        credentials.put("contrasena", "wrongpassword");

        when(usuariosRepository.findByNombre("testuser"))
                .thenReturn(Optional.of(usuarioActivo));
        when(passwordEncoder.matches("wrongpassword", "$2a$10$hashedpassword"))
                .thenReturn(false);

        // Act
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isBadRequest())
                .andReturn();

        // Assert - No debe leakar información sensible
        String response = result.getResponse().getContentAsString();
        assertFalse(response.contains("hashedpassword"), 
            "La respuesta no debe contener hashes de contraseñas");
        assertFalse(response.contains("user-id-123"), 
            "La respuesta no debe contener IDs de usuario en caso de error");
        assertFalse(response.contains("Database"), 
            "La respuesta no debe revelar detalles internos");
    }

    @Test
    void testIntegracionCompleta_LoginExitoso() throws Exception {
        // Arrange
        Map<String, String> credentials = new HashMap<>();
        credentials.put("nombre", "testuser");
        credentials.put("contrasena", "password123");

        when(usuariosRepository.findByNombre("testuser"))
                .thenReturn(Optional.of(usuarioActivo));
        when(passwordEncoder.matches("password123", "$2a$10$hashedpassword"))
                .thenReturn(true);
        when(jwtService.generateToken("testuser", "user-id-123"))
                .thenReturn(validToken);

        // Act
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isOk())
                .andReturn();

        // Assert - Verificar ciclo completo
        String responseJson = result.getResponse().getContentAsString();
        
        // Parsear respuesta JSON
        Map<String, Object> response = objectMapper.readValue(responseJson, Map.class);
        
        // Verificar todos los campos esperados
        assertEquals(validToken, response.get("token"));
        assertEquals("testuser", response.get("usuario"));
        assertEquals("Administrador", response.get("rolNombre"));
        assertEquals("admin-role-id", response.get("rolId"));
        assertEquals(86400000, response.get("expiresIn"));
        
        // Verificar orden de ejecución correcto
        verify(usuariosRepository).findByNombre("testuser");
        verify(passwordEncoder).matches("password123", "$2a$10$hashedpassword");
        verify(jwtService).generateToken("testuser", "user-id-123");
    }
}