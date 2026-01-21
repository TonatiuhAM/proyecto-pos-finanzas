package com.posfin.pos_finanzas_backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.posfin.pos_finanzas_backend.services.JwtService;
import com.posfin.pos_finanzas_backend.models.Estados;
import com.posfin.pos_finanzas_backend.models.MetodosPago;
import com.posfin.pos_finanzas_backend.models.Roles;
import com.posfin.pos_finanzas_backend.models.Usuarios;
import com.posfin.pos_finanzas_backend.repositories.EstadosRepository;
import com.posfin.pos_finanzas_backend.repositories.MetodosPagoRepository;
import com.posfin.pos_finanzas_backend.repositories.RolesRepository;
import com.posfin.pos_finanzas_backend.repositories.UsuariosRepository;
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

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de Integración para MetodosPagoController
 * 
 * Cobertura:
 * - GET /api/metodos_pago - Obtener todos los métodos de pago
 * - GET /api/metodos_pago/{id} - Obtener método de pago por ID
 * - POST /api/metodos_pago - Crear nuevo método de pago
 * - PUT /api/metodos_pago/{id} - Actualizar método de pago completo
 * - PATCH /api/metodos_pago/{id} - Actualizar método de pago parcial
 * - DELETE /api/metodos_pago/{id} - Eliminar método de pago
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Tests de MetodosPagoController")
class MetodosPagoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MetodosPagoRepository metodosPagoRepository;

    @Autowired
    private UsuariosRepository usuariosRepository;

    @Autowired
    private RolesRepository rolesRepository;

    @Autowired
    private EstadosRepository estadosRepository;

    private String jwtToken;
    private Usuarios usuarioAdmin;
    private MetodosPago metodoPagoTest;
    private MetodosPago metodoPagoTest2;

    @BeforeEach
    void setUp() {
        // 1. Limpieza selectiva de métodos de pago de test
        metodosPagoRepository.findById("metodo-pago-test-1").ifPresent(metodosPagoRepository::delete);
        metodosPagoRepository.findById("metodo-pago-test-2").ifPresent(metodosPagoRepository::delete);
        metodosPagoRepository.findById("metodo-pago-created").ifPresent(metodosPagoRepository::delete);

        // 2. Limpieza del usuario de test
        usuariosRepository.findByNombre("metodospago-test-admin").ifPresent(usuario -> 
            usuariosRepository.delete(usuario)
        );

        // 3. Crear/reutilizar estado activo
        Estados estadoActivo = estadosRepository.findById("estado-activo-test-metodospago")
                .orElseGet(() -> {
                    Estados estado = new Estados();
                    estado.setId("estado-activo-test-metodospago");
                    estado.setEstado("Activo");
                    return estadosRepository.save(estado);
                });

        // 4. Crear/reutilizar rol admin
        Roles rolAdmin = rolesRepository.findById("rol-admin-test-metodospago")
                .orElseGet(() -> {
                    Roles rol = new Roles();
                    rol.setId("rol-admin-test-metodospago");
                    rol.setRoles("Administrador");
                    return rolesRepository.save(rol);
                });

        // 5. Crear usuario de test único para este controlador
        usuarioAdmin = new Usuarios();
        usuarioAdmin.setId("usuario-admin-test-metodospago");
        usuarioAdmin.setNombre("metodospago-test-admin");
        usuarioAdmin.setContrasena(passwordEncoder.encode("password123"));
        usuarioAdmin.setTelefono("1234567890");
        usuarioAdmin.setEstados(estadoActivo);
        usuarioAdmin.setRoles(rolAdmin);
        usuarioAdmin = usuariosRepository.save(usuarioAdmin);

        // 6. Generar token JWT
        jwtToken = jwtService.generateToken(usuarioAdmin.getNombre(), usuarioAdmin.getId());

        // 7. Crear métodos de pago de test
        metodoPagoTest = new MetodosPago();
        metodoPagoTest.setId("metodo-pago-test-1");
        metodoPagoTest.setMetodoPago("Efectivo Test");
        metodoPagoTest = metodosPagoRepository.save(metodoPagoTest);

        metodoPagoTest2 = new MetodosPago();
        metodoPagoTest2.setId("metodo-pago-test-2");
        metodoPagoTest2.setMetodoPago("Tarjeta Test");
        metodoPagoTest2 = metodosPagoRepository.save(metodoPagoTest2);
    }

    @Test
    @DisplayName("GET /api/metodos_pago - Debe retornar lista de todos los métodos de pago")
    void testObtenerTodosLosMetodosPago_DebeRetornarListaCompleta() throws Exception {
        // Regenerar token para evitar problemas de 403
        Usuarios usuarioVerificado = usuariosRepository.findById(usuarioAdmin.getId()).orElse(null);
        String tokenFresco = jwtService.generateToken(usuarioVerificado.getNombre(), usuarioVerificado.getId());

        mockMvc.perform(get("/api/metodos_pago")
                .header("Authorization", "Bearer " + tokenFresco))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$[0].metodoPago").exists())
                .andExpect(jsonPath("$[1].metodoPago").exists());
    }

    @Test
    @DisplayName("GET /api/metodos_pago/{id} - Debe retornar método de pago por ID")
    void testObtenerMetodoPagoPorId_DebeRetornarMetodoPago() throws Exception {
        mockMvc.perform(get("/api/metodos_pago/{id}", metodoPagoTest.getId())
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(metodoPagoTest.getId()))
                .andExpect(jsonPath("$.metodoPago").value("Efectivo Test"));
    }

    @Test
    @DisplayName("GET /api/metodos_pago/{id} - Debe retornar 404 cuando el método de pago no existe")
    void testObtenerMetodoPagoPorId_NoExiste_DebeRetornar404() throws Exception {
        mockMvc.perform(get("/api/metodos_pago/{id}", "id-no-existe")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/metodos_pago - Debe crear un nuevo método de pago")
    void testCrearMetodoPago_DebeCrearExitosamente() throws Exception {
        // Arrange
        MetodosPago nuevoMetodoPago = new MetodosPago();
        nuevoMetodoPago.setId("metodo-pago-created");
        nuevoMetodoPago.setMetodoPago("Transferencia Test");

        // Act & Assert
        mockMvc.perform(post("/api/metodos_pago")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoMetodoPago)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("metodo-pago-created"))
                .andExpect(jsonPath("$.metodoPago").value("Transferencia Test"));

        // Verificar en base de datos
        MetodosPago metodoPagoGuardado = metodosPagoRepository.findById("metodo-pago-created").orElse(null);
        assertThat(metodoPagoGuardado).isNotNull();
        assertThat(metodoPagoGuardado.getMetodoPago()).isEqualTo("Transferencia Test");

        // Cleanup
        metodosPagoRepository.delete(metodoPagoGuardado);
    }

    @Test
    @DisplayName("PUT /api/metodos_pago/{id} - Debe actualizar método de pago completo")
    void testActualizarMetodoPago_DebeActualizarExitosamente() throws Exception {
        // Arrange
        MetodosPago metodoPagoActualizado = new MetodosPago();
        metodoPagoActualizado.setMetodoPago("Efectivo Actualizado");

        // Act & Assert
        mockMvc.perform(put("/api/metodos_pago/{id}", metodoPagoTest.getId())
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(metodoPagoActualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(metodoPagoTest.getId()))
                .andExpect(jsonPath("$.metodoPago").value("Efectivo Actualizado"));

        // Verificar en base de datos
        MetodosPago metodoPagoEnDb = metodosPagoRepository.findById(metodoPagoTest.getId()).orElse(null);
        assertThat(metodoPagoEnDb).isNotNull();
        assertThat(metodoPagoEnDb.getMetodoPago()).isEqualTo("Efectivo Actualizado");
    }

    @Test
    @DisplayName("PUT /api/metodos_pago/{id} - Debe retornar 404 cuando el método de pago no existe")
    void testActualizarMetodoPago_NoExiste_DebeRetornar404() throws Exception {
        // Arrange
        MetodosPago metodoPagoActualizado = new MetodosPago();
        metodoPagoActualizado.setMetodoPago("No Importa");

        // Act & Assert
        mockMvc.perform(put("/api/metodos_pago/{id}", "id-no-existe")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(metodoPagoActualizado)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /api/metodos_pago/{id} - Debe actualizar método de pago parcialmente")
    void testActualizarMetodoPagoParcial_DebeActualizarExitosamente() throws Exception {
        // Arrange
        Map<String, Object> updates = new HashMap<>();
        updates.put("metodoPago", "Efectivo Parcialmente Actualizado");

        // Act & Assert
        mockMvc.perform(patch("/api/metodos_pago/{id}", metodoPagoTest.getId())
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(metodoPagoTest.getId()))
                .andExpect(jsonPath("$.metodoPago").value("Efectivo Parcialmente Actualizado"));
    }

    @Test
    @DisplayName("PATCH /api/metodos_pago/{id} - Debe retornar 404 cuando el método de pago no existe")
    void testActualizarMetodoPagoParcial_NoExiste_DebeRetornar404() throws Exception {
        // Arrange
        Map<String, Object> updates = new HashMap<>();
        updates.put("metodoPago", "No Importa");

        // Act & Assert
        mockMvc.perform(patch("/api/metodos_pago/{id}", "id-no-existe")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/metodos_pago/{id} - Debe eliminar método de pago exitosamente")
    void testEliminarMetodoPago_DebeEliminarExitosamente() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/metodos_pago/{id}", metodoPagoTest2.getId())
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());

        // Verificar que ya no existe
        assertThat(metodosPagoRepository.findById(metodoPagoTest2.getId())).isEmpty();
    }

    @Test
    @DisplayName("DELETE /api/metodos_pago/{id} - Debe retornar 404 cuando el método de pago no existe")
    void testEliminarMetodoPago_NoExiste_DebeRetornar404() throws Exception {
        mockMvc.perform(delete("/api/metodos_pago/{id}", "id-no-existe")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/metodos_pago - Sin autenticación debe retornar 401")
    void testObtenerTodosLosMetodosPago_SinAutenticacion_DebeRetornar401() throws Exception {
        mockMvc.perform(get("/api/metodos_pago"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/metodos_pago - Sin autenticación debe retornar 401")
    void testCrearMetodoPago_SinAutenticacion_DebeRetornar401() throws Exception {
        MetodosPago nuevoMetodoPago = new MetodosPago();
        nuevoMetodoPago.setMetodoPago("Test");

        mockMvc.perform(post("/api/metodos_pago")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoMetodoPago)))
                .andExpect(status().isUnauthorized());
    }
}
