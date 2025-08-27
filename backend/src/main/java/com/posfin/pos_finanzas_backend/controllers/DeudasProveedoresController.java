package com.posfin.pos_finanzas_backend.controllers;

import com.posfin.pos_finanzas_backend.dtos.DeudaProveedorDTO;
import com.posfin.pos_finanzas_backend.services.DeudasProveedoresService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestionar las consultas de deudas a proveedores
 * Proporciona endpoints para obtener información detallada de deudas pendientes
 */
@RestController
@RequestMapping("/api/deudas-proveedores")
@CrossOrigin(origins = "*")
public class DeudasProveedoresController {

    @Autowired
    private DeudasProveedoresService deudasProveedoresService;

    /**
     * Obtiene la lista completa de proveedores con deudas pendientes
     * @return Lista de DTOs con información de deudas de proveedores
     */
    @GetMapping
    public ResponseEntity<List<DeudaProveedorDTO>> obtenerDeudasProveedores() {
        try {
            List<DeudaProveedorDTO> deudas = deudasProveedoresService.obtenerDeudasProveedores();
            return ResponseEntity.ok(deudas);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtiene la deuda específica de un proveedor
     * @param proveedorId ID del proveedor
     * @return DTO con información de deuda del proveedor
     */
    @GetMapping("/{proveedorId}")
    public ResponseEntity<DeudaProveedorDTO> obtenerDeudaProveedor(@PathVariable String proveedorId) {
        try {
            DeudaProveedorDTO deuda = deudasProveedoresService.obtenerDeudaProveedor(proveedorId);
            if (deuda != null) {
                return ResponseEntity.ok(deuda);
            } else {
                return ResponseEntity.noContent().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtiene estadísticas generales de las deudas a proveedores
     * @return Mapa con estadísticas: total proveedores con deuda, total deudas pendientes, deuda promedio
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasDeudas() {
        try {
            BigDecimal[] estadisticas = deudasProveedoresService.obtenerEstadisticasDeudas();
            
            Map<String, Object> response = new HashMap<>();
            response.put("totalProveedoresConDeuda", estadisticas[0]);
            response.put("totalDeudasPendientes", estadisticas[1]);
            response.put("deudaPromedio", estadisticas[2]);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtiene el total de todas las deudas pendientes
     * @return Total de deudas pendientes
     */
    @GetMapping("/total")
    public ResponseEntity<Map<String, BigDecimal>> obtenerTotalDeudasPendientes() {
        try {
            BigDecimal total = deudasProveedoresService.calcularTotalDeudasPendientes();
            
            Map<String, BigDecimal> response = new HashMap<>();
            response.put("totalDeudasPendientes", total);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}