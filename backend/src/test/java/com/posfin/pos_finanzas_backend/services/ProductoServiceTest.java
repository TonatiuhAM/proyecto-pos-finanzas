package com.posfin.pos_finanzas_backend.services;

import com.posfin.pos_finanzas_backend.dtos.ProductoCreacionDTO;
import com.posfin.pos_finanzas_backend.dtos.ProductosDTO;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de ProductoService")
class ProductoServiceTest {

    @Mock
    private ProductosRepository productosRepository;

    @Mock
    private CategoriasProductosRepository categoriasProductosRepository;

    @Mock
    private PersonasRepository personasRepository;

    @Mock
    private EstadosRepository estadosRepository;

    @Mock
    private HistorialPreciosRepository historialPreciosRepository;

    @Mock
    private HistorialCostosRepository historialCostosRepository;

    @Mock
    private InventarioRepository inventarioRepository;

    @Mock
    private MovimientosInventariosRepository movimientosInventariosRepository;

    @Mock
    private UbicacionesRepository ubicacionesRepository;

    @Mock
    private TIpoMovimientosRepository tipoMovimientosRepository;

    @Mock
    private UsuariosRepository usuariosRepository;

    @InjectMocks
    private ProductoService productoService;

    private ProductoCreacionDTO productoCreacionDTO;
    private Productos producto;
    private CategoriasProductos categoria;
    private Personas proveedor;
    private Estados estadoActivo;
    private Estados estadoInactivo;
    private Ubicaciones ubicacion;
    private Usuarios usuario;
    private TipoMovimientos tipoCreacion;
    private HistorialPrecios historialPrecio;
    private HistorialCostos historialCosto;
    private Inventarios inventario;

    @BeforeEach
    void setUp() {
        // Configurar categoría
        categoria = new CategoriasProductos();
        categoria.setId("cat-1");
        categoria.setCategoria("Alimentos");

        // Configurar proveedor
        proveedor = new Personas();
        proveedor.setId("prov-1");
        proveedor.setNombre("Proveedor");
        proveedor.setApellidoPaterno("Test");

        // Configurar estados
        estadoActivo = new Estados();
        estadoActivo.setId("estado-1");
        estadoActivo.setEstado("Activo");

        estadoInactivo = new Estados();
        estadoInactivo.setId("estado-2");
        estadoInactivo.setEstado("Inactivo");

        // Configurar ubicación
        ubicacion = new Ubicaciones();
        ubicacion.setId("ubic-1");
        ubicacion.setNombre("Almacén Principal");

        // Configurar usuario
        usuario = new Usuarios();
        usuario.setId("user-1");
        usuario.setNombre("admin");

        // Configurar tipo de movimiento
        tipoCreacion = new TipoMovimientos();
        tipoCreacion.setId("tipo-1");
        tipoCreacion.setMovimiento("Creación");

        // Configurar producto
        producto = new Productos();
        producto.setId("prod-123456");
        producto.setNombre("Producto Test");
        producto.setCategoriasProductos(categoria);
        producto.setProveedor(proveedor);
        producto.setEstados(estadoActivo);

        // Configurar DTO de creación
        productoCreacionDTO = new ProductoCreacionDTO();
        productoCreacionDTO.setNombre("Producto Test");
        productoCreacionDTO.setCategoriasProductosId("cat-1");
        productoCreacionDTO.setProveedorId("prov-1");
        productoCreacionDTO.setUbicacionId("ubic-1");
        productoCreacionDTO.setUsuarioId("user-1");
        productoCreacionDTO.setPrecioVenta(new BigDecimal("100.00"));
        productoCreacionDTO.setPrecioCompra(new BigDecimal("50.00"));
        productoCreacionDTO.setStockInicial(100);
        productoCreacionDTO.setStockMinimo(10);
        productoCreacionDTO.setStockMaximo(500);
        productoCreacionDTO.setUnidadMedida("piezas");

        // Configurar historial de precio
        historialPrecio = new HistorialPrecios();
        historialPrecio.setProductos(producto);
        historialPrecio.setPrecio(new BigDecimal("100.00"));

        // Configurar historial de costo
        historialCosto = new HistorialCostos();
        historialCosto.setProductos(producto);
        historialCosto.setCosto(new BigDecimal("50.00"));

        // Configurar inventario
        inventario = new Inventarios();
        inventario.setProducto(producto);
        inventario.setUbicacion(ubicacion);
        inventario.setCantidadPz(100);
        inventario.setCantidadKg(0);
        inventario.setCantidadMinima(10);
        inventario.setCantidadMaxima(500);
    }

