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

/**
 * Pruebas unitarias para ComprasService
 * Parte del plan de pruebas ISO/IEC 25010 - Lógica de negocio crítica
 * 
 * Cubre:
 * - Gestión de proveedores y cálculo de deudas
 * - Creación de órdenes de compra con validaciones
 * - Procesamiento de pagos con reglas de negocio
 * - Actualización de inventario y trazabilidad
 */
@ExtendWith(MockitoExtension.class)
class ComprasServiceTest {

    @Mock private PersonasRepository personasRepository;
    @Mock private OrdenesDeComprasRepository ordenesDeComprasRepository;
    @Mock private DetallesOrdenesDeComprasRepository detallesOrdenesDeComprasRepository;
    @Mock private HistorialCargosProveedoresRepository historialCargosProveedoresRepository;
    @Mock private ProductosRepository productosRepository;
    @Mock private EstadosRepository estadosRepository;
    @Mock private MetodosPagoRepository metodosPagoRepository;
    @Mock private HistorialCostosRepository historialCostosRepository;
    @Mock private InventarioRepository inventarioRepository;
    @Mock private MovimientosInventariosRepository movimientosInventariosRepository;
    @Mock private TIpoMovimientosRepository tipoMovimientosRepository;
    @Mock private UbicacionesRepository ubicacionesRepository;
    @Mock private UsuariosRepository usuariosRepository;

    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @InjectMocks
    private ComprasService comprasService;

    // Datos de prueba
    private Personas proveedorTest;
    private Productos productoTest;
    private Estados estadoActivo;
    private MetodosPago metodoPagoEfectivo;
    private HistorialCostos historialCostoTest;
    private Usuarios usuarioTest;
    private Ubicaciones ubicacionTest;
    private TipoMovimientos tipoCompra;

    @BeforeEach
    void setUp() {
        // Configurar datos de prueba reutilizables
        proveedorTest = new Personas();
        proveedorTest.setId("prov-001");
        proveedorTest.setNombre("Proveedor Test");
        proveedorTest.setTelefono("1234567890");
        proveedorTest.setEmail("proveedor@test.com");

        estadoActivo = new Estados();
        estadoActivo.setId("estado-001");
        estadoActivo.setEstado("Activo");

        proveedorTest.setEstados(estadoActivo);

        productoTest = new Productos();
        productoTest.setId("prod-001");
        productoTest.setNombre("Producto Test");
        productoTest.setEstados(estadoActivo);

        metodoPagoEfectivo = new MetodosPago();
        metodoPagoEfectivo.setId("pago-001");
        metodoPagoEfectivo.setMetodoPago("Efectivo");

        historialCostoTest = new HistorialCostos();
        historialCostoTest.setId("costo-001");
        historialCostoTest.setProducto(productoTest);
        historialCostoTest.setCosto(new BigDecimal("10.50"));

        usuarioTest = new Usuarios();
        usuarioTest.setId("user-001");
        usuarioTest.setNombre("usuariotest");

        ubicacionTest = new Ubicaciones();
        ubicacionTest.setId("ubic-001");
        ubicacionTest.setNombreUbicacion("Almacén Principal");

        tipoCompra = new TipoMovimientos();
        tipoCompra.setId("tipo-001");
        tipoCompra.setMovimiento("Compra");

        // Configurar SecurityContext mock
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("usuariotest");
    }

    // ========== PRUEBAS DE GESTIÓN DE PROVEEDORES ==========

    @Test
    @DisplayName("Debe obtener lista de proveedores activos con sus deudas calculadas")
    void testObtenerProveedores_Success() {
        // Given
        List<Personas> proveedoresActivos = Arrays.asList(proveedorTest);
        when(personasRepository.findProveedoresActivos()).thenReturn(proveedoresActivos);
        
        BigDecimal totalCompras = new BigDecimal("1000.00");
        BigDecimal totalPagos = new BigDecimal("300.00");
        when(ordenesDeComprasRepository.calcularTotalComprasProveedor("prov-001")).thenReturn(totalCompras);
        when(historialCargosProveedoresRepository.calcularTotalPagosProveedor("prov-001")).thenReturn(totalPagos);

        // When
        List<ProveedorDTO> result = comprasService.obtenerProveedores();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        
        ProveedorDTO proveedor = result.get(0);
        assertEquals("prov-001", proveedor.getId());
        assertEquals("Proveedor Test", proveedor.getNombre());
        assertEquals("Activo", proveedor.getEstado());
        assertEquals(new BigDecimal("700.00"), proveedor.getDeudaPendiente());
        
        verify(personasRepository).findProveedoresActivos();
        verify(ordenesDeComprasRepository).calcularTotalComprasProveedor("prov-001");
        verify(historialCargosProveedoresRepository).calcularTotalPagosProveedor("prov-001");
    }

