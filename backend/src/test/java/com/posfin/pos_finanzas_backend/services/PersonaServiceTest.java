package com.posfin.pos_finanzas_backend.services;

import com.posfin.pos_finanzas_backend.dtos.PersonaCreateRequestDTO;
import com.posfin.pos_finanzas_backend.dtos.PersonaResponseDTO;
import com.posfin.pos_finanzas_backend.models.CategoriaPersonas;
import com.posfin.pos_finanzas_backend.models.Estados;
import com.posfin.pos_finanzas_backend.models.Personas;
import com.posfin.pos_finanzas_backend.repositories.CategoriaPersonasRepository;
import com.posfin.pos_finanzas_backend.repositories.EstadosRepository;
import com.posfin.pos_finanzas_backend.repositories.PersonasRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de PersonaService")
class PersonaServiceTest {

    @Mock
    private PersonasRepository personasRepository;

    @Mock
    private CategoriaPersonasRepository categoriaPersonasRepository;

    @Mock
    private EstadosRepository estadosRepository;

    @InjectMocks
    private PersonaService personaService;

    private CategoriaPersonas categoriaCliente;
    private CategoriaPersonas categoriaProveedor;
    private Estados estadoActivo;
    private Estados estadoInactivo;
    private Personas persona1;
    private Personas persona2;

    @BeforeEach
    void setUp() {
        // Configurar categorías
        categoriaCliente = new CategoriaPersonas();
        categoriaCliente.setId("cat-cliente");
        categoriaCliente.setCategoria("Cliente");

        categoriaProveedor = new CategoriaPersonas();
        categoriaProveedor.setId("cat-proveedor");
        categoriaProveedor.setCategoria("Proveedor");

        // Configurar estados
        estadoActivo = new Estados();
        estadoActivo.setId("estado-activo");
        estadoActivo.setEstado("Activo");

        estadoInactivo = new Estados();
        estadoInactivo.setId("estado-inactivo");
        estadoInactivo.setEstado("Inactivo");

        // Configurar personas de prueba
        persona1 = new Personas();
        persona1.setId("persona-1");
        persona1.setNombre("Juan");
        persona1.setApellidoPaterno("Pérez");
        persona1.setApellidoMaterno("García");
        persona1.setRfc("PEGA900101ABC");
        persona1.setEmail("juan.perez@example.com");
        persona1.setTelefono("1234567890");
        persona1.setCategoriaPersonas(categoriaCliente);
        persona1.setEstados(estadoActivo);

        persona2 = new Personas();
        persona2.setId("persona-2");
        persona2.setNombre("María");
        persona2.setApellidoPaterno("López");
        persona2.setApellidoMaterno("Sánchez");
        persona2.setRfc("LOSA850215XYZ");
        persona2.setEmail("maria.lopez@example.com");
        persona2.setTelefono("0987654321");
        persona2.setCategoriaPersonas(categoriaProveedor);
        persona2.setEstados(estadoActivo);
    }

    @Test
    @DisplayName("Debe crear persona exitosamente con todos los campos")
    void testCrearPersona_Exitoso() {
        // Arrange
        PersonaCreateRequestDTO request = new PersonaCreateRequestDTO();
        request.setNombre("  Carlos  ");
        request.setApellidos("  Ramírez Torres  ");
        request.setRfc("  RATC950315DEF  ");
        request.setEmail("  CARLOS.RAMIREZ@EXAMPLE.COM  ");
        request.setTelefono("  5551234567  ");
        request.setIdCategoriaPersona("cat-cliente");

        when(categoriaPersonasRepository.findById("cat-cliente")).thenReturn(Optional.of(categoriaCliente));
        when(estadosRepository.findFirstByEstado("Activo")).thenReturn(Optional.of(estadoActivo));
        when(personasRepository.findByRfcAndEstados_Estado(anyString(), eq("Activo"))).thenReturn(Collections.emptyList());
        when(personasRepository.findByEmailAndEstados_Estado(anyString(), eq("Activo"))).thenReturn(Collections.emptyList());
        when(personasRepository.save(any(Personas.class))).thenAnswer(invocation -> {
            Personas personaGuardada = invocation.getArgument(0);
            personaGuardada.setId("nueva-persona");
            return personaGuardada;
        });

        // Act
        PersonaResponseDTO resultado = personaService.crearPersona(request);

        // Assert
        assertNotNull(resultado);
        assertEquals("Carlos", resultado.getNombre());
        assertEquals("Ramírez", resultado.getApellidoPaterno());
        assertEquals("Torres", resultado.getApellidoMaterno());
        assertEquals("RATC950315DEF", resultado.getRfc());
        assertEquals("carlos.ramirez@example.com", resultado.getEmail());
        assertEquals("5551234567", resultado.getTelefono());
        assertEquals("cat-cliente", resultado.getIdCategoriaPersona());
        assertEquals("Activo", resultado.getNombreEstado());

        verify(personasRepository).save(argThat(persona ->
                persona.getNombre().equals("Carlos") &&
                        persona.getRfc().equals("RATC950315DEF") &&
                        persona.getEmail().equals("carlos.ramirez@example.com")
        ));
    }

