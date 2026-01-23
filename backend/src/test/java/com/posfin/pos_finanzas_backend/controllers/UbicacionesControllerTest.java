package com.posfin.pos_finanzas_backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.posfin.pos_finanzas_backend.services.JwtService;
import com.posfin.pos_finanzas_backend.models.Estados;
import com.posfin.pos_finanzas_backend.models.Roles;
import com.posfin.pos_finanzas_backend.models.Ubicaciones;
import com.posfin.pos_finanzas_backend.models.Usuarios;
import com.posfin.pos_finanzas_backend.repositories.EstadosRepository;
import com.posfin.pos_finanzas_backend.repositories.RolesRepository;
import com.posfin.pos_finanzas_backend.repositories.UbicacionesRepository;
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
@DisplayName("Tests de UbicacionesController")
class UbicacionesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UbicacionesRepository ubicacionesRepository;

    @Autowired
    private UsuariosRepository usuariosRepository;

    @Autowired
    private RolesRepository rolesRepository;

    @Autowired
    private EstadosRepository estadosRepository;

    private String jwtToken;
    private Usuarios usuarioAdmin;
    private Ubicaciones ubicacionTest1;
    private Ubicaciones ubicacionTest2;

    @BeforeEach
    void setUp() {
        ubicacionesRepository.findById("ubicacion-test-1").ifPresent(ubicacionesRepository::delete);
        ubicacionesRepository.findById("ubicacion-test-2").ifPresent(ubicacionesRepository::delete);
        ubicacionesRepository.findById("ubicacion-created").ifPresent(ubicacionesRepository::delete);

        usuariosRepository.findByNombre("ubicaciones-test-admin").ifPresent(usuariosRepository::delete);

        Estados estadoActivo = estadosRepository.findById("estado-activo-test-ubicaciones")
                .orElseGet(() -> {
                    Estados estado = new Estados();
                    estado.setId("estado-activo-test-ubicaciones");
                    estado.setEstado("Activo");
                    return estadosRepository.save(estado);
                });

        Roles rolAdmin = rolesRepository.findById("rol-admin-test-ubicaciones")
                .orElseGet(() -> {
                    Roles rol = new Roles();
                    rol.setId("rol-admin-test-ubicaciones");
                    rol.setRoles("Administrador");
                    return rolesRepository.save(rol);
                });

        usuarioAdmin = new Usuarios();
        usuarioAdmin.setId("usuario-admin-test-ubicaciones");
        usuarioAdmin.setNombre("ubicaciones-test-admin");
        usuarioAdmin.setContrasena(passwordEncoder.encode("password123"));
        usuarioAdmin.setTelefono("1234567890");
        usuarioAdmin.setEstados(estadoActivo);
        usuarioAdmin.setRoles(rolAdmin);
        usuarioAdmin = usuariosRepository.save(usuarioAdmin);

        jwtToken = jwtService.generateToken(usuarioAdmin.getNombre(), usuarioAdmin.getId());

        ubicacionTest1 = new Ubicaciones();
        ubicacionTest1.setId("ubicacion-test-1");
        ubicacionTest1.setNombre("Almacen Test 1");
        ubicacionTest1.setUbicacion("Pasillo A1");
        ubicacionTest1 = ubicacionesRepository.save(ubicacionTest1);

        ubicacionTest2 = new Ubicaciones();
        ubicacionTest2.setId("ubicacion-test-2");
        ubicacionTest2.setNombre("Almacen Test 2");
        ubicacionTest2.setUbicacion("Pasillo B2");
        ubicacionTest2 = ubicacionesRepository.save(ubicacionTest2);
    }

    @Test
    @DisplayName("GET /api/ubicaciones - Debe retornar lista de todas las ubicaciones")
    void testObtenerTodasLasUbicaciones() throws Exception {
        Usuarios usuarioVerificado = usuariosRepository.findById(usuarioAdmin.getId()).orElse(null);
        String tokenFresco = jwtService.generateToken(usuarioVerificado.getNombre(), usuarioVerificado.getId());

        mockMvc.perform(get("/api/ubicaciones")
                .header("Authorization", "Bearer " + tokenFresco))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(2)));
    }

    @Test
    @DisplayName("GET /api/ubicaciones/{id} - Debe retornar ubicacion por ID")
    void testObtenerUbicacionPorId() throws Exception {
        mockMvc.perform(get("/api/ubicaciones/{id}", ubicacionTest1.getId())
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ubicacionTest1.getId()))
                .andExpect(jsonPath("$.nombre").value("Almacen Test 1"))
                .andExpect(jsonPath("$.ubicacion").value("Pasillo A1"));
    }

    @Test
    @DisplayName("GET /api/ubicaciones/{id} - Debe retornar 404 cuando no existe")
    void testObtenerUbicacionPorId_NoExiste() throws Exception {
        mockMvc.perform(get("/api/ubicaciones/{id}", "id-no-existe")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/ubicaciones - Debe crear una nueva ubicacion")
    void testCrearUbicacion() throws Exception {
        Ubicaciones nuevaUbicacion = new Ubicaciones();
        nuevaUbicacion.setId("ubicacion-created");
        nuevaUbicacion.setNombre("Almacen Nuevo");
        nuevaUbicacion.setUbicacion("Pasillo C3");

        mockMvc.perform(post("/api/ubicaciones")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevaUbicacion)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("ubicacion-created"))
                .andExpect(jsonPath("$.nombre").value("Almacen Nuevo"))
                .andExpect(jsonPath("$.ubicacion").value("Pasillo C3"));

        Ubicaciones ubicacionGuardada = ubicacionesRepository.findById("ubicacion-created").orElse(null);
        assertThat(ubicacionGuardada).isNotNull();
        assertThat(ubicacionGuardada.getNombre()).isEqualTo("Almacen Nuevo");

        ubicacionesRepository.delete(ubicacionGuardada);
    }

    @Test
    @DisplayName("PUT /api/ubicaciones/{id} - Debe actualizar ubicacion completa")
    void testActualizarUbicacion() throws Exception {
        Ubicaciones ubicacionActualizada = new Ubicaciones();
        ubicacionActualizada.setNombre("Almacen Actualizado");
        ubicacionActualizada.setUbicacion("Pasillo A2");

        mockMvc.perform(put("/api/ubicaciones/{id}", ubicacionTest1.getId())
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ubicacionActualizada)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ubicacionTest1.getId()))
                .andExpect(jsonPath("$.nombre").value("Almacen Actualizado"))
                .andExpect(jsonPath("$.ubicacion").value("Pasillo A2"));

        Ubicaciones ubicacionEnDb = ubicacionesRepository.findById(ubicacionTest1.getId()).orElse(null);
        assertThat(ubicacionEnDb).isNotNull();
        assertThat(ubicacionEnDb.getNombre()).isEqualTo("Almacen Actualizado");
    }

    @Test
    @DisplayName("PUT /api/ubicaciones/{id} - Debe retornar 404 cuando no existe")
    void testActualizarUbicacion_NoExiste() throws Exception {
        Ubicaciones ubicacionActualizada = new Ubicaciones();
        ubicacionActualizada.setNombre("No Importa");
        ubicacionActualizada.setUbicacion("No Importa");

        mockMvc.perform(put("/api/ubicaciones/{id}", "id-no-existe")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ubicacionActualizada)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /api/ubicaciones/{id} - Debe actualizar ubicacion parcialmente")
    void testActualizarUbicacionParcial() throws Exception {
        Map<String, Object> updates = new HashMap<>();
        updates.put("nombre", "Almacen Parcial");

        mockMvc.perform(patch("/api/ubicaciones/{id}", ubicacionTest1.getId())
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ubicacionTest1.getId()))
                .andExpect(jsonPath("$.nombre").value("Almacen Parcial"));
    }

    @Test
    @DisplayName("PATCH /api/ubicaciones/{id} - Debe retornar 404 cuando no existe")
    void testActualizarUbicacionParcial_NoExiste() throws Exception {
        Map<String, Object> updates = new HashMap<>();
        updates.put("nombre", "No Importa");

        mockMvc.perform(patch("/api/ubicaciones/{id}", "id-no-existe")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/ubicaciones/{id} - Debe eliminar ubicacion exitosamente")
    void testEliminarUbicacion() throws Exception {
        mockMvc.perform(delete("/api/ubicaciones/{id}", ubicacionTest2.getId())
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());

        assertThat(ubicacionesRepository.findById(ubicacionTest2.getId())).isEmpty();
    }

    @Test
    @DisplayName("DELETE /api/ubicaciones/{id} - Debe retornar 404 cuando no existe")
    void testEliminarUbicacion_NoExiste() throws Exception {
        mockMvc.perform(delete("/api/ubicaciones/{id}", "id-no-existe")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/ubicaciones - Sin autenticación debe retornar 401")
    void testObtenerUbicaciones_SinAutenticacion() throws Exception {
        mockMvc.perform(get("/api/ubicaciones"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/ubicaciones - Sin autenticación debe retornar 401")
    void testCrearUbicacion_SinAutenticacion() throws Exception {
        Ubicaciones nuevaUbicacion = new Ubicaciones();
        nuevaUbicacion.setNombre("Test");
        nuevaUbicacion.setUbicacion("Test");

        mockMvc.perform(post("/api/ubicaciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevaUbicacion)))
                .andExpect(status().isUnauthorized());
    }
}
