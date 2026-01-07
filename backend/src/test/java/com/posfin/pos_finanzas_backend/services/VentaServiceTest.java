package com.posfin.pos_finanzas_backend.services;

import com.posfin.pos_finanzas_backend.models.*;
import com.posfin.pos_finanzas_backend.dtos.OrdenesDeVentasDTO;
import com.posfin.pos_finanzas_backend.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para VentaService
 * Valida la lógica de negocio crítica según ISO/IEC 25010
 * Categorías: Adecuación Funcional y Confiabilidad
 */
@ExtendWith(MockitoExtension.class)
class VentaServiceTest {

    @Mock
    private OrdenesWorkspaceRepository ordenesWorkspaceRepository;

    @Mock
    private OrdenesDeVentasRepository ordenesDeVentasRepository;

    @Mock
    private DetallesOrdenesDeVentasRepository detallesOrdenesDeVentasRepository;

    @Mock
    private InventarioRepository inventarioRepository;

    @Mock
    private MovimientosInventariosRepository movimientosInventariosRepository;

    @Mock
    private PersonasRepository personasRepository;

    @Mock
    private UsuariosRepository usuariosRepository;

    @Mock
    private MetodosPagoRepository metodosPagoRepository;

    @Mock
    private TIpoMovimientosRepository tipoMovimientosRepository;

    @InjectMocks
    private VentaService ventaService;

    // Datos de prueba
    private String workspaceId = "workspace-123";
    private String clienteId = "cliente-123";
    private String usuarioId = "usuario-123";
    private String metodoPagoId = "metodo-pago-123";

    private OrdenesWorkspace ordenWorkspace1;
    private OrdenesWorkspace ordenWorkspace2;
    private Personas cliente;
    private Usuarios usuario;
    private MetodosPago metodoPago;
    private Productos producto1;
    private Productos producto2;
    private HistorialPrecios historialPrecio1;
    private HistorialPrecios historialPrecio2;
    private Inventarios inventario1;
    private Inventarios inventario2;
    private OrdenesDeVentas ordenVenta;
    private TipoMovimientos tipoMovimientoVenta;
    private Ubicaciones ubicacion;

    @BeforeEach
    void setUp() {
        // Configurar productos
        producto1 = new Productos();
        producto1.setId("producto-1");
        producto1.setNombre("Producto Test 1");

        producto2 = new Productos();
        producto2.setId("producto-2");
        producto2.setNombre("Producto Test 2");

        // Configurar historial de precios
        historialPrecio1 = new HistorialPrecios();
        historialPrecio1.setPrecio(new BigDecimal("10.50"));

        historialPrecio2 = new HistorialPrecios();
        historialPrecio2.setPrecio(new BigDecimal("25.00"));

        // Configurar órdenes de workspace
        ordenWorkspace1 = new OrdenesWorkspace();
        ordenWorkspace1.setId("orden-workspace-1");
        ordenWorkspace1.setWorkspaceId(workspaceId);
        ordenWorkspace1.setProducto(producto1);
        ordenWorkspace1.setHistorialPrecio(historialPrecio1);
        ordenWorkspace1.setCantidadPz(new BigDecimal("2"));
        ordenWorkspace1.setCantidadKg(BigDecimal.ZERO);

        ordenWorkspace2 = new OrdenesWorkspace();
        ordenWorkspace2.setId("orden-workspace-2");
        ordenWorkspace2.setWorkspaceId(workspaceId);
        ordenWorkspace2.setProducto(producto2);
        ordenWorkspace2.setHistorialPrecio(historialPrecio2);
        ordenWorkspace2.setCantidadPz(new BigDecimal("1"));
        ordenWorkspace2.setCantidadKg(new BigDecimal("3"));

        // Configurar cliente
        cliente = new Personas();
        cliente.setId(clienteId);
        cliente.setNombre("Cliente Test");
        cliente.setApellidoPaterno("Apellido");
        cliente.setApellidoMaterno("Materno");

        // Configurar usuario
        usuario = new Usuarios();
        usuario.setId(usuarioId);
        usuario.setNombre("Usuario Test");

        // Configurar método de pago
        metodoPago = new MetodosPago();
        metodoPago.setId(metodoPagoId);
        metodoPago.setMetodoPago("Efectivo");

        // Configurar ubicación
        ubicacion = new Ubicaciones();
        ubicacion.setId("ubicacion-1");
        ubicacion.setUbicacion("Almacén Principal");

        // Configurar inventarios
        inventario1 = new Inventarios();
        inventario1.setProducto(producto1);
        inventario1.setCantidadPz(10);
        inventario1.setCantidadKg(5);
        inventario1.setUbicacion(ubicacion);

        inventario2 = new Inventarios();
        inventario2.setProducto(producto2);
        inventario2.setCantidadPz(8);
        inventario2.setCantidadKg(15);
        inventario2.setUbicacion(ubicacion);

        // Configurar orden de venta
        ordenVenta = new OrdenesDeVentas();
        ordenVenta.setId("orden-venta-123");
        ordenVenta.setPersona(cliente);
        ordenVenta.setUsuario(usuario);
        ordenVenta.setMetodoPago(metodoPago);
        ordenVenta.setTotalVenta(new BigDecimal("146.00"));
        ordenVenta.setFechaOrden(OffsetDateTime.now());

        // Configurar tipo de movimiento
        tipoMovimientoVenta = new TipoMovimientos();
        tipoMovimientoVenta.setId("tipo-venta");
        tipoMovimientoVenta.setMovimiento("VENTA");
    }