    @Test
    @DisplayName("Debe calcular correctamente la deuda de un proveedor específico")
    void testCalcularDeudaProveedor_Success() {
        // Given
        when(personasRepository.findById("prov-001")).thenReturn(Optional.of(proveedorTest));
        when(ordenesDeComprasRepository.calcularTotalComprasProveedor("prov-001"))
            .thenReturn(new BigDecimal("2500.00"));
        when(historialCargosProveedoresRepository.calcularTotalPagosProveedor("prov-001"))
            .thenReturn(new BigDecimal("1200.00"));

        // When
        DeudaProveedorDTO result = comprasService.calcularDeudaProveedor("prov-001");

        // Then
        assertNotNull(result);
        assertEquals("prov-001", result.getProveedorId());
        assertEquals("Proveedor Test", result.getNombreProveedor());
        assertEquals(new BigDecimal("2500.00"), result.getTotalCompras());
        assertEquals(new BigDecimal("1200.00"), result.getTotalPagos());
        assertEquals(new BigDecimal("1300.00"), result.getDeudaPendiente());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el proveedor no existe para cálculo de deuda")
    void testCalcularDeudaProveedor_ProveedorNoEncontrado() {
        // Given
        when(personasRepository.findById("prov-inexistente")).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> comprasService.calcularDeudaProveedor("prov-inexistente"));
        
        assertTrue(exception.getMessage().contains("Proveedor no encontrado"));
        verify(personasRepository).findById("prov-inexistente");
    }

    // ========== PRUEBAS DE GESTIÓN DE PRODUCTOS POR PROVEEDOR ==========

    @Test
    @DisplayName("Debe obtener productos de un proveedor con precios y stock actual")
    void testObtenerProductosProveedor_Success() {
        // Given
        List<Productos> productos = Arrays.asList(productoTest);
        when(productosRepository.findByProveedorIdAndEstadoActivo("prov-001")).thenReturn(productos);
        when(historialCostosRepository.findLatestByProducto(productoTest))
            .thenReturn(Optional.of(historialCostoTest));
        
        Inventarios inventarioTest = new Inventarios();
        inventarioTest.setCantidadPz(10);
        inventarioTest.setCantidadKg(5);
        when(inventarioRepository.findByProducto(productoTest)).thenReturn(Optional.of(inventarioTest));

        // When
        List<ProductosDTO> result = comprasService.obtenerProductosProveedor("prov-001");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        
        ProductosDTO producto = result.get(0);
        assertEquals("prod-001", producto.getId());
        assertEquals("Producto Test", producto.getNombre());
        assertEquals(new BigDecimal("10.50"), producto.getPrecioCompraActual());
        assertEquals(15, producto.getCantidadInventario()); // 10 pz + 5 kg
        
        verify(productosRepository).findByProveedorIdAndEstadoActivo("prov-001");
        verify(historialCostosRepository).findLatestByProducto(productoTest);
        verify(inventarioRepository).findByProducto(productoTest);
    }

    // ========== PRUEBAS DE CREACIÓN DE ÓRDENES DE COMPRA ==========

