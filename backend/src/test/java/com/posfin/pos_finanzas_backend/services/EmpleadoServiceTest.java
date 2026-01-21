package com.posfin.pos_finanzas_backend.services;

import com.posfin.pos_finanzas_backend.dtos.EmpleadoCreateRequestDTO;
import com.posfin.pos_finanzas_backend.dtos.EmpleadoResponseDTO;
import com.posfin.pos_finanzas_backend.models.Estados;
import com.posfin.pos_finanzas_backend.models.Roles;
import com.posfin.pos_finanzas_backend.models.Usuarios;
import com.posfin.pos_finanzas_backend.repositories.EstadosRepository;
import com.posfin.pos_finanzas_backend.repositories.RolesRepository;
import com.posfin.pos_finanzas_backend.repositories.UsuariosRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de EmpleadoService")
class EmpleadoServiceTest {

    @Mock
    private UsuariosRepository usuariosRepository;

    @Mock
    private RolesRepository rolesRepository;

    @Mock
    private EstadosRepository estadosRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private EmpleadoService empleadoService;

    private Roles rolAdministrador;
    private Roles rolEmpleado;
    private Estados estadoActivo;
    private Estados estadoInactivo;
    private Usuarios usuario1;
    private Usuarios usuario2;

    @BeforeEach
    void setUp() {
        // Configurar roles
        rolAdministrador = new Roles();
        rolAdministrador.setId("rol-admin");
        rolAdministrador.setRoles("Administrador");

        rolEmpleado = new Roles();
        rolEmpleado.setId("rol-empleado");
        rolEmpleado.setRoles("Empleado");

        // Configurar estados
        estadoActivo = new Estados();
        estadoActivo.setId("estado-activo");
        estadoActivo.setEstado("Activo");

        estadoInactivo = new Estados();
        estadoInactivo.setId("estado-inactivo");
        estadoInactivo.setEstado("Inactivo");

        // Configurar usuarios de prueba
        usuario1 = new Usuarios();
        usuario1.setId("usuario-1");
        usuario1.setNombre("admin");
        usuario1.setTelefono("1234567890");
        usuario1.setContrasena("$2a$10$hashedPassword1");
        usuario1.setRoles(rolAdministrador);
        usuario1.setEstados(estadoActivo);

        usuario2 = new Usuarios();
        usuario2.setId("usuario-2");
        usuario2.setNombre("empleado1");
        usuario2.setTelefono("0987654321");
        usuario2.setContrasena("$2a$10$hashedPassword2");
        usuario2.setRoles(rolEmpleado);
        usuario2.setEstados(estadoActivo);
    }