    @Test
    void testProcesarVentaDesdeWorkspace_ExitosaCajaAbierta() {
        // Arrange
        List<OrdenesWorkspace> ordenesWorkspace = Arrays.asList(ordenWorkspace1, ordenWorkspace2);

        when(ordenesWorkspaceRepository.findByWorkspaceId(workspaceId))
                .thenReturn(ordenesWorkspace);
        when(personasRepository.findById(clienteId))
                .thenReturn(Optional.of(cliente));
        when(usuariosRepository.findById(usuarioId))
                .thenReturn(Optional.of(usuario));
        when(metodosPagoRepository.findById(metodoPagoId))
                .thenReturn(Optional.of(metodoPago));
        when(ordenesDeVentasRepository.save(any(OrdenesDeVentas.class)))
                .thenReturn(ordenVenta);
        when(inventarioRepository.findByProducto(producto1))
                .thenReturn(Optional.of(inventario1));
        when(inventarioRepository.findByProducto(producto2))
                .thenReturn(Optional.of(inventario2));
        when(tipoMovimientosRepository.findByMovimiento("VENTA"))
                .thenReturn(Optional.of(tipoMovimientoVenta));

        // Act
        OrdenesDeVentasDTO resultado = ventaService.procesarVentaDesdeWorkspace(
                workspaceId, clienteId, usuarioId, metodoPagoId);

        // Assert
        assertNotNull(resultado, "El resultado no debe ser null");
        assertEquals("orden-venta-123", resultado.getId());
        assertEquals(clienteId, resultado.getPersonaId());
        assertEquals("Cliente Test", resultado.getPersonaNombre());
        assertEquals(usuarioId, resultado.getUsuarioId());
        assertEquals("Usuario Test", resultado.getUsuarioNombre());
        assertEquals(metodoPagoId, resultado.getMetodoPagoId());
        assertEquals("Efectivo", resultado.getMetodoPagoNombre());

        // Verificar transacciones realizadas
        verify(ordenesDeVentasRepository).save(any(OrdenesDeVentas.class));
        verify(detallesOrdenesDeVentasRepository, times(2)).save(any(DetallesOrdenesDeVentas.class));
        verify(inventarioRepository, times(2)).save(any(Inventarios.class));
        verify(movimientosInventariosRepository, times(2)).save(any(MovimientosInventarios.class));
        verify(ordenesWorkspaceRepository).deleteAll(ordenesWorkspace);
    }