    @Test
    @DisplayName("Debe crear persona con apellidos compuestos separados correctamente")
    void testCrearPersona_ApellidosCompuestos() {
        // Arrange
        PersonaCreateRequestDTO request = new PersonaCreateRequestDTO();
        request.setNombre("Ana");
        request.setApellidos("De la Torre Fernández");
        request.setRfc("TOFA900101ABC");
        request.setEmail("ana.delatorre@example.com");
        request.setTelefono("5559876543");
        request.setIdCategoriaPersona("cat-proveedor");

        when(categoriaPersonasRepository.findById("cat-proveedor")).thenReturn(Optional.of(categoriaProveedor));
        when(estadosRepository.findFirstByEstado("Activo")).thenReturn(Optional.of(estadoActivo));
        when(personasRepository.findByRfcAndEstados_Estado(anyString(), anyString())).thenReturn(Collections.emptyList());
        when(personasRepository.findByEmailAndEstados_Estado(anyString(), anyString())).thenReturn(Collections.emptyList());
        when(personasRepository.save(any(Personas.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        PersonaResponseDTO resultado = personaService.crearPersona(request);

        // Assert
        assertEquals("De", resultado.getApellidoPaterno());
        assertEquals("la Torre Fernández", resultado.getApellidoMaterno());
    }

    @Test
    @DisplayName("Debe crear persona sin RFC ni email (campos opcionales)")
    void testCrearPersona_SinRfcNiEmail() {
        // Arrange
        PersonaCreateRequestDTO request = new PersonaCreateRequestDTO();
        request.setNombre("Pedro");
        request.setApellidos("González Ruiz");
        request.setRfc(null);
        request.setEmail(null);
        request.setTelefono("5551112222");
        request.setIdCategoriaPersona("cat-cliente");

        when(categoriaPersonasRepository.findById("cat-cliente")).thenReturn(Optional.of(categoriaCliente));
        when(estadosRepository.findFirstByEstado("Activo")).thenReturn(Optional.of(estadoActivo));
        when(personasRepository.save(any(Personas.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        PersonaResponseDTO resultado = personaService.crearPersona(request);

        // Assert
        assertNotNull(resultado);
        assertEquals("Pedro", resultado.getNombre());
        assertNull(resultado.getRfc());
        assertNull(resultado.getEmail());

        verify(personasRepository, never()).findByRfcAndEstados_Estado(anyString(), anyString());
        verify(personasRepository, never()).findByEmailAndEstados_Estado(anyString(), anyString());
    }

    @Test
    @DisplayName("Debe fallar al crear persona con categoría inexistente")
    void testCrearPersona_CategoriaNoExiste() {
        // Arrange
        PersonaCreateRequestDTO request = new PersonaCreateRequestDTO();
        request.setNombre("Test");
        request.setApellidos("Usuario");
        request.setIdCategoriaPersona("cat-inexistente");

        when(categoriaPersonasRepository.findById("cat-inexistente")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> personaService.crearPersona(request)
        );

        assertEquals("La categoría de persona no existe", exception.getMessage());
        verify(personasRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe fallar al crear persona si no existe estado Activo")
    void testCrearPersona_EstadoActivoNoExiste() {
        // Arrange
        PersonaCreateRequestDTO request = new PersonaCreateRequestDTO();
        request.setNombre("Test");
        request.setApellidos("Usuario");
        request.setIdCategoriaPersona("cat-cliente");

        when(categoriaPersonasRepository.findById("cat-cliente")).thenReturn(Optional.of(categoriaCliente));
        when(estadosRepository.findFirstByEstado("Activo")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> personaService.crearPersona(request)
        );

        assertEquals("No se encontró el estado activo", exception.getMessage());
        verify(personasRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe fallar al crear persona con RFC duplicado")
    void testCrearPersona_RfcDuplicado() {
        // Arrange
        PersonaCreateRequestDTO request = new PersonaCreateRequestDTO();
        request.setNombre("Test");
        request.setApellidos("Usuario");
        request.setRfc("PEGA900101ABC");
        request.setIdCategoriaPersona("cat-cliente");

        when(categoriaPersonasRepository.findById("cat-cliente")).thenReturn(Optional.of(categoriaCliente));
        when(estadosRepository.findFirstByEstado("Activo")).thenReturn(Optional.of(estadoActivo));
        when(personasRepository.findByRfcAndEstados_Estado("PEGA900101ABC", "Activo")).thenReturn(Arrays.asList(persona1));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> personaService.crearPersona(request)
        );

        assertEquals("Ya existe una persona activa con el RFC: PEGA900101ABC", exception.getMessage());
        verify(personasRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe fallar al crear persona con email duplicado")
    void testCrearPersona_EmailDuplicado() {
        // Arrange
        PersonaCreateRequestDTO request = new PersonaCreateRequestDTO();
        request.setNombre("Test");
        request.setApellidos("Usuario");
        request.setRfc("OTRO123456ABC");
        request.setEmail("juan.perez@example.com");
        request.setIdCategoriaPersona("cat-cliente");

        when(categoriaPersonasRepository.findById("cat-cliente")).thenReturn(Optional.of(categoriaCliente));
        when(estadosRepository.findFirstByEstado("Activo")).thenReturn(Optional.of(estadoActivo));
        when(personasRepository.findByRfcAndEstados_Estado(anyString(), anyString())).thenReturn(Collections.emptyList());
        when(personasRepository.findByEmailAndEstados_Estado("juan.perez@example.com", "Activo")).thenReturn(Arrays.asList(persona1));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> personaService.crearPersona(request)
        );

        assertEquals("Ya existe una persona activa con el email: juan.perez@example.com", exception.getMessage());
        verify(personasRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe obtener personas por categoría ordenadas por nombre")
    void testObtenerPersonasPorCategoria_Exitoso() {
        // Arrange
        when(categoriaPersonasRepository.findById("cat-cliente")).thenReturn(Optional.of(categoriaCliente));
        when(personasRepository.findByCategoriaPersonas_IdOrderByNombreAsc("cat-cliente"))
                .thenReturn(Arrays.asList(persona1));

        // Act
        List<PersonaResponseDTO> resultado = personaService.obtenerPersonasPorCategoria("cat-cliente");

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Juan", resultado.get(0).getNombre());
        verify(personasRepository).findByCategoriaPersonas_IdOrderByNombreAsc("cat-cliente");
    }

    @Test
    @DisplayName("Debe fallar al obtener personas con categoría inexistente")
    void testObtenerPersonasPorCategoria_CategoriaNoExiste() {
        // Arrange
        when(categoriaPersonasRepository.findById("cat-inexistente")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> personaService.obtenerPersonasPorCategoria("cat-inexistente")
        );

        assertEquals("La categoría de persona no existe", exception.getMessage());
        verify(personasRepository, never()).findByCategoriaPersonas_IdOrderByNombreAsc(anyString());
    }

    @Test
    @DisplayName("Debe obtener personas activas por categoría")
    void testObtenerPersonasActivasPorCategoria_Exitoso() {
        // Arrange
        when(categoriaPersonasRepository.findById("cat-proveedor")).thenReturn(Optional.of(categoriaProveedor));
        when(personasRepository.findByCategoriaPersonas_IdAndEstados_EstadoOrderByNombreAsc("cat-proveedor", "Activo"))
                .thenReturn(Arrays.asList(persona2));

        // Act
        List<PersonaResponseDTO> resultado = personaService.obtenerPersonasActivasPorCategoria("cat-proveedor");

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("María", resultado.get(0).getNombre());
        assertEquals("Activo", resultado.get(0).getNombreEstado());
    }

    @Test
    @DisplayName("Debe obtener todas las personas activas")
    void testObtenerTodasLasPersonas_Exitoso() {
        // Arrange
        when(personasRepository.findByEstados_EstadoOrderByNombreAsc("Activo"))
                .thenReturn(Arrays.asList(persona1, persona2));

        // Act
        List<PersonaResponseDTO> resultado = personaService.obtenerTodasLasPersonas();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("Juan", resultado.get(0).getNombre());
        assertEquals("María", resultado.get(1).getNombre());
    }

    @Test
    @DisplayName("Debe obtener persona por ID")
    void testObtenerPersonaPorId_Exitoso() {
        // Arrange
        when(personasRepository.findById("persona-1")).thenReturn(Optional.of(persona1));

        // Act
        PersonaResponseDTO resultado = personaService.obtenerPersonaPorId("persona-1");

        // Assert
        assertNotNull(resultado);
        assertEquals("persona-1", resultado.getId());
        assertEquals("Juan", resultado.getNombre());
        assertEquals("PEGA900101ABC", resultado.getRfc());
    }

    @Test
    @DisplayName("Debe fallar al obtener persona con ID inexistente")
    void testObtenerPersonaPorId_NoExiste() {
        // Arrange
        when(personasRepository.findById("persona-inexistente")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> personaService.obtenerPersonaPorId("persona-inexistente")
        );

        assertEquals("La persona no existe", exception.getMessage());
    }

    @Test
    @DisplayName("Debe actualizar estado de persona exitosamente")
    void testActualizarEstadoPersona_Exitoso() {
        // Arrange
        when(personasRepository.findById("persona-1")).thenReturn(Optional.of(persona1));
        when(estadosRepository.findFirstByEstado("Inactivo")).thenReturn(Optional.of(estadoInactivo));
        when(personasRepository.save(any(Personas.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        PersonaResponseDTO resultado = personaService.actualizarEstadoPersona("persona-1", "Inactivo");

        // Assert
        assertNotNull(resultado);
        assertEquals("Inactivo", resultado.getNombreEstado());
        verify(personasRepository).save(argThat(persona -> persona.getEstados().equals(estadoInactivo)));
    }

    @Test
    @DisplayName("Debe fallar al actualizar estado con persona inexistente")
    void testActualizarEstadoPersona_PersonaNoExiste() {
        // Arrange
        when(personasRepository.findById("persona-inexistente")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> personaService.actualizarEstadoPersona("persona-inexistente", "Inactivo")
        );

        assertEquals("La persona no existe", exception.getMessage());
        verify(personasRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe fallar al actualizar con estado inexistente")
    void testActualizarEstadoPersona_EstadoNoExiste() {
        // Arrange
        when(personasRepository.findById("persona-1")).thenReturn(Optional.of(persona1));
        when(estadosRepository.findFirstByEstado("EstadoInvalido")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> personaService.actualizarEstadoPersona("persona-1", "EstadoInvalido")
        );

        assertEquals("El estado no existe", exception.getMessage());
        verify(personasRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe eliminar persona (soft delete)")
    void testEliminarPersona_Exitoso() {
        // Arrange
        when(personasRepository.findById("persona-1")).thenReturn(Optional.of(persona1));
        when(estadosRepository.findFirstByEstado("Inactivo")).thenReturn(Optional.of(estadoInactivo));
        when(personasRepository.save(any(Personas.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        personaService.eliminarPersona("persona-1");

        // Assert
        verify(personasRepository).save(argThat(persona -> persona.getEstados().equals(estadoInactivo)));
    }

    @Test
    @DisplayName("Debe fallar al eliminar persona inexistente")
    void testEliminarPersona_NoExiste() {
        // Arrange
        when(personasRepository.findById("persona-inexistente")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> personaService.eliminarPersona("persona-inexistente")
        );

        assertEquals("La persona no existe", exception.getMessage());
        verify(personasRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe fallar al eliminar si no existe estado Inactivo")
    void testEliminarPersona_EstadoInactivoNoExiste() {
        // Arrange
        when(personasRepository.findById("persona-1")).thenReturn(Optional.of(persona1));
        when(estadosRepository.findFirstByEstado("Inactivo")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> personaService.eliminarPersona("persona-1")
        );

        assertEquals("No se encontró el estado inactivo", exception.getMessage());
        verify(personasRepository, never()).save(any());
    }
}
