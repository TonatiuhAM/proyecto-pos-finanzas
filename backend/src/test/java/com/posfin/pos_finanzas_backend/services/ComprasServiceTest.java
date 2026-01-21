package com.posfin.pos_finanzas_backend.services;

import com.posfin.pos_finanzas_backend.dtos.*;
import com.posfin.pos_finanzas_backend.models.*;
import com.posfin.pos_finanzas_backend.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de ComprasService")
class ComprasServiceTest {

    @Mock
    private PersonasRepository personasRepository;

    @Mock
    private OrdenesDeComprasRepository ordenesDeComprasRepository;

    @Mock
    private DetallesOrdenesDeComprasRepository detallesOrdenesDeComprasRepository;

    @Mock
    private HistorialCargosProveedoresRepository historialCargosProveedoresRepository;

    @Mock
    private ProductosRepository productosRepository;

    @Mock
    private EstadosRepository estadosRepository;

    @Mock
    private MetodosPagoRepository metodosPagoRepository;

    @Mock
    private HistorialCostosRepository historialCostosRepository;

    @Mock
    private InventarioRepository inventarioRepository;

    @Mock
    private MovimientosInventariosRepository movimientosInventariosRepository;

    @Mock
    private TIpoMovimientosRepository tipoMovimientosRepository;

    @Mock
    private UbicacionesRepository ubicacionesRepository;

    @Mock
    private UsuariosRepository usuariosRepository;

    @InjectMocks
    private ComprasService comprasService;

    private Personas proveedor;
    private Productos producto;
    private Estados estadoActivo;
    private MetodosPago metodoPagoEfectivo;
    private HistorialCostos historialCosto;
    private Inventarios inventario;
    private OrdenesDeCompras ordenCompra;
    private Usuarios usuario;
    private TipoMovimientos tipoMovimientoCompra;
    private Ubicaciones ubicacion;

    @BeforeEach
    void setUp() {
        // Configurar proveedor
        proveedor = new Personas();
        proveedor.setId("proveedor-1");
        proveedor.setNombre("Proveedor");
        proveedor.setApellidoPaterno("Test");
        proveedor.setTelefono("1234567890");

        Estados estadoActivoPersona = new Estados();
        estadoActivoPersona.setEstado("Activo");
        proveedor.setEstados(estadoActivoPersona);

        // Configurar producto
        producto = new Productos();
        producto.setId("producto-1");
        producto.setNombre("Producto Test");

        // Configurar estado activo
        estadoActivo = new Estados();
        estadoActivo.setId("estado-activo");
        estadoActivo.setEstado("Activo");

        // Configurar método de pago
        metodoPagoEfectivo = new MetodosPago();
        metodoPagoEfectivo.setId("metodo-efectivo");
        metodoPagoEfectivo.setMetodoPago("Efectivo");

        // Configurar historial de costos
        historialCosto = new HistorialCostos();
        historialCosto.setId("historial-1");
        historialCosto.setProductos(producto);
        historialCosto.setCosto(new BigDecimal("10.00"));

        // Configurar inventario
        inventario = new Inventarios();
        inventario.setId("inventario-1");
        inventario.setProducto(producto);
        inventario.setCantidadPz(100);
        inventario.setCantidadKg(0);

        // Configurar orden de compra
        ordenCompra = new OrdenesDeCompras();
        ordenCompra.setId("orden-1");
        ordenCompra.setPersona(proveedor);
        ordenCompra.setFechaOrden(OffsetDateTime.now());
        ordenCompra.setEstado(estadoActivo);
        ordenCompra.setTotalCompra(new BigDecimal("500.00"));

        // Configurar usuario
        usuario = new Usuarios();
        usuario.setId("usuario-1");
        usuario.setNombre("admin");

        // Configurar tipo de movimiento
        tipoMovimientoCompra = new TipoMovimientos();
        tipoMovimientoCompra.setId("tipo-compra");
        tipoMovimientoCompra.setMovimiento("Compra");

        // Configurar ubicación
        ubicacion = new Ubicaciones();
        ubicacion.setId("ubicacion-1");
        ubicacion.setUbicacion("Almacén Principal");
    }

