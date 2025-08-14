package com.posfin.pos_finanzas_backend.services;

import com.posfin.pos_finanzas_backend.dtos.DeudaProveedorDTO;
import com.posfin.pos_finanzas_backend.repositories.OrdenesDeComprasRepository;
import com.posfin.pos_finanzas_backend.repositories.HistorialCargosProveedoresRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para el servicio DeudasProveedoresService
 * Verifica el correcto funcionamiento del cálculo y consulta de deudas
 */
@ExtendWith(MockitoExtension.class)
class DeudasProveedoresServiceTest {

    @Mock
    private OrdenesDeComprasRepository ordenesDeComprasRepository;

    @Mock
    private HistorialCargosProveedoresRepository historialCargosProveedoresRepository;

    @InjectMocks
    private DeudasProveedoresService deudasProveedoresService;

    private Object[] datosMockProveedor;

    @BeforeEach
    void setUp() {
        // Datos mock para simular la respuesta de la consulta nativa
        datosMockProveedor = new Object[]{
            "proveedor-id-1",                    // proveedorId
            "Proveedor Test",                    // proveedorNombre
            "555-1234",                          // telefonoProveedor
            "test@proveedor.com",               // emailProveedor
            new BigDecimal("1000.00"),          // totalCompras
            new BigDecimal("300.00"),           // totalPagos
            OffsetDateTime.now().minusDays(30), // fechaOrdenMasAntigua
            3L                                  // cantidadOrdenes
        };
    }

    @Test
    void testObtenerDeudasProveedores_ConDeudasPendientes() {
        // Arrange
        List<Object[]> resultadosMock = new ArrayList<>();
        resultadosMock.add(datosMockProveedor);
        
        when(ordenesDeComprasRepository.obtenerDeudasProveedores())
            .thenReturn(resultadosMock);

        // Act
        List<DeudaProveedorDTO> resultado = deudasProveedoresService.obtenerDeudasProveedores();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        
        DeudaProveedorDTO deuda = resultado.get(0);
        assertEquals("proveedor-id-1", deuda.getProveedorId());
        assertEquals("Proveedor Test", deuda.getProveedorNombre());
        assertEquals("555-1234", deuda.getTelefonoProveedor());
        assertEquals("test@proveedor.com", deuda.getEmailProveedor());
        assertEquals(new BigDecimal("1000.00"), deuda.getTotalCompras());
        assertEquals(new BigDecimal("300.00"), deuda.getTotalPagos());
        assertEquals(new BigDecimal("700.00"), deuda.getDeudaPendiente());
        assertEquals("amarillo", deuda.getEstadoDeuda());
        assertEquals(3, deuda.getCantidadOrdenesPendientes());
        
        verify(ordenesDeComprasRepository).obtenerDeudasProveedores();
    }

    @Test
    void testObtenerDeudasProveedores_SinDeudas() {
        // Arrange
        when(ordenesDeComprasRepository.obtenerDeudasProveedores())
            .thenReturn(new ArrayList<>());

        // Act
        List<DeudaProveedorDTO> resultado = deudasProveedoresService.obtenerDeudasProveedores();

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        
        verify(ordenesDeComprasRepository).obtenerDeudasProveedores();
    }

    @Test
    void testObtenerDeudaProveedor_ConDeuda() {
        // Arrange
        String proveedorId = "proveedor-test";
        BigDecimal totalCompras = new BigDecimal("2000.00");
        BigDecimal totalPagos = new BigDecimal("500.00");
        
        when(ordenesDeComprasRepository.calcularTotalComprasProveedor(proveedorId))
            .thenReturn(totalCompras);
        when(historialCargosProveedoresRepository.calcularTotalPagosProveedor(proveedorId))
            .thenReturn(totalPagos);
        when(ordenesDeComprasRepository.findOrdersMasAntiguasByProveedorId(proveedorId))
            .thenReturn(new ArrayList<>());
        when(ordenesDeComprasRepository.contarOrdenesPendientesByProveedorId(proveedorId))
            .thenReturn(5L);

        // Act
        DeudaProveedorDTO resultado = deudasProveedoresService.obtenerDeudaProveedor(proveedorId);

        // Assert
        assertNotNull(resultado);
        assertEquals(proveedorId, resultado.getProveedorId());
        assertEquals(totalCompras, resultado.getTotalCompras());
        assertEquals(totalPagos, resultado.getTotalPagos());
        assertEquals(new BigDecimal("1500.00"), resultado.getDeudaPendiente());
        assertEquals("amarillo", resultado.getEstadoDeuda());
        assertEquals(5, resultado.getCantidadOrdenesPendientes());
        
        verify(ordenesDeComprasRepository).calcularTotalComprasProveedor(proveedorId);
        verify(historialCargosProveedoresRepository).calcularTotalPagosProveedor(proveedorId);
    }

