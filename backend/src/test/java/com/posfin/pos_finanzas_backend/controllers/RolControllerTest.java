package com.posfin.pos_finanzas_backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.posfin.pos_finanzas_backend.services.JwtService;
import com.posfin.pos_finanzas_backend.models.*;
import com.posfin.pos_finanzas_backend.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Tests de RolController")
class RolControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JwtService jwtService;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private RolesRepository rolesRepository;
    @Autowired private UsuariosRepository usuariosRepository;
    @Autowired private EstadosRepository estadosRepository;
    private String jwtToken;
    private Usuarios usuarioAdmin;
    private Roles rolTest1;

    @BeforeEach
    void setUp() {
        usuariosRepository.findByNombre("rol-test-admin").ifPresent(usuariosRepository::delete);

        Estados estadoActivo = estadosRepository.findById("estado-activo-test-rol")
                .orElseGet(() -> {
                    Estados estado = new Estados();
                    estado.setId("estado-activo-test-rol");
                    estado.setEstado("Activo");
                    return estadosRepository.save(estado);
                });

        Roles rolAdmin = rolesRepository.findById("rol-admin-test-rol")
                .orElseGet(() -> {
                    Roles rol = new Roles();
                    rol.setId("rol-admin-test-rol");
                    rol.setRoles("Administrador");
                    return rolesRepository.save(rol);
                });

        rolTest1 = rolesRepository.findById("rol-empleado-test")
                .orElseGet(() -> {
                    Roles rol = new Roles();
                    rol.setId("rol-empleado-test");
                    rol.setRoles("Empleado Test");
                    return rolesRepository.save(rol);
                });

        usuarioAdmin = new Usuarios();
        usuarioAdmin.setId("usuario-admin-test-rol");
        usuarioAdmin.setNombre("rol-test-admin");
        usuarioAdmin.setContrasena(passwordEncoder.encode("password123"));
        usuarioAdmin.setTelefono("1234567890");
        usuarioAdmin.setEstados(estadoActivo);
        usuarioAdmin.setRoles(rolAdmin);
        usuarioAdmin = usuariosRepository.save(usuarioAdmin);
        jwtToken = jwtService.generateToken(usuarioAdmin.getNombre(), usuarioAdmin.getId());
    }

    @Test
    @DisplayName("GET /api/roles - Debe retornar lista de roles")
    void testObtenerTodosLosRoles() throws Exception {
        Usuarios usr = usuariosRepository.findById(usuarioAdmin.getId()).orElse(null);
        String token = jwtService.generateToken(usr.getNombre(), usr.getId());
        mockMvc.perform(get("/api/roles").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(2)));
    }

    @Test
    @DisplayName("GET /api/roles/{id} - Debe retornar rol por ID")
    void testObtenerRolPorId() throws Exception {
        // El controller espera Long pero lo convierte a String internamente
        // Creamos un rol con ID numérico para este test
        Roles rolNumerico = rolesRepository.findById("1")
                .orElseGet(() -> {
                    Roles rol = new Roles();
                    rol.setId("1");
                    rol.setRoles("Rol Test Numérico");
                    return rolesRepository.save(rol);
                });
        
        mockMvc.perform(get("/api/roles/{id}", 1L)
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nombre").exists());
    }

    @Test
    @DisplayName("GET /api/roles/{id} - Debe retornar error cuando no existe")
    void testObtenerRolPorId_NoExiste() throws Exception {
        mockMvc.perform(get("/api/roles/{id}", 999999L)
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/roles - Sin autenticación debe retornar 401")
    void testObtenerRoles_SinAutenticacion() throws Exception {
        mockMvc.perform(get("/api/roles"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/roles/{id} - Sin autenticación debe retornar 401")
    void testObtenerRolPorId_SinAutenticacion() throws Exception {
        mockMvc.perform(get("/api/roles/{id}", 1L))
                .andExpect(status().isUnauthorized());
    }
}
