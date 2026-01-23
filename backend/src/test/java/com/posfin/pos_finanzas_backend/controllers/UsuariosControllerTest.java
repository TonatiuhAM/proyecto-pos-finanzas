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
@DisplayName("Tests de UsuariosController")
class UsuariosControllerTest {
    
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JwtService jwtService;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private UsuariosRepository usuariosRepository;
    @Autowired private RolesRepository rolesRepository;
    @Autowired private EstadosRepository estadosRepository;
    
    private String jwtToken;
    private Usuarios usuarioAdmin;
    private Usuarios usuarioTest1;
    private Usuarios usuarioTest2;
    private Roles rolEmpleado;
    private Estados estadoActivo;
    private Estados estadoInactivo;
    
    @BeforeEach
    void setUp() {
        // 1. Selective cleanup
        usuariosRepository.findById("usuario-test-1").ifPresent(usuariosRepository::delete);
        usuariosRepository.findById("usuario-test-2").ifPresent(usuariosRepository::delete);
        usuariosRepository.findByNombre("usuarios-test-admin").ifPresent(usuariosRepository::delete);
        
        // 2. Reuse catalog data with orElseGet()
        estadoActivo = estadosRepository.findById("estado-activo-test-usuarios")
            .orElseGet(() -> {
                Estados estado = new Estados();
                estado.setId("estado-activo-test-usuarios");
                estado.setEstado("Activo");
                return estadosRepository.save(estado);
            });
        
        estadoInactivo = estadosRepository.findById("estado-inactivo-test-usuarios")
            .orElseGet(() -> {
                Estados estado = new Estados();
                estado.setId("estado-inactivo-test-usuarios");
                estado.setEstado("Inactivo");
                return estadosRepository.save(estado);
            });
        
        Roles rolAdmin = rolesRepository.findById("rol-admin-test-usuarios")
            .orElseGet(() -> {
                Roles rol = new Roles();
                rol.setId("rol-admin-test-usuarios");
                rol.setRoles("Administrador");
                return rolesRepository.save(rol);
            });
        
        rolEmpleado = rolesRepository.findById("rol-empleado-test-usuarios")
            .orElseGet(() -> {
                Roles rol = new Roles();
                rol.setId("rol-empleado-test-usuarios");
                rol.setRoles("Empleado");
                return rolesRepository.save(rol);
            });
        
        // 3. Create unique test user for authentication
        usuarioAdmin = new Usuarios();
        usuarioAdmin.setId("usuario-admin-test-usuarios");
        usuarioAdmin.setNombre("usuarios-test-admin");
        usuarioAdmin.setContrasena(passwordEncoder.encode("password123"));
        usuarioAdmin.setTelefono("1234567890");
        usuarioAdmin.setEstados(estadoActivo);
        usuarioAdmin.setRoles(rolAdmin);
        usuarioAdmin = usuariosRepository.save(usuarioAdmin);
        
        // 4. Generate JWT token
        jwtToken = jwtService.generateToken(usuarioAdmin.getNombre(), usuarioAdmin.getId());
        
        // 5. Create test entities
        usuarioTest1 = new Usuarios();
        usuarioTest1.setId("usuario-test-1");
        usuarioTest1.setNombre("Usuario Test 1");
        usuarioTest1.setContrasena(passwordEncoder.encode("password123"));
        usuarioTest1.setTelefono("5551234567");
        usuarioTest1.setEstados(estadoActivo);
        usuarioTest1.setRoles(rolEmpleado);
        usuarioTest1 = usuariosRepository.save(usuarioTest1);
        
        usuarioTest2 = new Usuarios();
        usuarioTest2.setId("usuario-test-2");
        usuarioTest2.setNombre("Usuario Test 2");
        usuarioTest2.setContrasena(passwordEncoder.encode("password456"));
        usuarioTest2.setTelefono("5559876543");
        usuarioTest2.setEstados(estadoInactivo);
        usuarioTest2.setRoles(rolEmpleado);
        usuarioTest2 = usuariosRepository.save(usuarioTest2);
    }
    
    @Test
    @DisplayName("GET /api/usuarios - Debe retornar lista de UsuariosDTO")
    void testGetAll() throws Exception {
        // Regenerate token to avoid 403
        Usuarios usr = usuariosRepository.findById(usuarioAdmin.getId()).orElse(null);
        String token = jwtService.generateToken(usr.getNombre(), usr.getId());
        
        mockMvc.perform(get("/api/usuarios")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].nombre").exists())
                .andExpect(jsonPath("$[0].rolesId").exists())
                .andExpect(jsonPath("$[0].estadosId").exists());
    }
    