    @Test
    @DisplayName("Debe crear un producto completo exitosamente con unidad en piezas")
    void testCrearProductoCompleto_Exitoso_Piezas() {
        // Arrange
        when(categoriasProductosRepository.findById("cat-1")).thenReturn(Optional.of(categoria));
        when(personasRepository.findById("prov-1")).thenReturn(Optional.of(proveedor));
        when(ubicacionesRepository.findById("ubic-1")).thenReturn(Optional.of(ubicacion));
        when(usuariosRepository.findById("user-1")).thenReturn(Optional.of(usuario));
        when(estadosRepository.findAll()).thenReturn(Arrays.asList(estadoActivo, estadoInactivo));
        when(tipoMovimientosRepository.findAll()).thenReturn(Arrays.asList(tipoCreacion));
        when(productosRepository.save(any(Productos.class))).thenReturn(producto);
        when(historialPreciosRepository.findLatestByProducto(producto)).thenReturn(Optional.of(historialPrecio));
        when(historialCostosRepository.findLatestByProducto(producto)).thenReturn(Optional.of(historialCosto));
        when(inventarioRepository.findByProducto(producto)).thenReturn(Optional.of(inventario));

        // Act
        ProductosDTO resultado = productoService.crearProductoCompleto(productoCreacionDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals("Producto Test", resultado.getNombre());
        assertEquals("cat-1", resultado.getCategoriasProductosId());
        assertEquals("Alimentos", resultado.getCategoriasProductosCategoria());
        assertEquals(new BigDecimal("100.00"), resultado.getPrecioVentaActual());
        assertEquals(new BigDecimal("50.00"), resultado.getPrecioCompraActual());
        assertEquals(100, resultado.getCantidadInventario());

        // Verificar que se guardaron todas las entidades
        verify(productosRepository, times(1)).save(any(Productos.class));
        verify(historialPreciosRepository, times(1)).save(any(HistorialPrecios.class));
        verify(historialCostosRepository, times(1)).save(any(HistorialCostos.class));
        verify(inventarioRepository, times(1)).save(any(Inventarios.class));
        verify(movimientosInventariosRepository, times(1)).save(any(MovimientosInventarios.class));
    }

    @Test
    @DisplayName("Debe crear un producto completo exitosamente con unidad en kilogramos")
    void testCrearProductoCompleto_Exitoso_Kilogramos() {
        // Arrange
        productoCreacionDTO.setUnidadMedida("kilogramos");
        inventario.setCantidadPz(0);
        inventario.setCantidadKg(100);

        when(categoriasProductosRepository.findById("cat-1")).thenReturn(Optional.of(categoria));
        when(personasRepository.findById("prov-1")).thenReturn(Optional.of(proveedor));
        when(ubicacionesRepository.findById("ubic-1")).thenReturn(Optional.of(ubicacion));
        when(usuariosRepository.findById("user-1")).thenReturn(Optional.of(usuario));
        when(estadosRepository.findAll()).thenReturn(Arrays.asList(estadoActivo));
        when(tipoMovimientosRepository.findAll()).thenReturn(Arrays.asList(tipoCreacion));
        when(productosRepository.save(any(Productos.class))).thenReturn(producto);
        when(historialPreciosRepository.findLatestByProducto(producto)).thenReturn(Optional.of(historialPrecio));
        when(historialCostosRepository.findLatestByProducto(producto)).thenReturn(Optional.of(historialCosto));
        when(inventarioRepository.findByProducto(producto)).thenReturn(Optional.of(inventario));

        // Act
        ProductosDTO resultado = productoService.crearProductoCompleto(productoCreacionDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals(100, resultado.getCantidadInventario());
        verify(inventarioRepository, times(1)).save(argThat(inv -> 
            inv.getCantidadKg() == 100 && inv.getCantidadPz() == 0
        ));
    }

    @Test
    @DisplayName("Debe lanzar excepción si la categoría no existe")
    void testCrearProductoCompleto_CategoriaNoExiste() {
        // Arrange
        when(categoriasProductosRepository.findById("cat-1")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productoService.crearProductoCompleto(productoCreacionDTO);
        });

        assertEquals("Categoría no encontrada con ID: cat-1", exception.getMessage());
        verify(productosRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción si el proveedor no existe")
    void testCrearProductoCompleto_ProveedorNoExiste() {
        // Arrange
        when(categoriasProductosRepository.findById("cat-1")).thenReturn(Optional.of(categoria));
        when(personasRepository.findById("prov-1")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productoService.crearProductoCompleto(productoCreacionDTO);
        });

        assertEquals("Proveedor no encontrado con ID: prov-1", exception.getMessage());
        verify(productosRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción si la ubicación no existe")
    void testCrearProductoCompleto_UbicacionNoExiste() {
        // Arrange
        when(categoriasProductosRepository.findById("cat-1")).thenReturn(Optional.of(categoria));
        when(personasRepository.findById("prov-1")).thenReturn(Optional.of(proveedor));
        when(ubicacionesRepository.findById("ubic-1")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productoService.crearProductoCompleto(productoCreacionDTO);
        });

        assertEquals("Ubicación no encontrada con ID: ubic-1", exception.getMessage());
        verify(productosRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción si el usuario no existe")
    void testCrearProductoCompleto_UsuarioNoExiste() {
        // Arrange
        when(categoriasProductosRepository.findById("cat-1")).thenReturn(Optional.of(categoria));
        when(personasRepository.findById("prov-1")).thenReturn(Optional.of(proveedor));
        when(ubicacionesRepository.findById("ubic-1")).thenReturn(Optional.of(ubicacion));
        when(usuariosRepository.findById("user-1")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productoService.crearProductoCompleto(productoCreacionDTO);
        });

        assertEquals("Usuario no encontrado con ID: user-1", exception.getMessage());
        verify(productosRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción si no existe el estado 'Activo'")
    void testCrearProductoCompleto_EstadoActivoNoExiste() {
        // Arrange
        when(categoriasProductosRepository.findById("cat-1")).thenReturn(Optional.of(categoria));
        when(personasRepository.findById("prov-1")).thenReturn(Optional.of(proveedor));
        when(ubicacionesRepository.findById("ubic-1")).thenReturn(Optional.of(ubicacion));
        when(usuariosRepository.findById("user-1")).thenReturn(Optional.of(usuario));
        when(estadosRepository.findAll()).thenReturn(Arrays.asList(estadoInactivo)); // Sin estado Activo

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productoService.crearProductoCompleto(productoCreacionDTO);
        });

        assertEquals("Estado 'Activo' no encontrado en la base de datos", exception.getMessage());
        verify(productosRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción si no existe el tipo de movimiento 'Creación'")
    void testCrearProductoCompleto_TipoMovimientoNoExiste() {
        // Arrange
        when(categoriasProductosRepository.findById("cat-1")).thenReturn(Optional.of(categoria));
        when(personasRepository.findById("prov-1")).thenReturn(Optional.of(proveedor));
        when(ubicacionesRepository.findById("ubic-1")).thenReturn(Optional.of(ubicacion));
        when(usuariosRepository.findById("user-1")).thenReturn(Optional.of(usuario));
        when(estadosRepository.findAll()).thenReturn(Arrays.asList(estadoActivo));
        when(tipoMovimientosRepository.findAll()).thenReturn(Arrays.asList()); // Sin tipos de movimiento

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productoService.crearProductoCompleto(productoCreacionDTO);
        });

        assertEquals("Tipo de movimiento 'Creación' no encontrado en la base de datos", exception.getMessage());
        verify(productosRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe desactivar un producto exitosamente")
    void testDesactivarProducto_Exitoso() {
        // Arrange
        when(productosRepository.findById("prod-123456")).thenReturn(Optional.of(producto));
        when(estadosRepository.findAll()).thenReturn(Arrays.asList(estadoActivo, estadoInactivo));
        
        Productos productoInactivo = new Productos();
        productoInactivo.setId(producto.getId());
        productoInactivo.setNombre(producto.getNombre());
        productoInactivo.setCategoriasProductos(categoria);
        productoInactivo.setProveedor(proveedor);
        productoInactivo.setEstados(estadoInactivo);
        
        when(productosRepository.save(any(Productos.class))).thenReturn(productoInactivo);
        when(historialPreciosRepository.findLatestByProducto(any())).thenReturn(Optional.of(historialPrecio));
        when(historialCostosRepository.findLatestByProducto(any())).thenReturn(Optional.of(historialCosto));
        when(inventarioRepository.findByProducto(any())).thenReturn(Optional.of(inventario));

        // Act
        ProductosDTO resultado = productoService.desactivarProducto("prod-123456");

        // Assert
        assertNotNull(resultado);
        assertEquals("Inactivo", resultado.getEstadosEstado());
        verify(productosRepository, times(1)).save(any(Productos.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al desactivar producto inexistente")
    void testDesactivarProducto_ProductoNoExiste() {
        // Arrange
        when(productosRepository.findById("prod-inexistente")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productoService.desactivarProducto("prod-inexistente");
        });

        assertEquals("Producto no encontrado con ID: prod-inexistente", exception.getMessage());
        verify(productosRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción al desactivar si no existe estado Inactivo")
    void testDesactivarProducto_EstadoInactivoNoExiste() {
        // Arrange
        when(productosRepository.findById("prod-123456")).thenReturn(Optional.of(producto));
        when(estadosRepository.findAll()).thenReturn(Arrays.asList(estadoActivo)); // Sin estado Inactivo

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productoService.desactivarProducto("prod-123456");
        });

        assertEquals("Estado 'Inactivo' no encontrado en la base de datos", exception.getMessage());
    }

    @Test
    @DisplayName("Debe registrar movimiento de edición exitosamente")
    void testRegistrarMovimientoEdicion_Exitoso() {
        // Arrange
        TipoMovimientos tipoEdicion = new TipoMovimientos();
        tipoEdicion.setId("tipo-edit");
        tipoEdicion.setMovimiento("Edición");

        when(productosRepository.findById("prod-123456")).thenReturn(Optional.of(producto));
        when(usuariosRepository.findById("user-1")).thenReturn(Optional.of(usuario));
        when(ubicacionesRepository.findAll()).thenReturn(Arrays.asList(ubicacion));
        when(tipoMovimientosRepository.findByMovimiento("Edición")).thenReturn(Optional.of(tipoEdicion));

        // Act
        productoService.registrarMovimientoEdicion("prod-123456", "user-1", "Cambio de precio");

        // Assert
        verify(movimientosInventariosRepository, times(1)).save(argThat(mov ->
            mov.getProducto().equals(producto) &&
            mov.getTipoMovimiento().equals(tipoEdicion) &&
            mov.getCantidad().equals(BigDecimal.ZERO) &&
            mov.getClaveMovimiento().contains("EDICION-")
        ));
    }

    @Test
    @DisplayName("Debe registrar movimiento de edición sin usuario ID (usar primer usuario disponible)")
    void testRegistrarMovimientoEdicion_SinUsuarioId() {
        // Arrange
        TipoMovimientos tipoEdicion = new TipoMovimientos();
        tipoEdicion.setId("tipo-edit");
        tipoEdicion.setMovimiento("Edición");

        when(productosRepository.findById("prod-123456")).thenReturn(Optional.of(producto));
        when(usuariosRepository.findAll()).thenReturn(Arrays.asList(usuario));
        when(ubicacionesRepository.findAll()).thenReturn(Arrays.asList(ubicacion));
        when(tipoMovimientosRepository.findByMovimiento("Edición")).thenReturn(Optional.of(tipoEdicion));

        // Act
        productoService.registrarMovimientoEdicion("prod-123456", null, "Cambio de precio");

        // Assert
        verify(movimientosInventariosRepository, times(1)).save(any(MovimientosInventarios.class));
    }

    @Test
    @DisplayName("Debe crear tipo de movimiento si no existe al registrar edición")
    void testRegistrarMovimientoEdicion_CrearTipoMovimiento() {
        // Arrange
        TipoMovimientos nuevoTipoEdicion = new TipoMovimientos();
        nuevoTipoEdicion.setId("tipo-new");
        nuevoTipoEdicion.setMovimiento("Edición");

        when(productosRepository.findById("prod-123456")).thenReturn(Optional.of(producto));
        when(usuariosRepository.findById("user-1")).thenReturn(Optional.of(usuario));
        when(ubicacionesRepository.findAll()).thenReturn(Arrays.asList(ubicacion));
        when(tipoMovimientosRepository.findByMovimiento(anyString())).thenReturn(Optional.empty());
        when(tipoMovimientosRepository.save(any(TipoMovimientos.class))).thenReturn(nuevoTipoEdicion);

        // Act
        productoService.registrarMovimientoEdicion("prod-123456", "user-1", null);

        // Assert
        verify(tipoMovimientosRepository, times(1)).save(argThat(tipo ->
            tipo.getMovimiento().equals("Edición")
        ));
        verify(movimientosInventariosRepository, times(1)).save(any(MovimientosInventarios.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al registrar edición de producto inexistente")
    void testRegistrarMovimientoEdicion_ProductoNoExiste() {
        // Arrange
        when(productosRepository.findById("prod-inexistente")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productoService.registrarMovimientoEdicion("prod-inexistente", "user-1", "Motivo");
        });

        assertEquals("Producto no encontrado con ID: prod-inexistente", exception.getMessage());
        verify(movimientosInventariosRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe convertir Producto a DTO correctamente con todos los datos")
    void testConvertToDTO_Completo() {
        // Arrange
        when(historialPreciosRepository.findLatestByProducto(producto)).thenReturn(Optional.of(historialPrecio));
        when(historialCostosRepository.findLatestByProducto(producto)).thenReturn(Optional.of(historialCosto));
        when(inventarioRepository.findByProducto(producto)).thenReturn(Optional.of(inventario));

        // Act
        ProductosDTO resultado = productoService.convertToDTO(producto);

        // Assert
        assertNotNull(resultado);
        assertEquals("prod-123456", resultado.getId());
        assertEquals("Producto Test", resultado.getNombre());
        assertEquals("cat-1", resultado.getCategoriasProductosId());
        assertEquals("Alimentos", resultado.getCategoriasProductosCategoria());
        assertEquals("prov-1", resultado.getProveedorId());
        assertEquals("Proveedor", resultado.getProveedorNombre());
        assertEquals("Test", resultado.getProveedorApellidoPaterno());
        assertEquals("estado-1", resultado.getEstadosId());
        assertEquals("Activo", resultado.getEstadosEstado());
        assertEquals(new BigDecimal("100.00"), resultado.getPrecioVentaActual());
        assertEquals(new BigDecimal("50.00"), resultado.getPrecioCompraActual());
        assertEquals(100, resultado.getCantidadInventario());
    }

    @Test
    @DisplayName("Debe convertir Producto a DTO con valores por defecto si no hay historial")
    void testConvertToDTO_SinHistorial() {
        // Arrange
        when(historialPreciosRepository.findLatestByProducto(producto)).thenReturn(Optional.empty());
        when(historialCostosRepository.findLatestByProducto(producto)).thenReturn(Optional.empty());
        when(inventarioRepository.findByProducto(producto)).thenReturn(Optional.empty());

        // Act
        ProductosDTO resultado = productoService.convertToDTO(producto);

        // Assert
        assertNotNull(resultado);
        assertEquals(BigDecimal.ZERO, resultado.getPrecioVentaActual());
        assertEquals(BigDecimal.ZERO, resultado.getPrecioCompraActual());
        assertEquals(0, resultado.getCantidadInventario());
    }

    @Test
    @DisplayName("Debe calcular stock total sumando piezas y kilogramos")
    void testConvertToDTO_CalculoStockTotal() {
        // Arrange
        inventario.setCantidadPz(50);
        inventario.setCantidadKg(30);

        when(historialPreciosRepository.findLatestByProducto(producto)).thenReturn(Optional.of(historialPrecio));
        when(historialCostosRepository.findLatestByProducto(producto)).thenReturn(Optional.of(historialCosto));
        when(inventarioRepository.findByProducto(producto)).thenReturn(Optional.of(inventario));

        // Act
        ProductosDTO resultado = productoService.convertToDTO(producto);

        // Assert
        assertEquals(80, resultado.getCantidadInventario()); // 50 + 30
    }
}
