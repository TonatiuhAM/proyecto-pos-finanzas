package com.posfin.pos_finanzas_backend.services;

import com.posfin.pos_finanzas_backend.dtos.OrdenesWorkspaceDTO;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de OrdenesWorkspaceService")
class OrdenesWorkspaceServiceTest {

    @Mock
    private OrdenesWorkspaceRepository ordenesWorkspaceRepository;

    @Mock
    private WorkspacesRepository workspacesRepository;

    @Mock
    private ProductosRepository productosRepository;

    @Mock
    private HistorialPreciosRepository historialPreciosRepository;

    @Mock
    private InventarioRepository inventarioRepository;

    @InjectMocks
    private OrdenesWorkspaceService ordenesWorkspaceService;

    private Workspaces workspace;
    private Productos producto;
    private HistorialPrecios historialPrecio;
    private Inventarios inventario;
    private OrdenesWorkspace ordenWorkspace;

    @BeforeEach
    void setUp() {
        workspace = new Workspaces();
        workspace.setId("workspace-1");
        workspace.setNombre("Mesa 1");

        producto = new Productos();
        producto.setId("producto-1");
        producto.setNombre("Producto Test");

        historialPrecio = new HistorialPrecios();
        historialPrecio.setId("precio-1");
        historialPrecio.setPrecio(new BigDecimal("100.00"));
        historialPrecio.setProductos(producto);

        inventario = new Inventarios();
        inventario.setId("inventario-1");
        inventario.setProducto(producto);
        inventario.setCantidadPz(100);
        inventario.setCantidadKg(50);

        ordenWorkspace = new OrdenesWorkspace();
        ordenWorkspace.setId("orden-1");
        ordenWorkspace.setWorkspace(workspace);
        ordenWorkspace.setProducto(producto);
        ordenWorkspace.setHistorialPrecio(historialPrecio);
        ordenWorkspace.setCantidadPz(new BigDecimal("5"));
        ordenWorkspace.setCantidadKg(new BigDecimal("0"));
    }

    @Test
    @DisplayName("Debe obtener órdenes por workspace correctamente")
    void testObtenerOrdenesPorWorkspace_Exitoso() {
        when(ordenesWorkspaceRepository.findByWorkspaceId("workspace-1"))
            .thenReturn(Arrays.asList(ordenWorkspace));

        List<OrdenesWorkspaceDTO> resultado = ordenesWorkspaceService
            .obtenerOrdenesPorWorkspace("workspace-1");

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("orden-1", resultado.get(0).getId());
        verify(ordenesWorkspaceRepository, times(1)).findByWorkspaceId("workspace-1");
    }

    @Test
    @DisplayName("Debe agregar producto nuevo exitosamente")
    void testAgregarOActualizarProducto_NuevoProducto() {
        when(workspacesRepository.findById("workspace-1")).thenReturn(Optional.of(workspace));
        when(productosRepository.findById("producto-1")).thenReturn(Optional.of(producto));
        when(historialPreciosRepository.findLatestByProducto(producto))
            .thenReturn(Optional.of(historialPrecio));
        when(inventarioRepository.findByProducto(producto)).thenReturn(Optional.of(inventario));
        when(ordenesWorkspaceRepository.findByWorkspaceId("workspace-1"))
            .thenReturn(Collections.emptyList());
        when(ordenesWorkspaceRepository.save(any(OrdenesWorkspace.class))).thenReturn(ordenWorkspace);

        OrdenesWorkspaceDTO resultado = ordenesWorkspaceService.agregarOActualizarProducto(
            "workspace-1", "producto-1", new BigDecimal("5"), new BigDecimal("0")
        );

        assertNotNull(resultado);
        verify(ordenesWorkspaceRepository, times(1)).save(any(OrdenesWorkspace.class));
        verify(inventarioRepository, times(1)).save(argThat(inv -> 
            inv.getCantidadPz() == 95 // 100 - 5
        ));
    }

    @Test
    @DisplayName("Debe actualizar producto existente sumando cantidades")
    void testAgregarOActualizarProducto_ActualizarExistente() {
        when(workspacesRepository.findById("workspace-1")).thenReturn(Optional.of(workspace));
        when(productosRepository.findById("producto-1")).thenReturn(Optional.of(producto));
        when(historialPreciosRepository.findLatestByProducto(producto))
            .thenReturn(Optional.of(historialPrecio));
        when(inventarioRepository.findByProducto(producto)).thenReturn(Optional.of(inventario));
        when(ordenesWorkspaceRepository.findByWorkspaceId("workspace-1"))
            .thenReturn(Arrays.asList(ordenWorkspace));
        when(ordenesWorkspaceRepository.save(any(OrdenesWorkspace.class))).thenReturn(ordenWorkspace);

        OrdenesWorkspaceDTO resultado = ordenesWorkspaceService.agregarOActualizarProducto(
            "workspace-1", "producto-1", new BigDecimal("3"), new BigDecimal("0")
        );

        assertNotNull(resultado);
        verify(ordenesWorkspaceRepository, times(1)).save(argThat(orden ->
            orden.getCantidadPz().compareTo(new BigDecimal("8")) == 0 // 5 + 3
        ));
    }

