package com.posfin.pos_finanzas_backend.services;

import com.posfin.pos_finanzas_backend.dtos.DeudaProveedorDTO;
import com.posfin.pos_finanzas_backend.repositories.OrdenesDeComprasRepository;
import com.posfin.pos_finanzas_backend.repositories.HistorialCargosProveedoresRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para calcular y gestionar las deudas a proveedores
 * Proporciona métodos para obtener información detallada de las deudas pendientes
 */
@Service
public class DeudasProveedoresService {

    private static final Logger logger = LoggerFactory.getLogger(DeudasProveedoresService.class);

    @Autowired
    private OrdenesDeComprasRepository ordenesDeComprasRepository;

    @Autowired
    private HistorialCargosProveedoresRepository historialCargosProveedoresRepository;

    /**
     * Obtiene la lista de todos los proveedores con deudas pendientes
     * @return Lista de DTOs con información de deudas de proveedores
     */
    public List<DeudaProveedorDTO> obtenerDeudasProveedores() {
        try {
            logger.info("Iniciando cálculo de deudas de proveedores...");
            List<Object[]> resultados = ordenesDeComprasRepository.obtenerDeudasProveedores();
            logger.info("Consulta ejecutada exitosamente. Registros encontrados: {}", resultados.size());
            
            List<DeudaProveedorDTO> deudasProveedores = new ArrayList<>();

            for (Object[] resultado : resultados) {
                try {
                    String proveedorId = (String) resultado[0];
                    String proveedorNombre = (String) resultado[1];
                    String telefonoProveedor = (String) resultado[2];
                    String emailProveedor = (String) resultado[3];
                    BigDecimal totalCompras = (BigDecimal) resultado[4];
                    BigDecimal totalPagos = (BigDecimal) resultado[5];
                    
                    // Manejar fecha que puede venir como Timestamp o OffsetDateTime
                    OffsetDateTime fechaOrdenMasAntigua = null;
                    if (resultado[6] != null) {
                        if (resultado[6] instanceof Timestamp) {
                            fechaOrdenMasAntigua = ((Timestamp) resultado[6]).toInstant().atOffset(java.time.ZoneOffset.UTC);
                        } else if (resultado[6] instanceof OffsetDateTime) {
                            fechaOrdenMasAntigua = (OffsetDateTime) resultado[6];
                        }
                    }
                    
                    Long cantidadOrdenes = (Long) resultado[7];

                    // Asegurar que los valores no sean null
                    totalCompras = totalCompras != null ? totalCompras : BigDecimal.ZERO;
                    totalPagos = totalPagos != null ? totalPagos : BigDecimal.ZERO;
                    int cantidadOrdenesInt = cantidadOrdenes != null ? cantidadOrdenes.intValue() : 0;

                    // Solo agregar si hay deuda pendiente
                    BigDecimal deudaPendiente = totalCompras.subtract(totalPagos);
                    if (deudaPendiente.compareTo(BigDecimal.ZERO) > 0) {
                        DeudaProveedorDTO deudaDTO = new DeudaProveedorDTO(
                            proveedorId,
                            proveedorNombre,
                            telefonoProveedor,
                            emailProveedor,
                            totalCompras,
                            totalPagos,
                            fechaOrdenMasAntigua,
                            cantidadOrdenesInt
                        );

                        deudasProveedores.add(deudaDTO);
                        logger.debug("Proveedor agregado: {} - Deuda: {}", proveedorNombre, deudaPendiente);
                    }
                } catch (Exception e) {
                    logger.error("Error procesando resultado individual: {}", e.getMessage(), e);
                }
            }

            logger.info("Proceso completado. Total de proveedores con deuda: {}", deudasProveedores.size());
            return deudasProveedores;
        } catch (Exception e) {
            logger.error("Error ejecutando consulta de deudas de proveedores: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener deudas de proveedores", e);
        }
    }

    /**
     * Obtiene la deuda específica de un proveedor
     * @param proveedorId ID del proveedor
     * @return DTO con información de deuda del proveedor, o null si no tiene deudas
     */
    public DeudaProveedorDTO obtenerDeudaProveedor(String proveedorId) {
        BigDecimal totalCompras = ordenesDeComprasRepository.calcularTotalComprasProveedor(proveedorId);
        BigDecimal totalPagos = historialCargosProveedoresRepository.calcularTotalPagosProveedor(proveedorId);
        
        BigDecimal deudaPendiente = totalCompras.subtract(totalPagos);
        
        // Solo devolver información si hay deuda pendiente
        if (deudaPendiente.compareTo(BigDecimal.ZERO) > 0) {
            // Obtener información adicional del proveedor
            var ordenesMasAntiguas = ordenesDeComprasRepository.findOrdersMasAntiguasByProveedorId(proveedorId);
            OffsetDateTime fechaOrdenMasAntigua = null;
            if (!ordenesMasAntiguas.isEmpty()) {
                fechaOrdenMasAntigua = ordenesMasAntiguas.get(0).getFechaOrden();
            }
            
            Long cantidadOrdenesPendientes = ordenesDeComprasRepository.contarOrdenesPendientesByProveedorId(proveedorId);
            
            return new DeudaProveedorDTO(
                proveedorId,
                "", // El nombre se puede obtener de otra consulta si es necesario
                "",
                "",
                totalCompras,
                totalPagos,
                fechaOrdenMasAntigua,
                cantidadOrdenesPendientes != null ? cantidadOrdenesPendientes.intValue() : 0
            );
        }
        
        return null;
    }

    /**
     * Calcula el total de deudas pendientes de todos los proveedores
     * @return Total de deudas pendientes
     */
    public BigDecimal calcularTotalDeudasPendientes() {
        List<DeudaProveedorDTO> deudas = obtenerDeudasProveedores();
        return deudas.stream()
                .map(DeudaProveedorDTO::getDeudaPendiente)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Obtiene estadísticas generales de deudas
     * @return Array con [totalProveedoresConDeuda, totalDeudasPendientes, deudaPromedio]
     */
    public BigDecimal[] obtenerEstadisticasDeudas() {
        List<DeudaProveedorDTO> deudas = obtenerDeudasProveedores();
        
        BigDecimal totalProveedoresConDeuda = new BigDecimal(deudas.size());
        BigDecimal totalDeudasPendientes = calcularTotalDeudasPendientes();
        BigDecimal deudaPromedio = totalProveedoresConDeuda.compareTo(BigDecimal.ZERO) > 0 
            ? totalDeudasPendientes.divide(totalProveedoresConDeuda, 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
        
        return new BigDecimal[]{totalProveedoresConDeuda, totalDeudasPendientes, deudaPromedio};
    }
}