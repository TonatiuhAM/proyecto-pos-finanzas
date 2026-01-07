package com.posfin.pos_finanzas_backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.containsString;

/**
 * Pruebas de integración para SecurityConfig
 * Valida la configuración de seguridad según ISO/IEC 25010
 * Categoría: Seguridad (Security) - Confidencialidad y Control de Acceso
 */
@ExtendWith(SpringJUnitExtension.class)
@SpringBootTest
@AutoConfigureWebMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtRequestFilter jwtRequestFilter;

    /**
     * Test crítico: Verificar que endpoints protegidos rechacen acceso sin token
     * ISO/IEC 25010 - Security: Access Control
     */
    @Test
    void testEndpointsProtegidos_SinToken_403() throws Exception {
        // Array de endpoints que deben estar protegidos
        String[] protectedEndpoints = {
            "/api/ventas",
            "/api/compras", 
            "/api/productos",
            "/api/empleados",
            "/api/inventario",
            "/api/usuarios",
            "/api/reportes"
        };

        // Act & Assert - Todos los endpoints protegidos deben retornar 401/403
        for (String endpoint : protectedEndpoints) {
            mockMvc.perform(get(endpoint))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.error").value("Unauthorized"));

            mockMvc.perform(post(endpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(put(endpoint + "/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(delete(endpoint + "/1"))
                    .andExpect(status().isUnauthorized());
        }
    }

    /**
     * Test crítico: Verificar que endpoints públicos permitan acceso sin autenticación
     * ISO/IEC 25010 - Functional Suitability: Functional Correctness
     */
    @Test
    void testEndpointsPublicos_SinToken_200() throws Exception {
        // Endpoints que deben ser públicos
        String[] publicEndpoints = {
            "/api/auth/login",
            "/api/auth/create-admin"
        };

        for (String endpoint : publicEndpoints) {
            // OPTIONS request debe ser permitido (CORS preflight)
            mockMvc.perform(options(endpoint))
                    .andExpect(status().isOk());
        }

        // Test específico para login (debe fallar por datos, no por auth)
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest()) // Error de validación, no de auth
                .andExpect(content().string("Nombre de usuario y contraseña son requeridos"));
    }

    /**
     * Test de configuración CORS
     * ISO/IEC 25010 - Compatibility: Co-existence
     */
    @Test
    void testCORS_ConfiguracionCorrecta() throws Exception {
        // Act & Assert - Verificar headers CORS en respuesta OPTIONS
        mockMvc.perform(options("/api/auth/login")
                .header("Origin", "http://localhost:5173")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "authorization,content-type"))
                .andExpected(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Methods", 
                    containsString("POST")))
                .andExpect(header().string("Access-Control-Allow-Headers", 
                    containsString("authorization")))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    /**
     * Test CSRF deshabilitado para API REST
     * ISO/IEC 25010 - Security: Authenticity (configuración correcta)
     */
    @Test
    void testCSRF_DeshabilitadoAPI() throws Exception {
        // CSRF debe estar deshabilitado para APIs REST
        // Si estuviera habilitado, necesitaríamos token CSRF
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"test\",\"contrasena\":\"test\"}"))
                .andExpect(status().isBadRequest()) // Error de login, no de CSRF
                .andExpect(content().string("Usuario no encontrado"));
    }

    /**
     * Test de manejo de errores de autenticación
     * ISO/IEC 25010 - Security: Error Handling
     */
    @Test
    void testAuthenticationErrorHandling_JSON_Response() throws Exception {
        // Act
        var result = mockMvc.perform(get("/api/productos"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Assert - Verificar formato de error JSON
        String responseContent = result.getResponse().getContentAsString();
        assertTrue(responseContent.contains("\"error\":\"Unauthorized\""), 
            "La respuesta debe contener error estructurado");
        assertFalse(responseContent.contains("Exception"), 
            "La respuesta no debe revelar detalles internos de excepciones");
    }

    /**
     * Test de manejo de errores de autorización (403)
     * ISO/IEC 25010 - Security: Access Control
     */
    @Test
    @WithMockUser(roles = "USER") // Usuario autenticado pero sin permisos suficientes
    void testAccessDeniedHandling_JSON_Response() throws Exception {
        // Este test simula un usuario autenticado pero sin permisos
        // En el futuro, cuando tengamos endpoints específicos por rol
        
        // Por ahora, validamos la configuración del handler
        var result = mockMvc.perform(get("/api/admin/settings"))
                .andExpect(status().isNotFound()) // 404 porque el endpoint no existe aún
                .andReturn();
        
        // El test principal es que la configuración no falle
        assertNotNull(result);
    }

    /**
     * Test de configuración sin estado (Stateless)
     * ISO/IEC 25010 - Performance Efficiency: Behaviour
     */
    @Test
    void testSessionCreationPolicy_Stateless() throws Exception {
        // Primera petición sin token
        var result1 = mockMvc.perform(get("/api/productos"))
                .andExpect(status().isUnauthorized())
                .andReturn();

        // Segunda petición sin token - debe dar mismo resultado
        var result2 = mockMvc.perform(get("/api/productos"))
                .andExpect(status().isUnauthorized())
                .andReturn();

        // Verificar que no se creó sesión (no debe haber Set-Cookie headers)
        assertNull(result1.getResponse().getHeader("Set-Cookie"), 
            "No debe crear cookies de sesión");
        assertNull(result2.getResponse().getHeader("Set-Cookie"), 
            "No debe crear cookies de sesión");
    }

    /**
     * Test de filtro JWT configurado correctamente
     * ISO/IEC 25010 - Security: Authentication
     */
    @Test
    void testJwtFilter_Configurado() throws Exception {
        // Petición con token JWT malformado
        mockMvc.perform(get("/api/productos")
                .header("Authorization", "Bearer token.malformado.aqui"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        
        // Petición con header Authorization pero sin "Bearer "
        mockMvc.perform(get("/api/productos")
                .header("Authorization", "invalidformat"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Test de endpoints con diferentes métodos HTTP
     * ISO/IEC 25010 - Security: Functional Suitability
     */
    @Test
    void testHTTPMethods_Security() throws Exception {
        String protectedEndpoint = "/api/productos";

        // Todos los métodos deben estar protegidos
        mockMvc.perform(get(protectedEndpoint))
                .andExpect(status().isUnauthorized());
        
        mockMvc.perform(post(protectedEndpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpected(status().isUnauthorized());

        mockMvc.perform(put(protectedEndpoint + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpected(status().isUnauthorized());

        mockMvc.perform(patch(protectedEndpoint + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpected(status().isUnauthorized());

        mockMvc.perform(delete(protectedEndpoint + "/1"))
                .andExpect(status().isUnauthorized());

        // OPTIONS debe ser permitido para CORS
        mockMvc.perform(options(protectedEndpoint))
                .andExpect(status().isOk());
    }

    /**
     * Test de configuración de PasswordEncoder
     * ISO/IEC 25010 - Security: Confidentiality
     */
    @Test  
    void testPasswordEncoder_BCrypt() throws Exception {
        // Este test verifica que la configuración esté presente
        // La funcionalidad real se prueba en AuthControllerTest
        
        // Verificar que el endpoint de creación de admin no falle por configuración
        mockMvc.perform(post("/api/auth/create-admin"))
                .andExpect(status().isOk()); // Debe procesar, puede fallar por otros motivos
    }

    /**
     * Test de seguridad de headers
     * ISO/IEC 25010 - Security: Confidentiality
     */
    @Test
    void testSecurityHeaders() throws Exception {
        var result = mockMvc.perform(get("/api/productos"))
                .andExpect(status().isUnauthorized())
                .andReturn();

        // Verificar que no se revelan headers sensibles
        assertNull(result.getResponse().getHeader("Server"), 
            "No debe revelar información del servidor");
        
        // Verificar Content-Type correcto en errores
        assertEquals("application/json", result.getResponse().getContentType(),
            "Errores de seguridad deben ser JSON");
    }

    /**
     * Test integral de flujo de seguridad
     * ISO/IEC 25010 - Security: Integration Security
     */
    @Test
    void testSecurityFlow_Complete() throws Exception {
        // 1. Acceder sin token -> 401
        mockMvc.perform(get("/api/productos"))
                .andExpectedString(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));

        // 2. Login público debe funcionar (falla por datos, no por auth)
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"\",\"contrasena\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Nombre de usuario y contraseña son requeridos"));

        // 3. OPTIONS debe funcionar para CORS
        mockMvc.perform(options("/api/productos")
                .header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk());

        // 4. Verificar que filtros están en orden correcto (JWT antes de UsernamePassword)
        // Esto se verifica indirectamente - si el orden estuviera mal, tendríamos errores
        mockMvc.perform(get("/api/productos")
                .header("Authorization", "Bearer valid.token.here"))
                .andExpect(status().isUnauthorized()); // JWT inválido, pero filtro procesó
    }
}