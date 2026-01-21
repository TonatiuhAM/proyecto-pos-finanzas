package com.posfin.pos_finanzas_backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.posfin.pos_finanzas_backend.dtos.WorkspaceStatusDTO;
import com.posfin.pos_finanzas_backend.dtos.WorkspacesDTO;
import com.posfin.pos_finanzas_backend.models.*;
import com.posfin.pos_finanzas_backend.repositories.*;
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

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para WorkspacesController
 * 
 * Prueba el flujo completo de gestión de workspaces incluyendo:
 * - Creación y gestión de workspaces (CRUD)
 * - Consulta de workspaces con estado
 * - Endpoint de prueba
 * - Validación de workspaces permanentes y temporales
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Tests de WorkspacesController (Gestión de Espacios de Trabajo)")
class WorkspacesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WorkspacesRepository workspacesRepository;

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

    private String jwtToken;
    private Estados estadoActivo;
    private Roles rolAdmin;
    private Usuarios usuarioAdmin;
    private Workspaces workspacePermanente;
    private Workspaces workspaceTemporal;

    @BeforeEach
    void setUp() {
        // Limpiar workspaces de test
        workspacesRepository.findById("workspace-permanente-test").ifPresent(workspacesRepository::delete);
        workspacesRepository.findById("workspace-temporal-test").ifPresent(workspacesRepository::delete);
        
        // Limpiar usuario de test
        usuariosRepository.findByNombre("workspace-test-admin").ifPresent(u -> usuariosRepository.delete(u));

        // Crear o reutilizar estado
        estadoActivo = estadosRepository.findById("estado-activo-test-workspace").orElseGet(() -> {
            Estados estado = new Estados();
            estado.setId("estado-activo-test-workspace");
            estado.setEstado("Activo");
            return estadosRepository.save(estado);
        });

        // Crear o reutilizar rol
        rolAdmin = rolesRepository.findById("rol-admin-test-workspace").orElseGet(() -> {
            Roles rol = new Roles();
            rol.setId("rol-admin-test-workspace");
            rol.setRoles("Administrador");
            return rolesRepository.save(rol);
        });

        // Crear usuario admin para autenticación
        usuarioAdmin = new Usuarios();
        usuarioAdmin.setId("usuario-admin-test-workspace");
        usuarioAdmin.setNombre("workspace-test-admin");
        usuarioAdmin.setTelefono("5551111111");
        usuarioAdmin.setContrasena(passwordEncoder.encode("admin123"));
        usuarioAdmin.setEstados(estadoActivo);
        usuarioAdmin.setRoles(rolAdmin);
        usuarioAdmin = usuariosRepository.save(usuarioAdmin);

        // Generar token JWT para requests autenticados
        jwtToken = jwtService.generateToken(usuarioAdmin.getNombre(), usuarioAdmin.getId());

        // Crear workspaces de prueba
        workspacePermanente = new Workspaces();
        workspacePermanente.setId("workspace-permanente-test");
        workspacePermanente.setNombre("Mesa 1");
        workspacePermanente.setPermanente(true);
        workspacePermanente.setSolicitudCuenta(false);
        workspacePermanente = workspacesRepository.save(workspacePermanente);

        workspaceTemporal = new Workspaces();
        workspaceTemporal.setId("workspace-temporal-test");
        workspaceTemporal.setNombre("Mostrador");
        workspaceTemporal.setPermanente(false);
        workspaceTemporal.setSolicitudCuenta(false);
        workspaceTemporal = workspacesRepository.save(workspaceTemporal);
    }

    @Test
    @DisplayName("GET /api/workspaces/test - Debe retornar status OK")
    void testEndpointTest_DebeRetornarOK() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/workspaces/test")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("WorkspacesController funcionando correctamente"))
                .andExpect(jsonPath("$.user").value("workspace-test-admin"));
    }

    @Test
    @DisplayName("GET /api/workspaces - Debe obtener todos los workspaces")
    void testObtenerTodosLosWorkspaces_DebeRetornarLista() throws Exception {
        // Verificar que el usuario sigue existiendo y regenerar token si es necesario
        Usuarios usuarioVerificado = usuariosRepository.findById(usuarioAdmin.getId()).orElse(null);
        assertThat(usuarioVerificado).isNotNull();
        
        // Regenerar token para asegurar validez
        String tokenFresco = jwtService.generateToken(usuarioVerificado.getNombre(), usuarioVerificado.getId());
        
        // Act & Assert
        MvcResult result = mockMvc.perform(get("/api/workspaces")
                .header("Authorization", "Bearer " + tokenFresco))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        WorkspacesDTO[] workspaces = objectMapper.readValue(responseBody, WorkspacesDTO[].class);
        
        assertThat(workspaces).hasSizeGreaterThanOrEqualTo(2);
        assertThat(workspaces).anyMatch(w -> w.getNombre().equals("Mesa 1"));
        assertThat(workspaces).anyMatch(w -> w.getNombre().equals("Mostrador"));
    }

    @Test
    @DisplayName("GET /api/workspaces/{id} - Debe obtener workspace por ID existente")
    void testObtenerWorkspacePorId_ConIdExistente_DebeRetornarWorkspace() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/workspaces/{id}", workspacePermanente.getId())
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(workspacePermanente.getId()))
                .andExpect(jsonPath("$.nombre").value("Mesa 1"))
                .andExpect(jsonPath("$.permanente").value(true));
    }

    @Test
    @DisplayName("GET /api/workspaces/{id} - Debe retornar 404 con ID inexistente")
    void testObtenerWorkspacePorId_ConIdInexistente_DebeRetornar404() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/workspaces/{id}", "id-no-existe")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/workspaces - Debe crear workspace exitosamente")
    void testCrearWorkspace_ConDatosValidos_DebeRetornar200() throws Exception {
        // Arrange
        Map<String, Object> request = new HashMap<>();
        request.put("nombre", "Mesa 5");
        request.put("permanente", true);

        // Act & Assert
        MvcResult result = mockMvc.perform(post("/api/workspaces")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.nombre").value("Mesa 5"))
                .andExpect(jsonPath("$.permanente").value(true))
                .andReturn();

        // Verificar que se guardó en la base de datos
        String responseBody = result.getResponse().getContentAsString();
        WorkspacesDTO response = objectMapper.readValue(responseBody, WorkspacesDTO.class);
        
        Workspaces workspaceGuardado = workspacesRepository.findById(response.getId()).orElse(null);
        assertThat(workspaceGuardado).isNotNull();
        assertThat(workspaceGuardado.getNombre()).isEqualTo("Mesa 5");
        assertThat(workspaceGuardado.getPermanente()).isTrue();
        
        // Limpiar el workspace creado
        workspacesRepository.deleteById(response.getId());
    }

    @Test
    @DisplayName("POST /api/workspaces - Debe crear workspace temporal por defecto")
    void testCrearWorkspace_SinPermanente_DebeCrearTemporal() throws Exception {
        // Arrange
        Map<String, Object> request = new HashMap<>();
        request.put("nombre", "Barra");

        // Act & Assert
        MvcResult result = mockMvc.perform(post("/api/workspaces")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Barra"))
                .andExpect(jsonPath("$.permanente").value(false))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        WorkspacesDTO response = objectMapper.readValue(responseBody, WorkspacesDTO.class);
        
        // Limpiar
        workspacesRepository.deleteById(response.getId());
    }

    @Test
    @DisplayName("PUT /api/workspaces/{id} - Debe actualizar workspace existente")
    void testActualizarWorkspace_ConIdExistente_DebeRetornar200() throws Exception {
        // Arrange
        Map<String, Object> request = new HashMap<>();
        request.put("nombre", "Mesa 1 VIP");
        request.put("permanente", true);

        // Act & Assert
        mockMvc.perform(put("/api/workspaces/{id}", workspacePermanente.getId())
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(workspacePermanente.getId()))
                .andExpect(jsonPath("$.nombre").value("Mesa 1 VIP"))
                .andExpect(jsonPath("$.permanente").value(true));

        // Verificar que se actualizó en la base de datos
        Workspaces workspaceActualizado = workspacesRepository.findById(workspacePermanente.getId()).orElse(null);
        assertThat(workspaceActualizado).isNotNull();
        assertThat(workspaceActualizado.getNombre()).isEqualTo("Mesa 1 VIP");
        
        // Restaurar nombre original
        workspaceActualizado.setNombre("Mesa 1");
        workspacesRepository.save(workspaceActualizado);
    }

    @Test
    @DisplayName("PUT /api/workspaces/{id} - Debe retornar 404 con ID inexistente")
    void testActualizarWorkspace_ConIdInexistente_DebeRetornar404() throws Exception {
        // Arrange
        Map<String, Object> request = new HashMap<>();
        request.put("nombre", "Mesa Inexistente");
        request.put("permanente", false);

        // Act & Assert
        mockMvc.perform(put("/api/workspaces/{id}", "id-no-existe")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/workspaces/{id} - Debe eliminar workspace existente")
    void testEliminarWorkspace_ConIdExistente_DebeRetornar204() throws Exception {
        // Arrange - Crear workspace temporal para eliminar
        Workspaces workspaceTemp = new Workspaces();
        workspaceTemp.setId("workspace-temp-delete-test");
        workspaceTemp.setNombre("Para Eliminar");
        workspaceTemp.setPermanente(false);
        workspaceTemp = workspacesRepository.save(workspaceTemp);

        // Act & Assert
        mockMvc.perform(delete("/api/workspaces/{id}", workspaceTemp.getId())
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());

        // Verificar que se eliminó de la base de datos
        Workspaces workspaceEliminado = workspacesRepository.findById(workspaceTemp.getId()).orElse(null);
        assertThat(workspaceEliminado).isNull();
    }

    @Test
    @DisplayName("DELETE /api/workspaces/{id} - Debe retornar 404 con ID inexistente")
    void testEliminarWorkspace_ConIdInexistente_DebeRetornar404() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/workspaces/{id}", "id-no-existe")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/workspaces/status - Debe obtener workspaces con estado")
    void testObtenerWorkspacesConEstado_DebeRetornarListaConEstado() throws Exception {
        // Act & Assert
        MvcResult result = mockMvc.perform(get("/api/workspaces/status")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        WorkspaceStatusDTO[] workspaces = objectMapper.readValue(responseBody, WorkspaceStatusDTO[].class);
        
        assertThat(workspaces).hasSizeGreaterThanOrEqualTo(2);
        
        // Verificar que todos los workspaces tienen estado
        assertThat(workspaces).allMatch(w -> w.getEstado() != null);
        assertThat(workspaces).allMatch(w -> w.getEstado().equals("disponible") || 
                                              w.getEstado().equals("ocupado") || 
                                              w.getEstado().equals("cuenta"));
        
        // Verificar que nuestros workspaces de prueba están presentes
        assertThat(workspaces).anyMatch(w -> w.getNombre().equals("Mesa 1"));
        assertThat(workspaces).anyMatch(w -> w.getNombre().equals("Mostrador"));
    }

    @Test
    @DisplayName("POST /api/workspaces - Debe rechazar creación sin autenticación")
    void testCrearWorkspace_SinAutenticacion_DebeRetornar401() throws Exception {
        // Arrange
        Map<String, Object> request = new HashMap<>();
        request.put("nombre", "Sin Auth");
        request.put("permanente", false);

        // Act & Assert - Sin header Authorization
        mockMvc.perform(post("/api/workspaces")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