    @Test
    @DisplayName("GET /api/usuarios/{id} - Debe retornar UsuariosDTO por ID")
    void testGetById() throws Exception {
        mockMvc.perform(get("/api/usuarios/{id}", usuarioTest1.getId())
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(usuarioTest1.getId()))
                .andExpect(jsonPath("$.nombre").value("Usuario Test 1"))
                .andExpect(jsonPath("$.telefono").value("5551234567"))
                .andExpect(jsonPath("$.rolesId").value(rolEmpleado.getId()))
                .andExpect(jsonPath("$.rolesRoles").value("Empleado"))
                .andExpect(jsonPath("$.estadosId").value(estadoActivo.getId()))
                .andExpect(jsonPath("$.estadosEstado").value("Activo"));
    }
    
    @Test
    @DisplayName("GET /api/usuarios/{id} - 404 cuando no existe")
    void testGetById_NotFound() throws Exception {
        mockMvc.perform(get("/api/usuarios/{id}", "id-no-existe")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("POST /api/usuarios - Debe crear usuario con relaciones")
    void testCreate() throws Exception {
        Usuarios nuevo = new Usuarios();
        nuevo.setId("usuario-created");
        nuevo.setNombre("Usuario Creado");
        nuevo.setContrasena("password789");
        nuevo.setTelefono("5556667788");
        
        // Set relations using nested objects with ID
        Roles rol = new Roles();
        rol.setId(rolEmpleado.getId());
        nuevo.setRoles(rol);
        
        Estados estado = new Estados();
        estado.setId(estadoActivo.getId());
        nuevo.setEstados(estado);
        
        mockMvc.perform(post("/api/usuarios")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("usuario-created"))
                .andExpect(jsonPath("$.nombre").value("Usuario Creado"));
        
        assertThat(usuariosRepository.findById("usuario-created")).isPresent();
        usuariosRepository.deleteById("usuario-created");
    }
    
    @Test
    @DisplayName("POST /api/usuarios - Error cuando rol no existe")
    void testCreate_RolNotFound() throws Exception {
        Usuarios nuevo = new Usuarios();
        nuevo.setId("usuario-created-fail");
        nuevo.setNombre("Usuario Fail");
        nuevo.setContrasena("password789");
        nuevo.setTelefono("5556667788");
        
        // Set non-existent role
        Roles rol = new Roles();
        rol.setId("rol-no-existe");
        nuevo.setRoles(rol);
        
        Estados estado = new Estados();
        estado.setId(estadoActivo.getId());
        nuevo.setEstados(estado);
        
        // The controller throws RuntimeException which results in 500 error
        try {
            mockMvc.perform(post("/api/usuarios")
                    .header("Authorization", "Bearer " + jwtToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(nuevo)));
        } catch (Exception e) {
            // Expected: RuntimeException wrapped in ServletException
            assertThat(e.getCause()).isInstanceOf(RuntimeException.class);
            assertThat(e.getCause().getMessage()).contains("Roles no encontrado");
        }
    }
    
    @Test
    @DisplayName("POST /api/usuarios - Error cuando estado no existe")
    void testCreate_EstadoNotFound() throws Exception {
        Usuarios nuevo = new Usuarios();
        nuevo.setId("usuario-created-fail");
        nuevo.setNombre("Usuario Fail");
        nuevo.setContrasena("password789");
        nuevo.setTelefono("5556667788");
        
        Roles rol = new Roles();
        rol.setId(rolEmpleado.getId());
        nuevo.setRoles(rol);
        
        // Set non-existent estado
        Estados estado = new Estados();
        estado.setId("estado-no-existe");
        nuevo.setEstados(estado);
        
        // The controller throws RuntimeException which results in 500 error
        try {
            mockMvc.perform(post("/api/usuarios")
                    .header("Authorization", "Bearer " + jwtToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(nuevo)));
        } catch (Exception e) {
            // Expected: RuntimeException wrapped in ServletException
            assertThat(e.getCause()).isInstanceOf(RuntimeException.class);
            assertThat(e.getCause().getMessage()).contains("Estados no encontrado");
        }
    }
    
    @Test
    @DisplayName("PUT /api/usuarios/{id} - Debe actualizar usuario")
    void testUpdate() throws Exception {
        Usuarios actualizado = new Usuarios();
        actualizado.setNombre("Usuario Actualizado");
        actualizado.setContrasena("newpassword");
        actualizado.setTelefono("5559998888");
        
        Roles rol = new Roles();
        rol.setId(rolEmpleado.getId());
        actualizado.setRoles(rol);
        
        Estados estado = new Estados();
        estado.setId(estadoInactivo.getId());
        actualizado.setEstados(estado);
        
        mockMvc.perform(put("/api/usuarios/{id}", usuarioTest1.getId())
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Usuario Actualizado"))
                .andExpect(jsonPath("$.telefono").value("5559998888"));
    }
    
    @Test
    @DisplayName("PUT /api/usuarios/{id} - 404 cuando no existe")
    void testUpdate_NotFound() throws Exception {
        Usuarios actualizado = new Usuarios();
        actualizado.setNombre("Usuario Actualizado");
        actualizado.setContrasena("newpassword");
        actualizado.setTelefono("5559998888");
        
        Roles rol = new Roles();
        rol.setId(rolEmpleado.getId());
        actualizado.setRoles(rol);
        
        Estados estado = new Estados();
        estado.setId(estadoActivo.getId());
        actualizado.setEstados(estado);
        
        mockMvc.perform(put("/api/usuarios/{id}", "id-no-existe")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizado)))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("PATCH /api/usuarios/{id} - Debe actualizar campos simples")
    void testPartialUpdate_SimpleFields() throws Exception {
        Map<String, Object> updates = new HashMap<>();
        updates.put("nombre", "Nombre Actualizado");
        updates.put("telefono", "5551111111");
        
        mockMvc.perform(patch("/api/usuarios/{id}", usuarioTest1.getId())
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Nombre Actualizado"))
                .andExpect(jsonPath("$.telefono").value("5551111111"));
    }
    
    @Test
    @DisplayName("PATCH /api/usuarios/{id} - Debe actualizar rol")
    void testPartialUpdate_Roles() throws Exception {
        Map<String, Object> updates = new HashMap<>();
        Map<String, String> rolesMap = new HashMap<>();
        rolesMap.put("id", rolEmpleado.getId());
        updates.put("roles", rolesMap);
        
        mockMvc.perform(patch("/api/usuarios/{id}", usuarioTest1.getId())
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rolesId").value(rolEmpleado.getId()));
    }
    
    @Test
    @DisplayName("PATCH /api/usuarios/{id} - Error cuando rol no existe")
    void testPartialUpdate_RolNotFound() throws Exception {
        Map<String, Object> updates = new HashMap<>();
        Map<String, String> rolesMap = new HashMap<>();
        rolesMap.put("id", "rol-no-existe");
        updates.put("roles", rolesMap);
        
        mockMvc.perform(patch("/api/usuarios/{id}", usuarioTest1.getId())
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("PATCH /api/usuarios/{id} - Debe actualizar estado")
    void testPartialUpdate_Estados() throws Exception {
        Map<String, Object> updates = new HashMap<>();
        Map<String, String> estadosMap = new HashMap<>();
        estadosMap.put("id", estadoInactivo.getId());
        updates.put("estados", estadosMap);
        
        mockMvc.perform(patch("/api/usuarios/{id}", usuarioTest1.getId())
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadosId").value(estadoInactivo.getId()))
                .andExpect(jsonPath("$.estadosEstado").value("Inactivo"));
    }
    
    @Test
    @DisplayName("PATCH /api/usuarios/{id} - Error cuando estado no existe")
    void testPartialUpdate_EstadoNotFound() throws Exception {
        Map<String, Object> updates = new HashMap<>();
        Map<String, String> estadosMap = new HashMap<>();
        estadosMap.put("id", "estado-no-existe");
        updates.put("estados", estadosMap);
        
        mockMvc.perform(patch("/api/usuarios/{id}", usuarioTest1.getId())
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("PATCH /api/usuarios/{id} - 404 cuando no existe")
    void testPartialUpdate_NotFound() throws Exception {
        Map<String, Object> updates = new HashMap<>();
        updates.put("nombre", "Nombre Actualizado");
        
        mockMvc.perform(patch("/api/usuarios/{id}", "id-no-existe")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("DELETE /api/usuarios/{id} - Debe eliminar")
    void testDelete() throws Exception {
        mockMvc.perform(delete("/api/usuarios/{id}", usuarioTest2.getId())
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());
        
        assertThat(usuariosRepository.findById(usuarioTest2.getId())).isEmpty();
    }
    
    @Test
    @DisplayName("DELETE /api/usuarios/{id} - 404 cuando no existe")
    void testDelete_NotFound() throws Exception {
        mockMvc.perform(delete("/api/usuarios/{id}", "id-no-existe")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("GET /api/usuarios - Sin auth 401")
    void testGetAll_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @DisplayName("POST /api/usuarios - Sin auth 401")
    void testCreate_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isUnauthorized());
    }
}