    @Test
    @DisplayName("Debe crear orden de compra exitosamente con validaciones y actualización de inventario")
    void testCrearOrdenCompra_Success() {
        // Given
        CompraRequestDTO compraRequest = crearCompraRequestValida();
        
        when(personasRepository.findById("prov-001")).thenReturn(Optional.of(proveedorTest));
        when(estadosRepository.findFirstByEstado("Activo")).thenReturn(Optional.of(estadoActivo));
        when(metodosPagoRepository.findByMetodoPago("Efectivo")).thenReturn(Optional.of(metodoPagoEfectivo));
        when(productosRepository.findById("prod-001")).thenReturn(Optional.of(productoTest));
        when(historialCostosRepository.findLatestByProducto(productoTest))
            .thenReturn(Optional.of(historialCostoTest));
        
        OrdenesDeCompras ordenGuardada = crearOrdenCompraTest("orden-001", new BigDecimal("52.50"));
        when(ordenesDeComprasRepository.save(any(OrdenesDeCompras.class))).thenReturn(ordenGuardada);
        when(detallesOrdenesDeComprasRepository.save(any(DetallesOrdenesDeCompras.class)))
            .thenReturn(new DetallesOrdenesDeCompras());
        
        Inventarios inventarioExistente = new Inventarios();
        inventarioExistente.setCantidadPz(10);
        inventarioExistente.setCantidadKg(0);
        when(inventarioRepository.findByProducto(productoTest)).thenReturn(Optional.of(inventarioExistente));
        when(inventarioRepository.save(any(Inventarios.class))).thenReturn(inventarioExistente);
        
        when(usuariosRepository.findByNombre("usuariotest")).thenReturn(Optional.of(usuarioTest));
        when(tipoMovimientosRepository.findByMovimiento("Compra")).thenReturn(Optional.of(tipoCompra));
        when(ubicacionesRepository.findAll()).thenReturn(Arrays.asList(ubicacionTest));
        when(movimientosInventariosRepository.save(any(MovimientosInventarios.class)))
            .thenReturn(new MovimientosInventarios());

        // When
        OrdenesDeComprasDTO result = comprasService.crearOrdenCompra(compraRequest);

        // Then
        assertNotNull(result);
        assertEquals("orden-001", result.getId());
        assertEquals("prov-001", result.getPersonaId());
        assertEquals("Proveedor Test", result.getPersonaNombre());
        assertEquals(new BigDecimal("52.50"), result.getTotalCompra());
        
        // Verificar que se actualizó el inventario
        verify(inventarioRepository).save(argThat(inventario -> 
            inventario.getCantidadPz() == 15)); // 10 + 5 compradas
        
        // Verificar que se creó movimiento de inventario
        verify(movimientosInventariosRepository).save(argThat(movimiento ->
            movimiento.getClaveMovimiento().startsWith("COMPRA-orden-001")));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el proveedor no existe en creación de orden")
    void testCrearOrdenCompra_ProveedorNoEncontrado() {
        // Given
        CompraRequestDTO compraRequest = crearCompraRequestValida();
        when(personasRepository.findById("prov-001")).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> comprasService.crearOrdenCompra(compraRequest));
        
        assertTrue(exception.getMessage().contains("Proveedor no encontrado"));
        verify(personasRepository).findById("prov-001");
        verifyNoInteractions(ordenesDeComprasRepository);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el producto no existe en orden de compra")
    void testCrearOrdenCompra_ProductoNoEncontrado() {
        // Given
        CompraRequestDTO compraRequest = crearCompraRequestValida();
        
        when(personasRepository.findById("prov-001")).thenReturn(Optional.of(proveedorTest));
        when(estadosRepository.findFirstByEstado("Activo")).thenReturn(Optional.of(estadoActivo));
        when(metodosPagoRepository.findByMetodoPago("Efectivo")).thenReturn(Optional.of(metodoPagoEfectivo));
        when(productosRepository.findById("prod-001")).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> comprasService.crearOrdenCompra(compraRequest));
        
        assertTrue(exception.getMessage().contains("Producto no encontrado"));
        verify(productosRepository).findById("prod-001");
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando no hay historial de costos para el producto")
    void testCrearOrdenCompra_SinHistorialCostos() {
        // Given
        CompraRequestDTO compraRequest = crearCompraRequestValida();
        
        when(personasRepository.findById("prov-001")).thenReturn(Optional.of(proveedorTest));
        when(estadosRepository.findFirstByEstado("Activo")).thenReturn(Optional.of(estadoActivo));
        when(metodosPagoRepository.findByMetodoPago("Efectivo")).thenReturn(Optional.of(metodoPagoEfectivo));
        when(productosRepository.findById("prod-001")).thenReturn(Optional.of(productoTest));
        when(historialCostosRepository.findLatestByProducto(productoTest)).thenReturn(Optional.empty());

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            () -> comprasService.crearOrdenCompra(compraRequest));
        
        assertTrue(exception.getMessage().contains("No hay historial de costos"));
        assertTrue(exception.getMessage().contains("Producto Test"));
    }