    @Test
    @DisplayName("Debe lanzar excepción si workspace no existe")
    void testAgregarOActualizarProducto_WorkspaceNoExiste() {
        when(workspacesRepository.findById("workspace-1")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ordenesWorkspaceService.agregarOActualizarProducto(
                "workspace-1", "producto-1", new BigDecimal("5"), new BigDecimal("0")
            );
        });

        assertEquals("Workspace no encontrado: workspace-1", exception.getMessage());
        verify(ordenesWorkspaceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción si producto no existe")
    void testAgregarOActualizarProducto_ProductoNoExiste() {
        when(workspacesRepository.findById("workspace-1")).thenReturn(Optional.of(workspace));
        when(productosRepository.findById("producto-1")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ordenesWorkspaceService.agregarOActualizarProducto(
                "workspace-1", "producto-1", new BigDecimal("5"), new BigDecimal("0")
            );
        });

        assertEquals("Producto no encontrado: producto-1", exception.getMessage());
        verify(ordenesWorkspaceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción si no hay precio disponible")
    void testAgregarOActualizarProducto_SinPrecio() {
        when(workspacesRepository.findById("workspace-1")).thenReturn(Optional.of(workspace));
        when(productosRepository.findById("producto-1")).thenReturn(Optional.of(producto));
        when(historialPreciosRepository.findLatestByProducto(producto))
            .thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            ordenesWorkspaceService.agregarOActualizarProducto(
                "workspace-1", "producto-1", new BigDecimal("5"), new BigDecimal("0")
            );
        });

        assertEquals("No hay precio disponible para el producto: Producto Test", 
            exception.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar excepción por stock insuficiente en piezas")
    void testAgregarOActualizarProducto_StockInsuficientePiezas() {
        inventario.setCantidadPz(3); // Menos de lo solicitado

        when(workspacesRepository.findById("workspace-1")).thenReturn(Optional.of(workspace));
        when(productosRepository.findById("producto-1")).thenReturn(Optional.of(producto));
        when(historialPreciosRepository.findLatestByProducto(producto))
            .thenReturn(Optional.of(historialPrecio));
        when(inventarioRepository.findByProducto(producto)).thenReturn(Optional.of(inventario));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            ordenesWorkspaceService.agregarOActualizarProducto(
                "workspace-1", "producto-1", new BigDecimal("5"), new BigDecimal("0")
            );
        });

        assertTrue(exception.getMessage().contains("Stock insuficiente en Piezas"));
        verify(ordenesWorkspaceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe limpiar órdenes de workspace y restaurar inventario")
    void testLimpiarOrdenesWorkspace_Exitoso() {
        List<OrdenesWorkspace> ordenes = Arrays.asList(ordenWorkspace);

        when(ordenesWorkspaceRepository.findByWorkspaceId("workspace-1")).thenReturn(ordenes);
        when(inventarioRepository.findByProducto(producto)).thenReturn(Optional.of(inventario));

        ordenesWorkspaceService.limpiarOrdenesWorkspace("workspace-1");

        verify(inventarioRepository, times(1)).save(argThat(inv ->
            inv.getCantidadPz() == 105 // 100 + 5 (restaurado)
        ));
        verify(ordenesWorkspaceRepository, times(1)).deleteAll(ordenes);
    }

    @Test
    @DisplayName("Debe eliminar orden específica exitosamente")
    void testEliminarOrden_Exitoso() {
        when(ordenesWorkspaceRepository.existsById("orden-1")).thenReturn(true);

        boolean resultado = ordenesWorkspaceService.eliminarOrden("orden-1");

        assertTrue(resultado);
        verify(ordenesWorkspaceRepository, times(1)).deleteById("orden-1");
    }

    @Test
    @DisplayName("Debe retornar false al eliminar orden inexistente")
    void testEliminarOrden_OrdenNoExiste() {
        when(ordenesWorkspaceRepository.existsById("orden-inexistente")).thenReturn(false);

        boolean resultado = ordenesWorkspaceService.eliminarOrden("orden-inexistente");

        assertFalse(resultado);
        verify(ordenesWorkspaceRepository, never()).deleteById(anyString());
    }

    @Test
    @DisplayName("Debe crear inventario si no existe al restaurar")
    void testLimpiarOrdenesWorkspace_CrearInventarioSiNoExiste() {
        List<OrdenesWorkspace> ordenes = Arrays.asList(ordenWorkspace);

        when(ordenesWorkspaceRepository.findByWorkspaceId("workspace-1")).thenReturn(ordenes);
        when(inventarioRepository.findByProducto(producto)).thenReturn(Optional.empty());

        ordenesWorkspaceService.limpiarOrdenesWorkspace("workspace-1");

        verify(inventarioRepository, times(1)).save(argThat(inv ->
            inv.getProducto().equals(producto) &&
            inv.getCantidadPz() == 5 &&
            inv.getCantidadKg() == 0
        ));
    }
}
