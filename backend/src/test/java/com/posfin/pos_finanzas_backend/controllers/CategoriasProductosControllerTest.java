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
@DisplayName("Tests de CategoriasProductosController")
class CategoriasProductosControllerTest {
    
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JwtService jwtService;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private CategoriasProductosRepository categoriasProductosRepository;
    @Autowired private UsuariosRepository usuariosRepository;
    @Autowired private RolesRepository rolesRepository;
    @Autowired private EstadosRepository estadosRepository;
    
    private String jwtToken;
    private Usuarios usuarioAdmin;
    private CategoriasProductos categoriaTest1;
    private CategoriasProductos categoriaTest2;
    
    @BeforeEach
    void setUp() {
        // 1. Selective cleanup
        categoriasProductosRepository.findById("cat-prod-test-1").ifPresent(categoriasProductosRepository::delete);
        categoriasProductosRepository.findById("cat-prod-test-2").ifPresent(categoriasProductosRepository::delete);
        usuariosRepository.findByNombre("catprod-test-admin").ifPresent(usuariosRepository::delete);
        
        // 2. Reuse catalog data with orElseGet()
        Estados estadoActivo = estadosRepository.findById("estado-activo-test-catprod")
            .orElseGet(() -> {
                Estados estado = new Estados();
                estado.setId("estado-activo-test-catprod");
                estado.setEstado("Activo");
                return estadosRepository.save(estado);
            });
        
        Roles rolAdmin = rolesRepository.findById("rol-admin-test-catprod")
            .orElseGet(() -> {
                Roles rol = new Roles();
                rol.setId("rol-admin-test-catprod");
                rol.setRoles("Administrador");
                return rolesRepository.save(rol);
            });
        
        // 3. Create unique test user
        usuarioAdmin = new Usuarios();
        usuarioAdmin.setId("usuario-admin-test-catprod");
        usuarioAdmin.setNombre("catprod-test-admin");
        usuarioAdmin.setContrasena(passwordEncoder.encode("password123"));
        usuarioAdmin.setTelefono("1234567890");
        usuarioAdmin.setEstados(estadoActivo);
        usuarioAdmin.setRoles(rolAdmin);
        usuarioAdmin = usuariosRepository.save(usuarioAdmin);
        
        // 4. Generate JWT token
        jwtToken = jwtService.generateToken(usuarioAdmin.getNombre(), usuarioAdmin.getId());
        
        // 5. Create test entities
        categoriaTest1 = new CategoriasProductos();
        categoriaTest1.setId("cat-prod-test-1");
        categoriaTest1.setCategoria("Categoría Test 1");
        categoriaTest1 = categoriasProductosRepository.save(categoriaTest1);
        
        categoriaTest2 = new CategoriasProductos();
        categoriaTest2.setId("cat-prod-test-2");
        categoriaTest2.setCategoria("Categoría Test 2");
        categoriaTest2 = categoriasProductosRepository.save(categoriaTest2);
    }
    
    @Test
    @DisplayName("GET /api/categorias-productos - Debe retornar lista")
    void testGetAll() throws Exception {
        // Regenerate token to avoid 403
        Usuarios usr = usuariosRepository.findById(usuarioAdmin.getId()).orElse(null);
        String token = jwtService.generateToken(usr.getNombre(), usr.getId());
        
        mockMvc.perform(get("/api/categorias-productos")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(2)));
    }
    
    @Test
    @DisplayName("GET /api/categorias-productos/{id} - Debe retornar por ID")
    void testGetById() throws Exception {
        mockMvc.perform(get("/api/categorias-productos/{id}", categoriaTest1.getId())
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoriaTest1.getId()))
                .andExpect(jsonPath("$.categoria").value("Categoría Test 1"));
    }
    
    @Test
    @DisplayName("GET /api/categorias-productos/{id} - 404 cuando no existe")
    void testGetById_NotFound() throws Exception {
        mockMvc.perform(get("/api/categorias-productos/{id}", "id-no-existe")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("POST /api/categorias-productos - Debe crear")
    void testCreate() throws Exception {
        CategoriasProductos nueva = new CategoriasProductos();
        nueva.setId("cat-prod-created");
        nueva.setCategoria("Categoría Creada");
        
        mockMvc.perform(post("/api/categorias-productos")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nueva)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("cat-prod-created"))
                .andExpect(jsonPath("$.categoria").value("Categoría Creada"));
        
        assertThat(categoriasProductosRepository.findById("cat-prod-created")).isPresent();
        categoriasProductosRepository.deleteById("cat-prod-created");
    }
    
    @Test
    @DisplayName("PUT /api/categorias-productos/{id} - Debe actualizar")
    void testUpdate() throws Exception {
        CategoriasProductos actualizada = new CategoriasProductos();
        actualizada.setCategoria("Categoría Actualizada");
        
        mockMvc.perform(put("/api/categorias-productos/{id}", categoriaTest1.getId())
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizada)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoria").value("Categoría Actualizada"));
    }
    
    @Test
    @DisplayName("PUT /api/categorias-productos/{id} - 404 cuando no existe")
    void testUpdate_NotFound() throws Exception {
        CategoriasProductos actualizada = new CategoriasProductos();
        actualizada.setCategoria("Categoría Actualizada");
        
        mockMvc.perform(put("/api/categorias-productos/{id}", "id-no-existe")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizada)))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("PATCH /api/categorias-productos/{id} - Debe actualizar parcial")
    void testPartialUpdate() throws Exception {
        Map<String, Object> updates = new HashMap<>();
        updates.put("categoria", "Categoría Parcialmente Actualizada");
        
        mockMvc.perform(patch("/api/categorias-productos/{id}", categoriaTest1.getId())
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoria").value("Categoría Parcialmente Actualizada"));
    }
    
    @Test
    @DisplayName("PATCH /api/categorias-productos/{id} - 404 cuando no existe")
    void testPartialUpdate_NotFound() throws Exception {
        Map<String, Object> updates = new HashMap<>();
        updates.put("categoria", "Categoría Parcialmente Actualizada");
        
        mockMvc.perform(patch("/api/categorias-productos/{id}", "id-no-existe")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("DELETE /api/categorias-productos/{id} - Debe eliminar")
    void testDelete() throws Exception {
        mockMvc.perform(delete("/api/categorias-productos/{id}", categoriaTest2.getId())
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());
        
        assertThat(categoriasProductosRepository.findById(categoriaTest2.getId())).isEmpty();
    }
    
    @Test
    @DisplayName("DELETE /api/categorias-productos/{id} - 404 cuando no existe")
    void testDelete_NotFound() throws Exception {
        mockMvc.perform(delete("/api/categorias-productos/{id}", "id-no-existe")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("GET /api/categorias-productos - Sin auth 401")
    void testGetAll_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/categorias-productos"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @DisplayName("POST /api/categorias-productos - Sin auth 401")
    void testCreate_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/categorias-productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isUnauthorized());
    }
}