    // ========== PRUEBAS DE PROCESAMIENTO DE PAGOS ==========

    @Test
    @DisplayName("Debe procesar pago parcial correctamente con validaciones de deuda")
    void testRealizarPago_PagoParcial_Success() {
        // Given
        PagoRequestDTO pagoRequest = new PagoRequestDTO("prov-001", "orden-001", 
            new BigDecimal("200.00"), "pago-001", false);
        
        OrdenesDeCompras orden = crearOrdenCompraTest("orden-001", new BigDecimal("1000.00"));
        
        when(personasRepository.findById("prov-001")).thenReturn(Optional.of(proveedorTest));
        when(ordenesDeComprasRepository.findById("orden-001")).thenReturn(Optional.of(orden));
        when(metodosPagoRepository.findById("pago-001")).thenReturn(Optional.of(metodoPagoEfectivo));
        when(historialCargosProveedoresRepository.calcularTotalPagosOrdenCompra("orden-001"))
            .thenReturn(new BigDecimal("300.00")); // Ya se han pagado $300
        
        HistorialCargosProveedores pagoGuardado = new HistorialCargosProveedores();
        pagoGuardado.setId("pago-hist-001");
        pagoGuardado.setPersona(proveedorTest);
        pagoGuardado.setOrdenDeCompra(orden);
        pagoGuardado.setMontoPagado(new BigDecimal("200.00"));
        pagoGuardado.setFecha(OffsetDateTime.now());
        when(historialCargosProveedoresRepository.save(any(HistorialCargosProveedores.class)))
            .thenReturn(pagoGuardado);

        // When
        HistorialCargosProveedoresDTO result = comprasService.realizarPago(pagoRequest);

        // Then
        assertNotNull(result);
        assertEquals("pago-hist-001", result.getId());
        assertEquals("prov-001", result.getPersonaId());
        assertEquals("orden-001", result.getOrdenDeCompraId());
        assertEquals(new BigDecimal("200.00"), result.getMontoPagado());
        
        verify(historialCargosProveedoresRepository).calcularTotalPagosOrdenCompra("orden-001");
        verify(historialCargosProveedoresRepository).save(any(HistorialCargosProveedores.class));
    }

    @Test
    @DisplayName("Debe procesar pago total automáticamente calculando deuda pendiente")
    void testRealizarPago_PagoTotal_Success() {
        // Given
        PagoRequestDTO pagoRequest = new PagoRequestDTO("prov-001", "orden-001", 
            null, "pago-001", true); // Pagar todo el total
        
        OrdenesDeCompras orden = crearOrdenCompraTest("orden-001", new BigDecimal("1500.00"));
        
        when(personasRepository.findById("prov-001")).thenReturn(Optional.of(proveedorTest));
        when(ordenesDeComprasRepository.findById("orden-001")).thenReturn(Optional.of(orden));
        when(metodosPagoRepository.findById("pago-001")).thenReturn(Optional.of(metodoPagoEfectivo));
        when(historialCargosProveedoresRepository.calcularTotalPagosOrdenCompra("orden-001"))
            .thenReturn(new BigDecimal("400.00")); // Ya pagados $400
        
        HistorialCargosProveedores pagoGuardado = new HistorialCargosProveedores();
        pagoGuardado.setMontoPagado(new BigDecimal("1100.00")); // $1500 - $400
        when(historialCargosProveedoresRepository.save(any(HistorialCargosProveedores.class)))
            .thenReturn(pagoGuardado);

        // When
        HistorialCargosProveedoresDTO result = comprasService.realizarPago(pagoRequest);

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("1100.00"), result.getMontoPagado());
        
