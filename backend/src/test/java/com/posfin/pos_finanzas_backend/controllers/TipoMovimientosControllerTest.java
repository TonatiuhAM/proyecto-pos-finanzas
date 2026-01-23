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
@DisplayName("Tests de TipoMovimientosController")
class TipoMovimientosControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JwtService jwtService;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private TIpoMovimientosRepository tipoMovimientosRepository;
    @Autowired private UsuariosRepository usuariosRepository;
    @Autowired private RolesRepository rolesRepository;
    @Autowired private EstadosRepository estadosRepository;
    private String jwtToken;
    private Usuarios usuarioAdmin;
    private TipoMovimientos tipoMovTest1;
    private TipoMovimientos tipoMovTest2;

    @BeforeEach
    void setUp() {
        tipoMovimientosRepository.findById("tipo-mov-test-1").ifPresent(tipoMovimientosRepository::delete);
        tipoMovimientosRepository.findById("tipo-mov-test-2").ifPresent(tipoMovimientosRepository::delete);
        tipoMovimientosRepository.findById("tipo-mov-created").ifPresent(tipoMovimientosRepository::delete);
        usuariosRepository.findByNombre("tipomov-test-admin").ifPresent(usuariosRepository::delete);

        Estados estadoActivo = estadosRepository.findById("estado-activo-test-tipomov")
                .orElseGet(() -> {
                    Estados estado = new Estados();
                    estado.setId("estado-activo-test-tipomov");
                    estado.setEstado("Activo");
                    return estadosRepository.save(estado);
                });

        Roles rolAdmin = rolesRepository.findById("rol-admin-test-tipomov")
                .orElseGet(() -> {
                    Roles rol = new Roles();
                    rol.setId("rol-admin-test-tipomov");
                    rol.setRoles("Administrador");
                    return rolesRepository.save(rol);
                });

        usuarioAdmin = new Usuarios();
        usuarioAdmin.setId("usuario-admin-test-tipomov");
        usuarioAdmin.setNombre("tipomov-test-admin");
        usuarioAdmin.setContrasena(passwordEncoder.encode("password123"));
        usuarioAdmin.setTelefono("1234567890");
        usuarioAdmin.setEstados(estadoActivo);
        usuarioAdmin.setRoles(rolAdmin);
        usuarioAdmin = usuariosRepository.save(usuarioAdmin);
        jwtToken = jwtService.generateToken(usuarioAdmin.getNombre(), usuarioAdmin.getId());

        tipoMovTest1 = new TipoMovimientos();
        tipoMovTest1.setId("tipo-mov-test-1");
        tipoMovTest1.setMovimiento("Entrada Test");
        tipoMovTest1 = tipoMovimientosRepository.save(tipoMovTest1);

        tipoMovTest2 = new TipoMovimientos();
        tipoMovTest2.setId("tipo-mov-test-2");
        tipoMovTest2.setMovimiento("Salida Test");
        tipoMovTest2 = tipoMovimientosRepository.save(tipoMovTest2);
    }

    @Test
    @DisplayName("GET /api/tipo-movimientos - Debe retornar lista")
    void testObtenerTodos() throws Exception {
        Usuarios usr = usuariosRepository.findById(usuarioAdmin.getId()).orElse(null);
        String token = jwtService.generateToken(usr.getNombre(), usr.getId());
        mockMvc.perform(get("/api/tipo-movimientos").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(2)));
    }

    @Test
    @DisplayName("GET /api/tipo-movimientos/{id} - Debe retornar por ID")
    void testObtenerPorId() throws Exception {
        mockMvc.perform(get("/api/tipo-movimientos/{id}", tipoMovTest1.getId())
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tipoMovTest1.getId()))
                .andExpect(jsonPath("$.movimiento").value("Entrada Test"));
    }

    @Test
    @DisplayName("GET /api/tipo-movimientos/{id} - 404 cuando no existe")
    void testObtenerPorId_NoExiste() throws Exception {
        mockMvc.perform(get("/api/tipo-movimientos/{id}", "id-no-existe")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/tipo-movimientos - Debe crear")
    void testCrear() throws Exception {
        TipoMovimientos nuevo = new TipoMovimientos();
        nuevo.setId("tipo-mov-created");
        nuevo.setMovimiento("Ajuste Test");
        mockMvc.perform(post("/api/tipo-movimientos")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("tipo-mov-created"));
        assertThat(tipoMovimientosRepository.findById("tipo-mov-created")).isPresent();
        tipoMovimientosRepository.deleteById("tipo-mov-created");
    }

    @Test
    @DisplayName("PUT /api/tipo-movimientos/{id} - Debe actualizar")
    void testActualizar() throws Exception {
        TipoMovimientos actualizado = new TipoMovimientos();
        actualizado.setMovimiento("Entrada Actualizada");
        mockMvc.perform(put("/api/tipo-movimientos/{id}", tipoMovTest1.getId())
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.movimiento").value("Entrada Actualizada"));
    }

    @Test
    @DisplayName("PUT /api/tipo-movimientos/{id} - 404 cuando no existe")
    void testActualizar_NoExiste() throws Exception {
        TipoMovimientos actualizado = new TipoMovimientos();
        actualizado.setMovimiento("Test");
        mockMvc.perform(put("/api/tipo-movimientos/{id}", "id-no-existe")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizado)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /api/tipo-movimientos/{id} - Debe actualizar parcial")
    void testActualizarParcial() throws Exception {
        Map<String, Object> updates = new HashMap<>();
        updates.put("movimiento", "Entrada Parcial");
        mockMvc.perform(patch("/api/tipo-movimientos/{id}", tipoMovTest1.getId())
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.movimiento").value("Entrada Parcial"));
    }

    @Test
    @DisplayName("PATCH /api/tipo-movimientos/{id} - 404 cuando no existe")
    void testActualizarParcial_NoExiste() throws Exception {
        Map<String, Object> updates = new HashMap<>();
        updates.put("movimiento", "Test");
        mockMvc.perform(patch("/api/tipo-movimientos/{id}", "id-no-existe")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/tipo-movimientos/{id} - Debe eliminar")
    void testEliminar() throws Exception {
        mockMvc.perform(delete("/api/tipo-movimientos/{id}", tipoMovTest2.getId())
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());
        assertThat(tipoMovimientosRepository.findById(tipoMovTest2.getId())).isEmpty();
    }

    @Test
    @DisplayName("DELETE /api/tipo-movimientos/{id} - 404 cuando no existe")
    void testEliminar_NoExiste() throws Exception {
        mockMvc.perform(delete("/api/tipo-movimientos/{id}", "id-no-existe")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/tipo-movimientos - Sin auth 401")
    void testSinAuth() throws Exception {
        mockMvc.perform(get("/api/tipo-movimientos")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/tipo-movimientos - Sin auth 401")
    void testCrearSinAuth() throws Exception {
        mockMvc.perform(post("/api/tipo-movimientos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")).andExpect(status().isUnauthorized());
    }
}
