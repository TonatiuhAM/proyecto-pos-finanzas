package com.posfin.pos_finanzas_backend.services;

import com.posfin.pos_finanzas_backend.dtos.OrdenesDeVentasDTO;
import com.posfin.pos_finanzas_backend.models.*;
import com.posfin.pos_finanzas_backend.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de VentaService")
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

    private Personas cliente;
    private Usuarios usuario;
    private MetodosPago metodoPago;
    private Productos producto;
    private HistorialPrecios historialPrecio;
    private OrdenesWorkspace ordenWorkspace;
    private Inventarios inventario;
    private Ubicaciones ubicacion;
    private TipoMovimientos tipoVenta;
    private OrdenesDeVentas ordenVenta;

    @BeforeEach
    void setUp() {
        // Configurar cliente
        cliente = new Personas();
        cliente.setId("cliente-1");
        cliente.setNombre("Cliente");
        cliente.setApellidoPaterno("Test");
        cliente.setApellidoMaterno("Apellido");

        // Configurar usuario
        usuario = new Usuarios();
        usuario.setId("usuario-1");
        usuario.setNombre("vendedor1");

        // Configurar método de pago
        metodoPago = new MetodosPago();
        metodoPago.setId("metodo-1");
        metodoPago.setMetodoPago("Efectivo");

        // Configurar producto
        producto = new Productos();
        producto.setId("producto-1");
        producto.setNombre("Producto Test");

        // Configurar historial de precio
        historialPrecio = new HistorialPrecios();
        historialPrecio.setId("precio-1");
        historialPrecio.setPrecio(new BigDecimal("100.00"));
        historialPrecio.setProductos(producto);

        // Configurar ubicación
        ubicacion = new Ubicaciones();
        ubicacion.setId("ubicacion-1");
        ubicacion.setNombre("Almacén Principal");

        // Configurar inventario
        inventario = new Inventarios();
        inventario.setId("inventario-1");
        inventario.setProducto(producto);
        inventario.setUbicacion(ubicacion);
        inventario.setCantidadPz(100);
        inventario.setCantidadKg(0);

        // Configurar orden workspace
        ordenWorkspace = new OrdenesWorkspace();
        ordenWorkspace.setId("orden-workspace-1");
        ordenWorkspace.setProducto(producto);
        ordenWorkspace.setHistorialPrecio(historialPrecio);
        ordenWorkspace.setCantidadPz(new BigDecimal("5"));
        ordenWorkspace.setCantidadKg(new BigDecimal("0"));

        // Configurar tipo de movimiento
        tipoVenta = new TipoMovimientos();
        tipoVenta.setId("tipo-venta");
        tipoVenta.setMovimiento("VENTA");

        // Configurar orden de venta
        ordenVenta = new OrdenesDeVentas();
        ordenVenta.setId("orden-venta-1");
        ordenVenta.setPersona(cliente);
        ordenVenta.setUsuario(usuario);
        ordenVenta.setMetodoPago(metodoPago);
        ordenVenta.setTotalVenta(new BigDecimal("500.00"));
        ordenVenta.setFechaOrden(OffsetDateTime.now());
    }

    @Test
    @DisplayName("Debe procesar venta desde workspace exitosamente")
    void testProcesarVentaDesdeWorkspace_Exitoso() {
        // Arrange
        List<OrdenesWorkspace> ordenesWorkspace = Arrays.asList(ordenWorkspace);

        when(ordenesWorkspaceRepository.findByWorkspaceId("workspace-1")).thenReturn(ordenesWorkspace);
        when(personasRepository.findById("cliente-1")).thenReturn(Optional.of(cliente));
        when(usuariosRepository.findById("usuario-1")).thenReturn(Optional.of(usuario));
        when(metodosPagoRepository.findById("metodo-1")).thenReturn(Optional.of(metodoPago));
        when(ordenesDeVentasRepository.save(any(OrdenesDeVentas.class))).thenReturn(ordenVenta);
        when(inventarioRepository.findByProducto(producto)).thenReturn(Optional.of(inventario));
        when(tipoMovimientosRepository.findByMovimiento("VENTA")).thenReturn(Optional.of(tipoVenta));

        // Act
        OrdenesDeVentasDTO resultado = ventaService.procesarVentaDesdeWorkspace(
            "workspace-1", "cliente-1", "usuario-1", "metodo-1"
        );

        // Assert
        assertNotNull(resultado);
        assertEquals("orden-venta-1", resultado.getId());
        assertEquals("cliente-1", resultado.getPersonaId());
        assertEquals("usuario-1", resultado.getUsuarioId());
        assertEquals("metodo-1", resultado.getMetodoPagoId());

        verify(ordenesDeVentasRepository, times(1)).save(any(OrdenesDeVentas.class));
        verify(detallesOrdenesDeVentasRepository, times(1)).save(any(DetallesOrdenesDeVentas.class));
        verify(inventarioRepository, times(1)).save(any(Inventarios.class));
        verify(movimientosInventariosRepository, times(1)).save(any(MovimientosInventarios.class));
        verify(ordenesWorkspaceRepository, times(1)).deleteAll(ordenesWorkspace);
    }

    @Test
    @DisplayName("Debe lanzar excepción si no hay productos en el carrito")
    void testProcesarVentaDesdeWorkspace_CarritoVacio() {
        // Arrange
        when(ordenesWorkspaceRepository.findByWorkspaceId("workspace-1"))
            .thenReturn(Collections.emptyList());

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            ventaService.procesarVentaDesdeWorkspace("workspace-1", "cliente-1", "usuario-1", "metodo-1");
        });

        assertEquals("No hay productos en el carrito para procesar la venta", exception.getMessage());
        verify(ordenesDeVentasRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción si el cliente no existe")
    void testProcesarVentaDesdeWorkspace_ClienteNoExiste() {
        // Arrange
        List<OrdenesWorkspace> ordenesWorkspace = Arrays.asList(ordenWorkspace);

        when(ordenesWorkspaceRepository.findByWorkspaceId("workspace-1")).thenReturn(ordenesWorkspace);
        when(personasRepository.findById("cliente-1")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ventaService.procesarVentaDesdeWorkspace("workspace-1", "cliente-1", "usuario-1", "metodo-1");
        });

        assertEquals("Cliente no encontrado: cliente-1", exception.getMessage());
        verify(ordenesDeVentasRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe usar cliente por defecto si no se especifica cliente")
    void testProcesarVentaDesdeWorkspace_ClientePorDefecto() {
        // Arrange
        List<OrdenesWorkspace> ordenesWorkspace = Arrays.asList(ordenWorkspace);

        when(ordenesWorkspaceRepository.findByWorkspaceId("workspace-1")).thenReturn(ordenesWorkspace);
        when(personasRepository.findAll()).thenReturn(Arrays.asList(cliente));
        when(personasRepository.findById(cliente.getId())).thenReturn(Optional.of(cliente));
        when(usuariosRepository.findById("usuario-1")).thenReturn(Optional.of(usuario));
        when(metodosPagoRepository.findById("metodo-1")).thenReturn(Optional.of(metodoPago));
        when(ordenesDeVentasRepository.save(any(OrdenesDeVentas.class))).thenReturn(ordenVenta);
        when(inventarioRepository.findByProducto(producto)).thenReturn(Optional.of(inventario));
        when(tipoMovimientosRepository.findByMovimiento("VENTA")).thenReturn(Optional.of(tipoVenta));

        // Act
        OrdenesDeVentasDTO resultado = ventaService.procesarVentaDesdeWorkspace(
            "workspace-1", null, "usuario-1", "metodo-1"
        );

        // Assert
        assertNotNull(resultado);
        verify(personasRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe lanzar excepción si el usuario no existe")
    void testProcesarVentaDesdeWorkspace_UsuarioNoExiste() {
        // Arrange
        List<OrdenesWorkspace> ordenesWorkspace = Arrays.asList(ordenWorkspace);

        when(ordenesWorkspaceRepository.findByWorkspaceId("workspace-1")).thenReturn(ordenesWorkspace);
        when(personasRepository.findById("cliente-1")).thenReturn(Optional.of(cliente));
        when(usuariosRepository.findById("usuario-1")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ventaService.procesarVentaDesdeWorkspace("workspace-1", "cliente-1", "usuario-1", "metodo-1");
        });

        assertEquals("Usuario no encontrado: usuario-1", exception.getMessage());
        verify(ordenesDeVentasRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción si el método de pago no existe")
    void testProcesarVentaDesdeWorkspace_MetodoPagoNoExiste() {
        // Arrange
        List<OrdenesWorkspace> ordenesWorkspace = Arrays.asList(ordenWorkspace);

        when(ordenesWorkspaceRepository.findByWorkspaceId("workspace-1")).thenReturn(ordenesWorkspace);
        when(personasRepository.findById("cliente-1")).thenReturn(Optional.of(cliente));
        when(usuariosRepository.findById("usuario-1")).thenReturn(Optional.of(usuario));
        when(metodosPagoRepository.findById("metodo-1")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ventaService.procesarVentaDesdeWorkspace("workspace-1", "cliente-1", "usuario-1", "metodo-1");
        });

        assertEquals("Método de pago no encontrado: metodo-1", exception.getMessage());
        verify(ordenesDeVentasRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe calcular total de venta correctamente con múltiples productos")
    void testProcesarVentaDesdeWorkspace_CalculoTotalMultiple() {
        // Arrange
        OrdenesWorkspace ordenWorkspace2 = new OrdenesWorkspace();
        ordenWorkspace2.setId("orden-workspace-2");
        ordenWorkspace2.setProducto(producto);
        
        HistorialPrecios precio2 = new HistorialPrecios();
        precio2.setPrecio(new BigDecimal("50.00"));
        ordenWorkspace2.setHistorialPrecio(precio2);
        ordenWorkspace2.setCantidadPz(new BigDecimal("3"));
        ordenWorkspace2.setCantidadKg(new BigDecimal("2"));

        List<OrdenesWorkspace> ordenesWorkspace = Arrays.asList(ordenWorkspace, ordenWorkspace2);

        when(ordenesWorkspaceRepository.findByWorkspaceId("workspace-1")).thenReturn(ordenesWorkspace);
        when(personasRepository.findById("cliente-1")).thenReturn(Optional.of(cliente));
        when(usuariosRepository.findById("usuario-1")).thenReturn(Optional.of(usuario));
        when(metodosPagoRepository.findById("metodo-1")).thenReturn(Optional.of(metodoPago));
        when(ordenesDeVentasRepository.save(any(OrdenesDeVentas.class))).thenAnswer(invocation -> {
            OrdenesDeVentas orden = invocation.getArgument(0);
            orden.setId("orden-venta-1");
            return orden;
        });
        when(inventarioRepository.findByProducto(producto)).thenReturn(Optional.of(inventario));
        when(tipoMovimientosRepository.findByMovimiento("VENTA")).thenReturn(Optional.of(tipoVenta));

        // Act
        OrdenesDeVentasDTO resultado = ventaService.procesarVentaDesdeWorkspace(
            "workspace-1", "cliente-1", "usuario-1", "metodo-1"
        );

        // Assert
        assertNotNull(resultado);
        // Total esperado: (100 * 5) + (50 * (3 + 2)) = 500 + 250 = 750
        verify(ordenesDeVentasRepository, times(1)).save(argThat(orden ->
            orden.getTotalVenta().compareTo(new BigDecimal("750.00")) == 0
        ));
    }

    @Test
    @DisplayName("Debe actualizar inventario correctamente después de venta")
    void testProcesarVentaDesdeWorkspace_ActualizacionInventario() {
        // Arrange
        List<OrdenesWorkspace> ordenesWorkspace = Arrays.asList(ordenWorkspace);

        when(ordenesWorkspaceRepository.findByWorkspaceId("workspace-1")).thenReturn(ordenesWorkspace);
        when(personasRepository.findById("cliente-1")).thenReturn(Optional.of(cliente));
        when(usuariosRepository.findById("usuario-1")).thenReturn(Optional.of(usuario));
        when(metodosPagoRepository.findById("metodo-1")).thenReturn(Optional.of(metodoPago));
        when(ordenesDeVentasRepository.save(any(OrdenesDeVentas.class))).thenReturn(ordenVenta);
        when(inventarioRepository.findByProducto(producto)).thenReturn(Optional.of(inventario));
        when(tipoMovimientosRepository.findByMovimiento("VENTA")).thenReturn(Optional.of(tipoVenta));

        // Act
        ventaService.procesarVentaDesdeWorkspace("workspace-1", "cliente-1", "usuario-1", "metodo-1");

        // Assert
        verify(inventarioRepository, times(1)).save(argThat(inv ->
            inv.getCantidadPz() == 95 // 100 - 5 = 95
        ));
    }

    @Test
    @DisplayName("Debe lanzar excepción si no existe inventario para el producto")
    void testProcesarVentaDesdeWorkspace_InventarioNoExiste() {
        // Arrange
        List<OrdenesWorkspace> ordenesWorkspace = Arrays.asList(ordenWorkspace);

        when(ordenesWorkspaceRepository.findByWorkspaceId("workspace-1")).thenReturn(ordenesWorkspace);
        when(personasRepository.findById("cliente-1")).thenReturn(Optional.of(cliente));
        when(usuariosRepository.findById("usuario-1")).thenReturn(Optional.of(usuario));
        when(metodosPagoRepository.findById("metodo-1")).thenReturn(Optional.of(metodoPago));
        when(ordenesDeVentasRepository.save(any(OrdenesDeVentas.class))).thenReturn(ordenVenta);
        when(inventarioRepository.findByProducto(producto)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            ventaService.procesarVentaDesdeWorkspace("workspace-1", "cliente-1", "usuario-1", "metodo-1");
        });

        assertEquals("No se encontró inventario para el producto: Producto Test", exception.getMessage());
    }

    @Test
    @DisplayName("Debe prevenir inventario negativo")
    void testProcesarVentaDesdeWorkspace_PrevenirInventarioNegativo() {
        // Arrange
        inventario.setCantidadPz(3); // Stock menor que la venta
        ordenWorkspace.setCantidadPz(new BigDecimal("5"));

        List<OrdenesWorkspace> ordenesWorkspace = Arrays.asList(ordenWorkspace);

        when(ordenesWorkspaceRepository.findByWorkspaceId("workspace-1")).thenReturn(ordenesWorkspace);
        when(personasRepository.findById("cliente-1")).thenReturn(Optional.of(cliente));
        when(usuariosRepository.findById("usuario-1")).thenReturn(Optional.of(usuario));
        when(metodosPagoRepository.findById("metodo-1")).thenReturn(Optional.of(metodoPago));
        when(ordenesDeVentasRepository.save(any(OrdenesDeVentas.class))).thenReturn(ordenVenta);
        when(inventarioRepository.findByProducto(producto)).thenReturn(Optional.of(inventario));
        when(tipoMovimientosRepository.findByMovimiento("VENTA")).thenReturn(Optional.of(tipoVenta));

        // Act
        ventaService.procesarVentaDesdeWorkspace("workspace-1", "cliente-1", "usuario-1", "metodo-1");

        // Assert - debe quedar en 0, no negativo
        verify(inventarioRepository, times(1)).save(argThat(inv ->
            inv.getCantidadPz() == 0
        ));
    }

    @Test
    @DisplayName("Debe crear movimiento de inventario correctamente")
    void testProcesarVentaDesdeWorkspace_MovimientoInventario() {
        // Arrange
        List<OrdenesWorkspace> ordenesWorkspace = Arrays.asList(ordenWorkspace);

        when(ordenesWorkspaceRepository.findByWorkspaceId("workspace-1")).thenReturn(ordenesWorkspace);
        when(personasRepository.findById("cliente-1")).thenReturn(Optional.of(cliente));
        when(usuariosRepository.findById("usuario-1")).thenReturn(Optional.of(usuario));
        when(metodosPagoRepository.findById("metodo-1")).thenReturn(Optional.of(metodoPago));
        when(ordenesDeVentasRepository.save(any(OrdenesDeVentas.class))).thenReturn(ordenVenta);
        when(inventarioRepository.findByProducto(producto)).thenReturn(Optional.of(inventario));
        when(tipoMovimientosRepository.findByMovimiento("VENTA")).thenReturn(Optional.of(tipoVenta));

        // Act
        ventaService.procesarVentaDesdeWorkspace("workspace-1", "cliente-1", "usuario-1", "metodo-1");

        // Assert
        verify(movimientosInventariosRepository, times(1)).save(argThat(mov ->
            mov.getTipoMovimiento().equals(tipoVenta) &&
            mov.getCantidad().compareTo(new BigDecimal("5")) == 0 &&
            mov.getClaveMovimiento().startsWith("VENTA-")
        ));
    }

    @Test
    @DisplayName("Debe lanzar excepción si no existe tipo de movimiento VENTA")
    void testProcesarVentaDesdeWorkspace_TipoVentaNoExiste() {
        // Arrange
        List<OrdenesWorkspace> ordenesWorkspace = Arrays.asList(ordenWorkspace);

        when(ordenesWorkspaceRepository.findByWorkspaceId("workspace-1")).thenReturn(ordenesWorkspace);
        when(personasRepository.findById("cliente-1")).thenReturn(Optional.of(cliente));
        when(usuariosRepository.findById("usuario-1")).thenReturn(Optional.of(usuario));
        when(metodosPagoRepository.findById("metodo-1")).thenReturn(Optional.of(metodoPago));
        when(ordenesDeVentasRepository.save(any(OrdenesDeVentas.class))).thenReturn(ordenVenta);
        when(inventarioRepository.findByProducto(producto)).thenReturn(Optional.of(inventario));
        when(tipoMovimientosRepository.findByMovimiento("VENTA")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            ventaService.procesarVentaDesdeWorkspace("workspace-1", "cliente-1", "usuario-1", "metodo-1");
        });

        assertEquals("Tipo de movimiento 'VENTA' no encontrado en el sistema", exception.getMessage());
    }

    @Test
    @DisplayName("Debe limpiar workspace después de procesar venta")
    void testProcesarVentaDesdeWorkspace_LimpiarWorkspace() {
        // Arrange
        List<OrdenesWorkspace> ordenesWorkspace = Arrays.asList(ordenWorkspace);

        when(ordenesWorkspaceRepository.findByWorkspaceId("workspace-1")).thenReturn(ordenesWorkspace);
        when(personasRepository.findById("cliente-1")).thenReturn(Optional.of(cliente));
        when(usuariosRepository.findById("usuario-1")).thenReturn(Optional.of(usuario));
        when(metodosPagoRepository.findById("metodo-1")).thenReturn(Optional.of(metodoPago));
        when(ordenesDeVentasRepository.save(any(OrdenesDeVentas.class))).thenReturn(ordenVenta);
        when(inventarioRepository.findByProducto(producto)).thenReturn(Optional.of(inventario));
        when(tipoMovimientosRepository.findByMovimiento("VENTA")).thenReturn(Optional.of(tipoVenta));

        // Act
        ventaService.procesarVentaDesdeWorkspace("workspace-1", "cliente-1", "usuario-1", "metodo-1");

        // Assert
        verify(ordenesWorkspaceRepository, times(1)).deleteAll(ordenesWorkspace);
    }

    @Test
    @DisplayName("Debe obtener cliente por defecto correctamente")
    void testObtenerClientePorDefecto_Exitoso() {
        // Arrange
        when(personasRepository.findAll()).thenReturn(Arrays.asList(cliente));

        // Act
        String clienteId = ventaService.obtenerClientePorDefecto();

        // Assert
        assertNotNull(clienteId);
        assertEquals("cliente-1", clienteId);
        verify(personasRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe lanzar excepción si no hay clientes registrados")
    void testObtenerClientePorDefecto_SinClientes() {
        // Arrange
        when(personasRepository.findAll()).thenReturn(Collections.emptyList());

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            ventaService.obtenerClientePorDefecto();
        });

        assertEquals("No hay clientes registrados en el sistema", exception.getMessage());
    }
}
