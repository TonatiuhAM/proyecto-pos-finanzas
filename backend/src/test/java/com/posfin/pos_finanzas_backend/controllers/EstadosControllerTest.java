package com.posfin.pos_finanzas_backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.posfin.pos_finanzas_backend.services.JwtService;
import com.posfin.pos_finanzas_backend.models.Estados;
import com.posfin.pos_finanzas_backend.models.Roles;
import com.posfin.pos_finanzas_backend.models.Usuarios;
import com.posfin.pos_finanzas_backend.repositories.EstadosRepository;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Tests de EstadosController")
class EstadosControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EstadosRepository estadosRepository;

    @Autowired
    private UsuariosRepository usuariosRepository;

    @Autowired
    private RolesRepository rolesRepository;

    private String jwtToken;
    private Usuarios usuarioAdmin;
    private Estados estadoTest1;
    private Estados estadoTest2;

    @BeforeEach
    void setUp() {
        // Limpieza selectiva de estados de test
        estadosRepository.findById("estado-test-1").ifPresent(estadosRepository::delete);
        estadosRepository.findById("estado-test-2").ifPresent(estadosRepository::delete);
        estadosRepository.findById("estado-created").ifPresent(estadosRepository::delete);

        // Limpieza del usuario de test
        usuariosRepository.findByNombre("estados-test-admin").ifPresent(usuariosRepository::delete);

        // Crear/reutilizar estado activo para usuario
        Estados estadoActivo = estadosRepository.findById("estado-activo-test-estados")
                .orElseGet(() -> {
                    Estados estado = new Estados();
                    estado.setId("estado-activo-test-estados");
                    estado.setEstado("Activo");
                    return estadosRepository.save(estado);
                });

        // Crear/reutilizar rol admin
        Roles rolAdmin = rolesRepository.findById("rol-admin-test-estados")
                .orElseGet(() -> {
                    Roles rol = new Roles();
                    rol.setId("rol-admin-test-estados");
                    rol.setRoles("Administrador");
                    return rolesRepository.save(rol);
                });

        // Crear usuario de test
        usuarioAdmin = new Usuarios();
        usuarioAdmin.setId("usuario-admin-test-estados");
        usuarioAdmin.setNombre("estados-test-admin");
        usuarioAdmin.setContrasena(passwordEncoder.encode("password123"));
        usuarioAdmin.setTelefono("1234567890");
        usuarioAdmin.setEstados(estadoActivo);
        usuarioAdmin.setRoles(rolAdmin);
        usuarioAdmin = usuariosRepository.save(usuarioAdmin);

        // Generar token JWT
        jwtToken = jwtService.generateToken(usuarioAdmin.getNombre(), usuarioAdmin.getId());

        // Crear estados de test
        estadoTest1 = new Estados();
        estadoTest1.setId("estado-test-1");
        estadoTest1.setEstado("Pendiente Test");
        estadoTest1 = estadosRepository.save(estadoTest1);

        estadoTest2 = new Estados();
        estadoTest2.setId("estado-test-2");
        estadoTest2.setEstado("Completado Test");
        estadoTest2 = estadosRepository.save(estadoTest2);
    }

    @Test
    @DisplayName("GET /api/estados - Debe retornar lista de todos los estados")
    void testObtenerTodosLosEstados() throws Exception {
        Usuarios usuarioVerificado = usuariosRepository.findById(usuarioAdmin.getId()).orElse(null);
        String tokenFresco = jwtService.generateToken(usuarioVerificado.getNombre(), usuarioVerificado.getId());

        mockMvc.perform(get("/api/estados")
                .header("Authorization", "Bearer " + tokenFresco))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(2)));
    }

    @Test
    @DisplayName("GET /api/estados/{id} - Debe retornar estado por ID")
    void testObtenerEstadoPorId() throws Exception {
        mockMvc.perform(get("/api/estados/{id}", estadoTest1.getId())
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(estadoTest1.getId()))
                .andExpect(jsonPath("$.estado").value("Pendiente Test"));
    }

    @Test
    @DisplayName("GET /api/estados/{id} - Debe retornar 404 cuando el estado no existe")
    void testObtenerEstadoPorId_NoExiste() throws Exception {
        mockMvc.perform(get("/api/estados/{id}", "id-no-existe")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/estados - Debe crear un nuevo estado")
    void testCrearEstado() throws Exception {
        Estados nuevoEstado = new Estados();
        nuevoEstado.setId("estado-created");
        nuevoEstado.setEstado("Nuevo Estado Test");

        mockMvc.perform(post("/api/estados")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoEstado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("estado-created"))
                .andExpect(jsonPath("$.estado").value("Nuevo Estado Test"));

        Estados estadoGuardado = estadosRepository.findById("estado-created").orElse(null);
        assertThat(estadoGuardado).isNotNull();
        assertThat(estadoGuardado.getEstado()).isEqualTo("Nuevo Estado Test");

        estadosRepository.delete(estadoGuardado);
    }

    @Test
    @DisplayName("PUT /api/estados/{id} - Debe actualizar estado completo")
    void testActualizarEstado() throws Exception {
        Estados estadoActualizado = new Estados();
        estadoActualizado.setEstado("Pendiente Actualizado");

        mockMvc.perform(put("/api/estados/{id}", estadoTest1.getId())
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(estadoActualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(estadoTest1.getId()))
                .andExpect(jsonPath("$.estado").value("Pendiente Actualizado"));

        Estados estadoEnDb = estadosRepository.findById(estadoTest1.getId()).orElse(null);
        assertThat(estadoEnDb).isNotNull();
        assertThat(estadoEnDb.getEstado()).isEqualTo("Pendiente Actualizado");
    }

    @Test
    @DisplayName("PUT /api/estados/{id} - Debe retornar 404 cuando el estado no existe")
    void testActualizarEstado_NoExiste() throws Exception {
        Estados estadoActualizado = new Estados();
        estadoActualizado.setEstado("No Importa");

        mockMvc.perform(put("/api/estados/{id}", "id-no-existe")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(estadoActualizado)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /api/estados/{id} - Debe actualizar estado parcialmente")
    void testActualizarEstadoParcial() throws Exception {
        Map<String, Object> updates = new HashMap<>();
        updates.put("estado", "Pendiente Parcial");

        mockMvc.perform(patch("/api/estados/{id}", estadoTest1.getId())
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(estadoTest1.getId()))
                .andExpect(jsonPath("$.estado").value("Pendiente Parcial"));
    }

    @Test
    @DisplayName("PATCH /api/estados/{id} - Debe retornar 404 cuando el estado no existe")
    void testActualizarEstadoParcial_NoExiste() throws Exception {
        Map<String, Object> updates = new HashMap<>();
        updates.put("estado", "No Importa");

        mockMvc.perform(patch("/api/estados/{id}", "id-no-existe")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/estados/{id} - Debe eliminar estado exitosamente")
    void testEliminarEstado() throws Exception {
        mockMvc.perform(delete("/api/estados/{id}", estadoTest2.getId())
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());

        assertThat(estadosRepository.findById(estadoTest2.getId())).isEmpty();
    }

    @Test
    @DisplayName("DELETE /api/estados/{id} - Debe retornar 404 cuando el estado no existe")
    void testEliminarEstado_NoExiste() throws Exception {
        mockMvc.perform(delete("/api/estados/{id}", "id-no-existe")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/estados - Sin autenticación debe retornar 401")
    void testObtenerEstados_SinAutenticacion() throws Exception {
        mockMvc.perform(get("/api/estados"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/estados - Sin autenticación debe retornar 401")
    void testCrearEstado_SinAutenticacion() throws Exception {
        Estados nuevoEstado = new Estados();
        nuevoEstado.setEstado("Test");

        mockMvc.perform(post("/api/estados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoEstado)))
                .andExpect(status().isUnauthorized());
    }
}