        verify(historialCargosProveedoresRepository).save(argThat(pago ->
            pago.getMontoPagado().equals(new BigDecimal("1100.00"))));
    }

    @Test
    @DisplayName("Debe rechazar pago cuando el monto excede la deuda pendiente")
    void testRealizarPago_MontoExcedeDeuda() {
        // Given
        PagoRequestDTO pagoRequest = new PagoRequestDTO("prov-001", "orden-001", 
            new BigDecimal("1000.00"), "pago-001", false);
        
        OrdenesDeCompras orden = crearOrdenCompraTest("orden-001", new BigDecimal("500.00"));
        
        when(personasRepository.findById("prov-001")).thenReturn(Optional.of(proveedorTest));
        when(ordenesDeComprasRepository.findById("orden-001")).thenReturn(Optional.of(orden));
        when(metodosPagoRepository.findById("pago-001")).thenReturn(Optional.of(metodoPagoEfectivo));
        when(historialCargosProveedoresRepository.calcularTotalPagosOrdenCompra("orden-001"))
            .thenReturn(new BigDecimal("100.00"));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> comprasService.realizarPago(pagoRequest));
        
        assertTrue(exception.getMessage().contains("no puede ser mayor a la deuda pendiente"));
        assertTrue(exception.getMessage().contains("$400.00")); // Deuda pendiente
        
        verify(historialCargosProveedoresRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe rechazar pago cuando la orden ya está completamente pagada")
    void testRealizarPago_OrdenYaPagada() {
        // Given
        PagoRequestDTO pagoRequest = new PagoRequestDTO("prov-001", "orden-001", 
            null, "pago-001", true);
        
        OrdenesDeCompras orden = crearOrdenCompraTest("orden-001", new BigDecimal("1000.00"));
        
        when(personasRepository.findById("prov-001")).thenReturn(Optional.of(proveedorTest));
        when(ordenesDeComprasRepository.findById("orden-001")).thenReturn(Optional.of(orden));
        when(metodosPagoRepository.findById("pago-001")).thenReturn(Optional.of(metodoPagoEfectivo));
        when(historialCargosProveedoresRepository.calcularTotalPagosOrdenCompra("orden-001"))
            .thenReturn(new BigDecimal("1000.00")); // Completamente pagada

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> comprasService.realizarPago(pagoRequest));
        
        assertTrue(exception.getMessage().contains("ya está completamente pagada"));
        verify(historialCargosProveedoresRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe rechazar pago con monto cero o negativo")
    void testRealizarPago_MontoInvalido() {
        // Given
        PagoRequestDTO pagoRequest = new PagoRequestDTO("prov-001", "orden-001", 
            BigDecimal.ZERO, "pago-001", false);
        
        OrdenesDeCompras orden = crearOrdenCompraTest("orden-001", new BigDecimal("1000.00"));
        
        when(personasRepository.findById("prov-001")).thenReturn(Optional.of(proveedorTest));
        when(ordenesDeComprasRepository.findById("orden-001")).thenReturn(Optional.of(orden));
        when(metodosPagoRepository.findById("pago-001")).thenReturn(Optional.of(metodoPagoEfectivo));
        when(historialCargosProveedoresRepository.calcularTotalPagosOrdenCompra("orden-001"))
            .thenReturn(BigDecimal.ZERO);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> comprasService.realizarPago(pagoRequest));
        
        assertTrue(exception.getMessage().contains("debe ser mayor a cero"));
        verify(historialCargosProveedoresRepository, never()).save(any());
    }

    // ========== MÉTODOS AUXILIARES PARA PRUEBAS ==========

    private CompraRequestDTO crearCompraRequestValida() {
        CompraRequestDTO compraRequest = new CompraRequestDTO();
        compraRequest.setProveedorId("prov-001");
        // metodoPagoId null para usar "Efectivo" por defecto
        
        CompraRequestDTO.ProductoCompraDTO productoCompra = new CompraRequestDTO.ProductoCompraDTO();
        productoCompra.setProductoId("prod-001");
        productoCompra.setCantidadPz(new BigDecimal("5"));
        productoCompra.setCantidadKg(BigDecimal.ZERO);
        
        compraRequest.setProductos(Arrays.asList(productoCompra));
        return compraRequest;
    }

    private OrdenesDeCompras crearOrdenCompraTest(String id, BigDecimal total) {
        OrdenesDeCompras orden = new OrdenesDeCompras();
        orden.setId(id);
        orden.setPersona(proveedorTest);
        orden.setTotalCompra(total);
        orden.setFechaOrden(OffsetDateTime.now());
        orden.setEstado(estadoActivo);
        return orden;
    }
}