    @Test
    @DisplayName("Debe obtener proveedores activos con sus deudas")
    void testObtenerProveedores_Exitoso() {
        // Arrange
        when(personasRepository.findProveedoresActivos()).thenReturn(Arrays.asList(proveedor));
        when(personasRepository.findById("proveedor-1")).thenReturn(Optional.of(proveedor));
        when(ordenesDeComprasRepository.calcularTotalComprasProveedor("proveedor-1"))
                .thenReturn(new BigDecimal("1000.00"));
        when(historialCargosProveedoresRepository.calcularTotalPagosProveedor("proveedor-1"))
                .thenReturn(new BigDecimal("600.00"));

        // Act
        List<ProveedorDTO> resultado = comprasService.obtenerProveedores();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        ProveedorDTO proveedorDTO = resultado.get(0);
        assertEquals("proveedor-1", proveedorDTO.getId());
        assertEquals("Proveedor", proveedorDTO.getNombre());
        assertEquals(new BigDecimal("400.00"), proveedorDTO.getDeudaPendiente());
    }

    @Test
    @DisplayName("Debe calcular deuda de proveedor correctamente")
    void testCalcularDeudaProveedor_Exitoso() {
        // Arrange
        when(personasRepository.findById("proveedor-1")).thenReturn(Optional.of(proveedor));
        when(ordenesDeComprasRepository.calcularTotalComprasProveedor("proveedor-1"))
                .thenReturn(new BigDecimal("2500.00"));
        when(historialCargosProveedoresRepository.calcularTotalPagosProveedor("proveedor-1"))
                .thenReturn(new BigDecimal("1800.00"));

        // Act
        DeudaProveedorDTO resultado = comprasService.calcularDeudaProveedor("proveedor-1");

        // Assert
        assertNotNull(resultado);
        assertEquals("proveedor-1", resultado.getProveedorId());
        assertEquals("Proveedor", resultado.getProveedorNombre());
        assertEquals(new BigDecimal("2500.00"), resultado.getTotalCompras());
        assertEquals(new BigDecimal("1800.00"), resultado.getTotalPagos());
        assertEquals(new BigDecimal("700.00"), resultado.getDeudaPendiente());
    }

    @Test
    @DisplayName("Debe fallar al calcular deuda de proveedor inexistente")
    void testCalcularDeudaProveedor_ProveedorNoExiste() {
        // Arrange
        when(personasRepository.findById("proveedor-inexistente")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> comprasService.calcularDeudaProveedor("proveedor-inexistente")
        );

        assertEquals("Proveedor no encontrado: proveedor-inexistente", exception.getMessage());
    }

    @Test
    @DisplayName("Debe obtener productos de proveedor con precios y stock")
    void testObtenerProductosProveedor_Exitoso() {
        // Arrange
        when(productosRepository.findByProveedorIdAndEstadoActivo("proveedor-1"))
                .thenReturn(Arrays.asList(producto));
        when(historialCostosRepository.findLatestByProducto(producto))
                .thenReturn(Optional.of(historialCosto));
        when(inventarioRepository.findByProducto(producto))
                .thenReturn(Optional.of(inventario));

        Estados estadoActivoProducto = new Estados();
        estadoActivoProducto.setEstado("Activo");
        producto.setEstados(estadoActivoProducto);

        // Act
        List<ProductosDTO> resultado = comprasService.obtenerProductosProveedor("proveedor-1");

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        ProductosDTO productoDTO = resultado.get(0);
        assertEquals("producto-1", productoDTO.getId());
        assertEquals("Producto Test", productoDTO.getNombre());
        assertEquals(new BigDecimal("10.00"), productoDTO.getPrecioCompraActual());
        assertEquals(100, productoDTO.getCantidadInventario());
    }