    @Test
    void testObtenerDeudaProveedor_SinDeuda() {
        // Arrange
        String proveedorId = "proveedor-sin-deuda";
        BigDecimal totalCompras = new BigDecimal("1000.00");
        BigDecimal totalPagos = new BigDecimal("1000.00");
        
        when(ordenesDeComprasRepository.calcularTotalComprasProveedor(proveedorId))
            .thenReturn(totalCompras);
        when(historialCargosProveedoresRepository.calcularTotalPagosProveedor(proveedorId))
            .thenReturn(totalPagos);

        // Act
        DeudaProveedorDTO resultado = deudasProveedoresService.obtenerDeudaProveedor(proveedorId);

        // Assert
        assertNull(resultado);
        
        verify(ordenesDeComprasRepository).calcularTotalComprasProveedor(proveedorId);
        verify(historialCargosProveedoresRepository).calcularTotalPagosProveedor(proveedorId);
    }

    @Test
    void testCalcularTotalDeudasPendientes() {
        // Arrange
        List<Object[]> resultadosMock = new ArrayList<>();
        resultadosMock.add(datosMockProveedor);
        
        // Agregar otro proveedor con deuda
        Object[] segundoProveedor = new Object[]{
            "proveedor-id-2",
            "Segundo Proveedor",
            "555-5678",
            "segundo@proveedor.com",
            new BigDecimal("500.00"),
            new BigDecimal("100.00"),
            OffsetDateTime.now().minusDays(15),
            1L
        };
        resultadosMock.add(segundoProveedor);
        
        when(ordenesDeComprasRepository.obtenerDeudasProveedores())
            .thenReturn(resultadosMock);

        // Act
        BigDecimal totalDeudas = deudasProveedoresService.calcularTotalDeudasPendientes();

        // Assert
        // Primera deuda: 1000 - 300 = 700
        // Segunda deuda: 500 - 100 = 400
        // Total: 700 + 400 = 1100
        assertEquals(new BigDecimal("1100.00"), totalDeudas);
    }

    @Test
    void testObtenerEstadisticasDeudas() {
        // Arrange
        List<Object[]> resultadosMock = new ArrayList<>();
        resultadosMock.add(datosMockProveedor);
        
        when(ordenesDeComprasRepository.obtenerDeudasProveedores())
            .thenReturn(resultadosMock);

        // Act
        BigDecimal[] estadisticas = deudasProveedoresService.obtenerEstadisticasDeudas();

        // Assert
        assertNotNull(estadisticas);
        assertEquals(3, estadisticas.length);
        
        // Verificar total de proveedores con deuda
        assertEquals(new BigDecimal("1"), estadisticas[0]);
        
        // Verificar total de deudas pendientes (1000 - 300 = 700)
        assertEquals(new BigDecimal("700.00"), estadisticas[1]);
        
        // Verificar deuda promedio (700 / 1 = 700)
        assertEquals(new BigDecimal("700.00"), estadisticas[2]);
    }

    @Test
    void testObtenerEstadisticasDeudas_SinProveedores() {
        // Arrange
        when(ordenesDeComprasRepository.obtenerDeudasProveedores())
            .thenReturn(new ArrayList<>());

        // Act
        BigDecimal[] estadisticas = deudasProveedoresService.obtenerEstadisticasDeudas();

        // Assert
        assertNotNull(estadisticas);
        assertEquals(3, estadisticas.length);
        assertEquals(BigDecimal.ZERO, estadisticas[0]); // Total proveedores
        assertEquals(BigDecimal.ZERO, estadisticas[1]); // Total deudas
        assertEquals(BigDecimal.ZERO, estadisticas[2]); // Deuda promedio
    }
}