    @Test
    @DisplayName("Debe obtener todos los empleados")
    void testObtenerTodosLosEmpleados_Exitoso() {
        // Arrange
        when(usuariosRepository.findAll()).thenReturn(Arrays.asList(usuario1, usuario2));

        // Act
        List<EmpleadoResponseDTO> resultado = empleadoService.obtenerTodosLosEmpleados();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("admin", resultado.get(0).getNombre());
        assertEquals("empleado1", resultado.get(1).getNombre());
        assertEquals("Administrador", resultado.get(0).getRolNombre());
        assertEquals("Empleado", resultado.get(1).getRolNombre());
        verify(usuariosRepository).findAll();
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay empleados")
    void testObtenerTodosLosEmpleados_ListaVacia() {
        // Arrange
        when(usuariosRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<EmpleadoResponseDTO> resultado = empleadoService.obtenerTodosLosEmpleados();

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("Debe crear empleado exitosamente")
    void testCrearEmpleado_Exitoso() {
        // Arrange
        EmpleadoCreateRequestDTO request = new EmpleadoCreateRequestDTO();
        request.setNombre("nuevoEmpleado");
        request.setTelefono("5551234567");
        request.setContrasena("password123");
        request.setRolId("rol-empleado");

        when(rolesRepository.findById("rol-empleado")).thenReturn(Optional.of(rolEmpleado));
        when(estadosRepository.findFirstByEstado("Activo")).thenReturn(Optional.of(estadoActivo));
        when(usuariosRepository.findByNombre("nuevoEmpleado")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$hashedPasswordNew");
        when(usuariosRepository.save(any(Usuarios.class))).thenAnswer(invocation -> {
            Usuarios usuarioGuardado = invocation.getArgument(0);
            usuarioGuardado.setId("nuevo-usuario");
            return usuarioGuardado;
        });

        // Act
        EmpleadoResponseDTO resultado = empleadoService.crearEmpleado(request);

        // Assert
        assertNotNull(resultado);
        assertEquals("nuevoEmpleado", resultado.getNombre());
        assertEquals("5551234567", resultado.getTelefono());
        assertEquals("Empleado", resultado.getRolNombre());
        assertEquals("Activo", resultado.getEstadoNombre());

        verify(passwordEncoder).encode("password123");
        verify(usuariosRepository).save(argThat(usuario ->
                usuario.getNombre().equals("nuevoEmpleado") &&
                        usuario.getContrasena().equals("$2a$10$hashedPasswordNew") &&
                        usuario.getRoles().equals(rolEmpleado) &&
                        usuario.getEstados().equals(estadoActivo)
        ));
    }

    @Test
    @DisplayName("Debe fallar al crear empleado con rol inexistente")
    void testCrearEmpleado_RolNoExiste() {
        // Arrange
        EmpleadoCreateRequestDTO request = new EmpleadoCreateRequestDTO();
        request.setNombre("nuevoEmpleado");
        request.setTelefono("5551234567");
        request.setContrasena("password123");
        request.setRolId("rol-inexistente");

        when(rolesRepository.findById("rol-inexistente")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> empleadoService.crearEmpleado(request)
        );

        assertEquals("El rol especificado no existe", exception.getMessage());
        verify(usuariosRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe fallar al crear empleado si no existe estado Activo")
    void testCrearEmpleado_EstadoActivoNoExiste() {
        // Arrange
        EmpleadoCreateRequestDTO request = new EmpleadoCreateRequestDTO();
        request.setNombre("nuevoEmpleado");
        request.setTelefono("5551234567");
        request.setContrasena("password123");
        request.setRolId("rol-empleado");

        when(rolesRepository.findById("rol-empleado")).thenReturn(Optional.of(rolEmpleado));
        when(estadosRepository.findFirstByEstado("Activo")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> empleadoService.crearEmpleado(request)
        );

        assertEquals("No se encontró el estado 'Activo' en la base de datos", exception.getMessage());
        verify(usuariosRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe fallar al crear empleado con nombre duplicado")
    void testCrearEmpleado_NombreDuplicado() {
        // Arrange
        EmpleadoCreateRequestDTO request = new EmpleadoCreateRequestDTO();
        request.setNombre("admin");
        request.setTelefono("5551234567");
        request.setContrasena("password123");
        request.setRolId("rol-empleado");

        when(rolesRepository.findById("rol-empleado")).thenReturn(Optional.of(rolEmpleado));
        when(estadosRepository.findFirstByEstado("Activo")).thenReturn(Optional.of(estadoActivo));
        when(usuariosRepository.findByNombre("admin")).thenReturn(Optional.of(usuario1));

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> empleadoService.crearEmpleado(request)
        );

        assertEquals("Ya existe un usuario con ese nombre", exception.getMessage());
        verify(usuariosRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe hashear la contraseña al crear empleado")
    void testCrearEmpleado_ContrasenaHasheada() {
        // Arrange
        EmpleadoCreateRequestDTO request = new EmpleadoCreateRequestDTO();
        request.setNombre("empleadoTest");
        request.setTelefono("5551234567");
        request.setContrasena("miPasswordSecreta");
        request.setRolId("rol-empleado");

        when(rolesRepository.findById("rol-empleado")).thenReturn(Optional.of(rolEmpleado));
        when(estadosRepository.findFirstByEstado("Activo")).thenReturn(Optional.of(estadoActivo));
        when(usuariosRepository.findByNombre("empleadoTest")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("miPasswordSecreta")).thenReturn("$2a$10$hashedSecretPassword");
        when(usuariosRepository.save(any(Usuarios.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        empleadoService.crearEmpleado(request);

        // Assert
        verify(passwordEncoder).encode("miPasswordSecreta");
        verify(usuariosRepository).save(argThat(usuario ->
                usuario.getContrasena().equals("$2a$10$hashedSecretPassword")
        ));
    }

    @Test
    @DisplayName("Debe cambiar estado de empleado exitosamente")
    void testCambiarEstadoEmpleado_Exitoso() {
        // Arrange
        when(usuariosRepository.findById("usuario-1")).thenReturn(Optional.of(usuario1));
        when(estadosRepository.findFirstByEstado("Inactivo")).thenReturn(Optional.of(estadoInactivo));
        when(usuariosRepository.save(any(Usuarios.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        EmpleadoResponseDTO resultado = empleadoService.cambiarEstadoEmpleado("usuario-1", "Inactivo");

        // Assert
        assertNotNull(resultado);
        assertEquals("usuario-1", resultado.getId());
        assertEquals("Inactivo", resultado.getEstadoNombre());
        verify(usuariosRepository).save(argThat(usuario -> usuario.getEstados().equals(estadoInactivo)));
    }

    @Test
    @DisplayName("Debe fallar al cambiar estado con empleado inexistente")
    void testCambiarEstadoEmpleado_EmpleadoNoExiste() {
        // Arrange
        when(usuariosRepository.findById("usuario-inexistente")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> empleadoService.cambiarEstadoEmpleado("usuario-inexistente", "Inactivo")
        );

        assertEquals("Empleado no encontrado", exception.getMessage());
        verify(usuariosRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe fallar al cambiar a estado inexistente")
    void testCambiarEstadoEmpleado_EstadoNoExiste() {
        // Arrange
        when(usuariosRepository.findById("usuario-1")).thenReturn(Optional.of(usuario1));
        when(estadosRepository.findFirstByEstado("EstadoInvalido")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> empleadoService.cambiarEstadoEmpleado("usuario-1", "EstadoInvalido")
        );

        assertEquals("El estado especificado no existe", exception.getMessage());
        verify(usuariosRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe obtener empleado por ID exitosamente")
    void testObtenerEmpleadoPorId_Exitoso() {
        // Arrange
        when(usuariosRepository.findById("usuario-2")).thenReturn(Optional.of(usuario2));

        // Act
        EmpleadoResponseDTO resultado = empleadoService.obtenerEmpleadoPorId("usuario-2");

        // Assert
        assertNotNull(resultado);
        assertEquals("usuario-2", resultado.getId());
        assertEquals("empleado1", resultado.getNombre());
        assertEquals("0987654321", resultado.getTelefono());
        assertEquals("Empleado", resultado.getRolNombre());
        assertEquals("Activo", resultado.getEstadoNombre());
    }

    @Test
    @DisplayName("Debe fallar al obtener empleado con ID inexistente")
    void testObtenerEmpleadoPorId_NoExiste() {
        // Arrange
        when(usuariosRepository.findById("usuario-inexistente")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> empleadoService.obtenerEmpleadoPorId("usuario-inexistente")
        );

        assertEquals("Empleado no encontrado", exception.getMessage());
    }

    @Test
    @DisplayName("Debe convertir Usuario a EmpleadoResponseDTO correctamente")
    void testConversionADTO() {
        // Arrange
        when(usuariosRepository.findById("usuario-1")).thenReturn(Optional.of(usuario1));

        // Act
        EmpleadoResponseDTO resultado = empleadoService.obtenerEmpleadoPorId("usuario-1");

        // Assert
        assertNotNull(resultado);
        assertEquals(usuario1.getId(), resultado.getId());
        assertEquals(usuario1.getNombre(), resultado.getNombre());
        assertEquals(usuario1.getTelefono(), resultado.getTelefono());
        assertEquals(usuario1.getRoles().getId(), resultado.getRolId());
        assertEquals(usuario1.getRoles().getRoles(), resultado.getRolNombre());
        assertEquals(usuario1.getEstados().getId(), resultado.getEstadoId());
        assertEquals(usuario1.getEstados().getEstado(), resultado.getEstadoNombre());
    }
}