    @Test
    @DisplayName("Debe crear orden de compra exitosamente")
    void testCrearOrdenCompra_Exitoso() {
        // Arrange
        CompraRequestDTO request = new CompraRequestDTO();
        request.setProveedorId("proveedor-1");
        request.setMetodoPagoId("metodo-efectivo");

        CompraRequestDTO.ProductoCompraDTO productoCompra = new CompraRequestDTO.ProductoCompraDTO();
        productoCompra.setProductoId("producto-1");
        productoCompra.setCantidadPz(new BigDecimal("10"));
        productoCompra.setCantidadKg(BigDecimal.ZERO);
        request.setProductos(Arrays.asList(productoCompra));

        when(personasRepository.findById("proveedor-1")).thenReturn(Optional.of(proveedor));
        when(estadosRepository.findFirstByEstado("Activo")).thenReturn(Optional.of(estadoActivo));
        when(metodosPagoRepository.findById("metodo-efectivo")).thenReturn(Optional.of(metodoPagoEfectivo));
        when(productosRepository.findById("producto-1")).thenReturn(Optional.of(producto));
        when(historialCostosRepository.findLatestByProducto(producto)).thenReturn(Optional.of(historialCosto));
        when(ordenesDeComprasRepository.save(any(OrdenesDeCompras.class))).thenAnswer(invocation -> {
            OrdenesDeCompras orden = invocation.getArgument(0);
            orden.setId("orden-nueva");
            return orden;
        });
        when(detallesOrdenesDeComprasRepository.save(any(DetallesOrdenesDeCompras.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(inventarioRepository.findByProducto(producto)).thenReturn(Optional.of(inventario));
        when(inventarioRepository.save(any(Inventarios.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Mock SecurityContext para usuario autenticado
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("admin");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(usuariosRepository.findByNombre("admin")).thenReturn(Optional.of(usuario));
        when(tipoMovimientosRepository.findByMovimiento("Compra")).thenReturn(Optional.of(tipoMovimientoCompra));
        when(ubicacionesRepository.findAll()).thenReturn(Arrays.asList(ubicacion));
        when(movimientosInventariosRepository.save(any(MovimientosInventarios.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OrdenesDeComprasDTO resultado = comprasService.crearOrdenCompra(request);

        // Assert
        assertNotNull(resultado);
        assertEquals("orden-nueva", resultado.getId());
        assertEquals("proveedor-1", resultado.getPersonaId());
        assertEquals(new BigDecimal("100.00"), resultado.getTotalCompra()); // 10 pz * 10.00

        verify(ordenesDeComprasRepository).save(any(OrdenesDeCompras.class));
        verify(detallesOrdenesDeComprasRepository).save(any(DetallesOrdenesDeCompras.class));
        verify(inventarioRepository).save(argThat(inv -> inv.getCantidadPz() == 110)); // 100 + 10
        verify(movimientosInventariosRepository).save(any(MovimientosInventarios.class));
    }

    @Test
    @DisplayName("Debe fallar al crear orden con proveedor inexistente")
    void testCrearOrdenCompra_ProveedorNoExiste() {
        // Arrange
        CompraRequestDTO request = new CompraRequestDTO();
        request.setProveedorId("proveedor-inexistente");

        when(personasRepository.findById("proveedor-inexistente")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> comprasService.crearOrdenCompra(request)
        );

        assertEquals("Proveedor no encontrado: proveedor-inexistente", exception.getMessage());
        verify(ordenesDeComprasRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe fallar al crear orden si no existe estado Activo")
    void testCrearOrdenCompra_EstadoActivoNoExiste() {
        // Arrange
        CompraRequestDTO request = new CompraRequestDTO();
        request.setProveedorId("proveedor-1");

        when(personasRepository.findById("proveedor-1")).thenReturn(Optional.of(proveedor));
        when(estadosRepository.findFirstByEstado("Activo")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> comprasService.crearOrdenCompra(request)
        );

        assertEquals("Estado 'Activo' no encontrado", exception.getMessage());
        verify(ordenesDeComprasRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe fallar al crear orden con producto sin historial de costos")
    void testCrearOrdenCompra_ProductoSinHistorialCostos() {
        // Arrange
        CompraRequestDTO request = new CompraRequestDTO();
        request.setProveedorId("proveedor-1");
        request.setMetodoPagoId("metodo-efectivo");

        CompraRequestDTO.ProductoCompraDTO productoCompra = new CompraRequestDTO.ProductoCompraDTO();
        productoCompra.setProductoId("producto-1");
        productoCompra.setCantidadPz(new BigDecimal("10"));
        request.setProductos(Arrays.asList(productoCompra));

        when(personasRepository.findById("proveedor-1")).thenReturn(Optional.of(proveedor));
        when(estadosRepository.findFirstByEstado("Activo")).thenReturn(Optional.of(estadoActivo));
        when(metodosPagoRepository.findById("metodo-efectivo")).thenReturn(Optional.of(metodoPagoEfectivo));
        when(productosRepository.findById("producto-1")).thenReturn(Optional.of(producto));
        when(historialCostosRepository.findLatestByProducto(producto)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> comprasService.crearOrdenCompra(request)
        );

        assertTrue(exception.getMessage().contains("No hay historial de costos"));
        verify(ordenesDeComprasRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe realizar pago completo a proveedor")
    void testRealizarPago_PagoTotalExitoso() {
        // Arrange
        PagoRequestDTO request = new PagoRequestDTO();
        request.setProveedorId("proveedor-1");
        request.setOrdenCompraId("orden-1");
        request.setMetodoPagoId("metodo-efectivo");
        request.setPagarTodoElTotal(true);

        when(personasRepository.findById("proveedor-1")).thenReturn(Optional.of(proveedor));
        when(ordenesDeComprasRepository.findById("orden-1")).thenReturn(Optional.of(ordenCompra));
        when(metodosPagoRepository.findById("metodo-efectivo")).thenReturn(Optional.of(metodoPagoEfectivo));
        when(historialCargosProveedoresRepository.calcularTotalPagosOrdenCompra("orden-1"))
                .thenReturn(new BigDecimal("300.00")); // Ya se pagaron 300
        when(historialCargosProveedoresRepository.save(any(HistorialCargosProveedores.class)))
                .thenAnswer(invocation -> {
                    HistorialCargosProveedores pago = invocation.getArgument(0);
                    pago.setId("pago-nuevo");
                    return pago;
                });

        // Act
        HistorialCargosProveedoresDTO resultado = comprasService.realizarPago(request);

        // Assert
        assertNotNull(resultado);
        assertEquals("pago-nuevo", resultado.getId());
        assertEquals("proveedor-1", resultado.getPersonaId());
        assertEquals("orden-1", resultado.getOrdenDeCompraId());
        assertEquals(new BigDecimal("200.00"), resultado.getMontoPagado()); // 500 total - 300 pagado = 200 restante

        verify(historialCargosProveedoresRepository).save(argThat(pago ->
                pago.getMontoPagado().equals(new BigDecimal("200.00"))
        ));
    }

    @Test
    @DisplayName("Debe realizar pago parcial a proveedor")
    void testRealizarPago_PagoParcialExitoso() {
        // Arrange
        PagoRequestDTO request = new PagoRequestDTO();
        request.setProveedorId("proveedor-1");
        request.setOrdenCompraId("orden-1");
        request.setMetodoPagoId("metodo-efectivo");
        request.setPagarTodoElTotal(false);
        request.setMontoPagado(new BigDecimal("150.00"));

        when(personasRepository.findById("proveedor-1")).thenReturn(Optional.of(proveedor));
        when(ordenesDeComprasRepository.findById("orden-1")).thenReturn(Optional.of(ordenCompra));
        when(metodosPagoRepository.findById("metodo-efectivo")).thenReturn(Optional.of(metodoPagoEfectivo));
        when(historialCargosProveedoresRepository.calcularTotalPagosOrdenCompra("orden-1"))
                .thenReturn(new BigDecimal("100.00"));
        when(historialCargosProveedoresRepository.save(any(HistorialCargosProveedores.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        HistorialCargosProveedoresDTO resultado = comprasService.realizarPago(request);

        // Assert
        assertNotNull(resultado);
        assertEquals(new BigDecimal("150.00"), resultado.getMontoPagado());

        verify(historialCargosProveedoresRepository).save(argThat(pago ->
                pago.getMontoPagado().equals(new BigDecimal("150.00"))
        ));
    }

    @Test
    @DisplayName("Debe fallar al pagar orden ya completamente pagada")
    void testRealizarPago_OrdenYaPagada() {
        // Arrange
        PagoRequestDTO request = new PagoRequestDTO();
        request.setProveedorId("proveedor-1");
        request.setOrdenCompraId("orden-1");
        request.setMetodoPagoId("metodo-efectivo");
        request.setPagarTodoElTotal(true);

        when(personasRepository.findById("proveedor-1")).thenReturn(Optional.of(proveedor));
        when(ordenesDeComprasRepository.findById("orden-1")).thenReturn(Optional.of(ordenCompra));
        when(metodosPagoRepository.findById("metodo-efectivo")).thenReturn(Optional.of(metodoPagoEfectivo));
        when(historialCargosProveedoresRepository.calcularTotalPagosOrdenCompra("orden-1"))
                .thenReturn(new BigDecimal("500.00")); // Ya se pagó todo

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> comprasService.realizarPago(request)
        );

        assertEquals("Esta orden ya está completamente pagada", exception.getMessage());
        verify(historialCargosProveedoresRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe fallar al pagar monto mayor a deuda pendiente")
    void testRealizarPago_MontoMayorADeuda() {
        // Arrange
        PagoRequestDTO request = new PagoRequestDTO();
        request.setProveedorId("proveedor-1");
        request.setOrdenCompraId("orden-1");
        request.setMetodoPagoId("metodo-efectivo");
        request.setPagarTodoElTotal(false);
        request.setMontoPagado(new BigDecimal("600.00")); // Mayor a los 500 totales

        when(personasRepository.findById("proveedor-1")).thenReturn(Optional.of(proveedor));
        when(ordenesDeComprasRepository.findById("orden-1")).thenReturn(Optional.of(ordenCompra));
        when(metodosPagoRepository.findById("metodo-efectivo")).thenReturn(Optional.of(metodoPagoEfectivo));
        when(historialCargosProveedoresRepository.calcularTotalPagosOrdenCompra("orden-1"))
                .thenReturn(BigDecimal.ZERO);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> comprasService.realizarPago(request)
        );

        assertTrue(exception.getMessage().contains("no puede ser mayor a la deuda pendiente"));
        verify(historialCargosProveedoresRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe fallar al pagar monto negativo o cero")
    void testRealizarPago_MontoInvalido() {
        // Arrange
        PagoRequestDTO request = new PagoRequestDTO();
        request.setProveedorId("proveedor-1");
        request.setOrdenCompraId("orden-1");
        request.setMetodoPagoId("metodo-efectivo");
        request.setPagarTodoElTotal(false);
        request.setMontoPagado(BigDecimal.ZERO);

        when(personasRepository.findById("proveedor-1")).thenReturn(Optional.of(proveedor));
        when(ordenesDeComprasRepository.findById("orden-1")).thenReturn(Optional.of(ordenCompra));
        when(metodosPagoRepository.findById("metodo-efectivo")).thenReturn(Optional.of(metodoPagoEfectivo));
        when(historialCargosProveedoresRepository.calcularTotalPagosOrdenCompra("orden-1"))
                .thenReturn(BigDecimal.ZERO);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> comprasService.realizarPago(request)
        );

        assertEquals("El monto a pagar debe ser mayor a cero", exception.getMessage());
        verify(historialCargosProveedoresRepository, never()).save(any());
    }
}