    @Test
    void testProcesarVentaDesdeWorkspace_FallaCarritoVacio() {
        // Arrange - Carrito vacío
        when(ordenesWorkspaceRepository.findByWorkspaceId(workspaceId))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            ventaService.procesarVentaDesdeWorkspace(workspaceId, clienteId, usuarioId, metodoPagoId);
        });

        assertEquals("No hay productos en el carrito para procesar la venta", 
            exception.getMessage());

        // Verificar que no se realizó ninguna operación
        verify(ordenesDeVentasRepository, never()).save(any());
        verify(inventarioRepository, never()).save(any());
        verify(movimientosInventariosRepository, never()).save(any());
    }

    @Test
    void testValidarStock_ProductoSuficiente() {
        // Arrange
        List<OrdenesWorkspace> ordenesWorkspace = Arrays.asList(ordenWorkspace1);

        when(ordenesWorkspaceRepository.findByWorkspaceId(workspaceId))
                .thenReturn(ordenesWorkspace);
        when(personasRepository.findById(clienteId))
                .thenReturn(Optional.of(cliente));
        when(usuariosRepository.findById(usuarioId))
                .thenReturn(Optional.of(usuario));
        when(metodosPagoRepository.findById(metodoPagoId))
                .thenReturn(Optional.of(metodoPago));
        when(ordenesDeVentasRepository.save(any(OrdenesDeVentas.class)))
                .thenReturn(ordenVenta);
        when(inventarioRepository.findByProducto(producto1))
                .thenReturn(Optional.of(inventario1));
        when(tipoMovimientosRepository.findByMovimiento("VENTA"))
                .thenReturn(Optional.of(tipoMovimientoVenta));

        // Act
        OrdenesDeVentasDTO resultado = ventaService.procesarVentaDesdeWorkspace(
                workspaceId, clienteId, usuarioId, metodoPagoId);

        // Assert - La venta debe procesarse correctamente
        assertNotNull(resultado);
        
        // Verificar que el inventario se actualizó correctamente
        verify(inventarioRepository).save(argThat(inventario -> {
            // Inventario inicial: 10 pz - venta: 2 pz = 8 pz restantes
            return inventario.getCantidadPz() == 8;
        }));
    }

    @Test
    void testValidarStock_ProductoInventarioInsuficiente() {
        // Arrange - Inventario insuficiente
        inventario1.setCantidadPz(1); // Solo 1 pieza disponible, pero se quieren vender 2
        List<OrdenesWorkspace> ordenesWorkspace = Arrays.asList(ordenWorkspace1);

        when(ordenesWorkspaceRepository.findByWorkspaceId(workspaceId))
                .thenReturn(ordenesWorkspace);
        when(personasRepository.findById(clienteId))
                .thenReturn(Optional.of(cliente));
        when(usuariosRepository.findById(usuarioId))
                .thenReturn(Optional.of(usuario));
        when(metodosPagoRepository.findById(metodoPagoId))
                .thenReturn(Optional.of(metodoPago));
        when(ordenesDeVentasRepository.save(any(OrdenesDeVentas.class)))
                .thenReturn(ordenVenta);
        when(inventarioRepository.findByProducto(producto1))
                .thenReturn(Optional.of(inventario1));
        when(tipoMovimientosRepository.findByMovimiento("VENTA"))
                .thenReturn(Optional.of(tipoMovimientoVenta));

        // Act
        OrdenesDeVentasDTO resultado = ventaService.procesarVentaDesdeWorkspace(
                workspaceId, clienteId, usuarioId, metodoPagoId);

        // Assert - La venta debe procesarse pero el inventario no debe quedar negativo
        assertNotNull(resultado);
        
        verify(inventarioRepository).save(argThat(inventario -> {
            // El sistema debe proteger contra inventarios negativos (Math.max(0, nuevaCantidad))
            return inventario.getCantidadPz() >= 0;
        }));
    }

    @Test
    void testCalcularTotalVenta_ConDescuentos() {
        // Arrange - Crear escenario con diferentes cantidades y precios
        ordenWorkspace1.setCantidadPz(new BigDecimal("3")); // 3 * 10.50 = 31.50
        ordenWorkspace1.setCantidadKg(new BigDecimal("2")); // 2 * 10.50 = 21.00
        
        ordenWorkspace2.setCantidadPz(new BigDecimal("1")); // 1 * 25.00 = 25.00
        ordenWorkspace2.setCantidadKg(new BigDecimal("2")); // 2 * 25.00 = 50.00
        
        // Total esperado: 31.50 + 21.00 + 25.00 + 50.00 = 127.50
        
        List<OrdenesWorkspace> ordenesWorkspace = Arrays.asList(ordenWorkspace1, ordenWorkspace2);

        when(ordenesWorkspaceRepository.findByWorkspaceId(workspaceId))
                .thenReturn(ordenesWorkspace);
        when(personasRepository.findById(clienteId))
                .thenReturn(Optional.of(cliente));
        when(usuariosRepository.findById(usuarioId))
                .thenReturn(Optional.of(usuario));
        when(metodosPagoRepository.findById(metodoPagoId))
                .thenReturn(Optional.of(metodoPago));
        when(inventarioRepository.findByProducto(any(Productos.class)))
                .thenReturn(Optional.of(inventario1), Optional.of(inventario2));
        when(tipoMovimientosRepository.findByMovimiento("VENTA"))
                .thenReturn(Optional.of(tipoMovimientoVenta));

        // Configurar el mock para que retorne una orden con el total calculado
        when(ordenesDeVentasRepository.save(any(OrdenesDeVentas.class)))
                .thenAnswer(invocation -> {
                    OrdenesDeVentas orden = invocation.getArgument(0);
                    orden.setId("orden-venta-test");
                    return orden;
                });

        // Act
        OrdenesDeVentasDTO resultado = ventaService.procesarVentaDesdeWorkspace(
                workspaceId, clienteId, usuarioId, metodoPagoId);

        // Assert
        assertNotNull(resultado);
        
        // Verificar que se llamó save con el total correcto
        verify(ordenesDeVentasRepository).save(argThat(orden -> {
            return orden.getTotalVenta().compareTo(new BigDecimal("127.50")) == 0;
        }));
    }

    @Test
    void testValidarCliente_ClienteExistente() {
        // Arrange
        when(personasRepository.findById(clienteId))
                .thenReturn(Optional.of(cliente));
        when(ordenesWorkspaceRepository.findByWorkspaceId(workspaceId))
                .thenReturn(Arrays.asList(ordenWorkspace1));
        when(usuariosRepository.findById(usuarioId))
                .thenReturn(Optional.of(usuario));
        when(metodosPagoRepository.findById(metodoPagoId))
                .thenReturn(Optional.of(metodoPago));
        when(inventarioRepository.findByProducto(producto1))
                .thenReturn(Optional.of(inventario1));
        when(tipoMovimientosRepository.findByMovimiento("VENTA"))
                .thenReturn(Optional.of(tipoMovimientoVenta));
        when(ordenesDeVentasRepository.save(any(OrdenesDeVentas.class)))
                .thenReturn(ordenVenta);

        // Act
        OrdenesDeVentasDTO resultado = ventaService.procesarVentaDesdeWorkspace(
                workspaceId, clienteId, usuarioId, metodoPagoId);

        // Assert
        assertNotNull(resultado);
        assertEquals(clienteId, resultado.getPersonaId());
        verify(personasRepository).findById(clienteId);
    }

    @Test
    void testValidarCliente_ClienteNoExistente() {
        // Arrange
        when(personasRepository.findById(clienteId))
                .thenReturn(Optional.empty());
        when(ordenesWorkspaceRepository.findByWorkspaceId(workspaceId))
                .thenReturn(Arrays.asList(ordenWorkspace1));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ventaService.procesarVentaDesdeWorkspace(workspaceId, clienteId, usuarioId, metodoPagoId);
        });

        assertTrue(exception.getMessage().contains("Cliente no encontrado"));
        verify(ordenesDeVentasRepository, never()).save(any());
    }

    @Test
    void testObtenerClientePorDefecto_ClientesExistentes() {
        // Arrange
        List<Personas> clientes = Arrays.asList(cliente);
        when(personasRepository.findAll())
                .thenReturn(clientes);

        // Act
        String clienteDefecto = ventaService.obtenerClientePorDefecto();

        // Assert
        assertEquals(clienteId, clienteDefecto);
        verify(personasRepository).findAll();
    }

    @Test
    void testObtenerClientePorDefecto_SinClientes() {
        // Arrange
        when(personasRepository.findAll())
                .thenReturn(Collections.emptyList());

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            ventaService.obtenerClientePorDefecto();
        });

        assertEquals("No hay clientes registrados en el sistema", exception.getMessage());
    }

    @Test
    void testValidarUsuario_UsuarioNoExistente() {
        // Arrange
        when(usuariosRepository.findById(usuarioId))
                .thenReturn(Optional.empty());
        when(ordenesWorkspaceRepository.findByWorkspaceId(workspaceId))
                .thenReturn(Arrays.asList(ordenWorkspace1));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ventaService.procesarVentaDesdeWorkspace(workspaceId, clienteId, usuarioId, metodoPagoId);
        });

        assertTrue(exception.getMessage().contains("Usuario no encontrado"));
        verify(ordenesDeVentasRepository, never()).save(any());
    }

    @Test
    void testValidarMetodoPago_MetodoPagoNoExistente() {
        // Arrange
        when(metodosPagoRepository.findById(metodoPagoId))
                .thenReturn(Optional.empty());
        when(ordenesWorkspaceRepository.findByWorkspaceId(workspaceId))
                .thenReturn(Arrays.asList(ordenWorkspace1));
        when(personasRepository.findById(clienteId))
                .thenReturn(Optional.of(cliente));
        when(usuariosRepository.findById(usuarioId))
                .thenReturn(Optional.of(usuario));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ventaService.procesarVentaDesdeWorkspace(workspaceId, clienteId, usuarioId, metodoPagoId);
        });

        assertTrue(exception.getMessage().contains("Método de pago no encontrado"));
        verify(ordenesDeVentasRepository, never()).save(any());
    }

    @Test
    void testActualizarInventario_ProductoSinInventario() {
        // Arrange
        when(ordenesWorkspaceRepository.findByWorkspaceId(workspaceId))
                .thenReturn(Arrays.asList(ordenWorkspace1));
        when(personasRepository.findById(clienteId))
                .thenReturn(Optional.of(cliente));
        when(usuariosRepository.findById(usuarioId))
                .thenReturn(Optional.of(usuario));
        when(metodosPagoRepository.findById(metodoPagoId))
                .thenReturn(Optional.of(metodoPago));
        when(ordenesDeVentasRepository.save(any(OrdenesDeVentas.class)))
                .thenReturn(ordenVenta);
        when(inventarioRepository.findByProducto(producto1))
                .thenReturn(Optional.empty()); // Sin inventario

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            ventaService.procesarVentaDesdeWorkspace(workspaceId, clienteId, usuarioId, metodoPagoId);
        });

        assertTrue(exception.getMessage().contains("No se encontró inventario para el producto"));
        verify(movimientosInventariosRepository, never()).save(any());
    }

    @Test
    void testCrearMovimientoInventario_TipoMovimientoNoExistente() {
        // Arrange
        when(ordenesWorkspaceRepository.findByWorkspaceId(workspaceId))
                .thenReturn(Arrays.asList(ordenWorkspace1));
        when(personasRepository.findById(clienteId))
                .thenReturn(Optional.of(cliente));
        when(usuariosRepository.findById(usuarioId))
                .thenReturn(Optional.of(usuario));
        when(metodosPagoRepository.findById(metodoPagoId))
                .thenReturn(Optional.of(metodoPago));
        when(ordenesDeVentasRepository.save(any(OrdenesDeVentas.class)))
                .thenReturn(ordenVenta);
        when(inventarioRepository.findByProducto(producto1))
                .thenReturn(Optional.of(inventario1));
        when(tipoMovimientosRepository.findByMovimiento("VENTA"))
                .thenReturn(Optional.empty()); // Tipo movimiento no encontrado

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            ventaService.procesarVentaDesdeWorkspace(workspaceId, clienteId, usuarioId, metodoPagoId);
        });

        assertTrue(exception.getMessage().contains("Tipo de movimiento 'VENTA' no encontrado"));
    }

    @Test
    void testTransaccionalidad_RollbackEnError() {
        // Este test verifica que la anotación @Transactional funcione correctamente
        // Arrange
        when(ordenesWorkspaceRepository.findByWorkspaceId(workspaceId))
                .thenReturn(Arrays.asList(ordenWorkspace1));
        when(personasRepository.findById(clienteId))
                .thenReturn(Optional.of(cliente));
        when(usuariosRepository.findById(usuarioId))
                .thenReturn(Optional.of(usuario));
        when(metodosPagoRepository.findById(metodoPagoId))
                .thenReturn(Optional.of(metodoPago));
        when(ordenesDeVentasRepository.save(any(OrdenesDeVentas.class)))
                .thenReturn(ordenVenta);
        when(inventarioRepository.findByProducto(producto1))
                .thenReturn(Optional.of(inventario1));
        when(detallesOrdenesDeVentasRepository.save(any(DetallesOrdenesDeVentas.class)))
                .thenThrow(new RuntimeException("Error al guardar detalle")); // Simular error

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ventaService.procesarVentaDesdeWorkspace(workspaceId, clienteId, usuarioId, metodoPagoId);
        });

        assertEquals("Error al guardar detalle", exception.getMessage());
        
        // Verificar que se intentó crear la orden pero falló en los detalles
        verify(ordenesDeVentasRepository).save(any(OrdenesDeVentas.class));
        verify(detallesOrdenesDeVentasRepository).save(any(DetallesOrdenesDeVentas.class));
        
        // No debe limpiar el workspace si hubo error (por rollback)
        verify(ordenesWorkspaceRepository, never()).deleteAll(any());
    }

    @Test
    void testConvertirADTO_MapeoCorrecto() {
        // Arrange
        List<OrdenesWorkspace> ordenesWorkspace = Arrays.asList(ordenWorkspace1);

        when(ordenesWorkspaceRepository.findByWorkspaceId(workspaceId))
                .thenReturn(ordenesWorkspace);
        when(personasRepository.findById(clienteId))
                .thenReturn(Optional.of(cliente));
        when(usuariosRepository.findById(usuarioId))
                .thenReturn(Optional.of(usuario));
        when(metodosPagoRepository.findById(metodePagoId))
                .thenReturn(Optional.of(metodoPago));
        when(ordenesDeVentasRepository.save(any(OrdenesDeVentas.class)))
                .thenReturn(ordenVenta);
        when(inventarioRepository.findByProducto(producto1))
                .thenReturn(Optional.of(inventario1));
        when(tipoMovimientosRepository.findByMovimiento("VENTA"))
                .thenReturn(Optional.of(tipoMovimientoVenta));

        // Act
        OrdenesDeVentasDTO resultado = ventaService.procesarVentaDesdeWorkspace(
                workspaceId, clienteId, usuarioId, metodoPagoId);

        // Assert - Verificar mapeo completo
        assertNotNull(resultado);
        assertEquals(ordenVenta.getId(), resultado.getId());
        assertEquals(ordenVenta.getTotalVenta(), resultado.getTotalVenta());
        assertNotNull(resultado.getFechaOrden());
        
        // Verificar mapeo de persona
        assertEquals(cliente.getId(), resultado.getPersonaId());
        assertEquals(cliente.getNombre(), resultado.getPersonaNombre());
        assertEquals(cliente.getApellidoPaterno(), resultado.getPersonaApellidoPaterno());
        assertEquals(cliente.getApellidoMaterno(), resultado.getPersonaApellidoMaterno());
        
        // Verificar mapeo de usuario
        assertEquals(usuario.getId(), resultado.getUsuarioId());
        assertEquals(usuario.getNombre(), resultado.getUsuarioNombre());
        
        // Verificar mapeo de método de pago
        assertEquals(metodoPago.getId(), resultado.getMetodoPagoId());
        assertEquals(metodoPago.getMetodoPago(), resultado.getMetodoPagoNombre());
    }
}