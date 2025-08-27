package com.posfin.pos_finanzas_backend.controllers;

import com.posfin.pos_finanzas_backend.dtos.*;
import com.posfin.pos_finanzas_backend.services.ComprasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de compras a proveedores
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ComprasController {

    @Autowired
    private ComprasService comprasService;

    /**
     * Obtiene lista de proveedores activos con estado de deuda
     * GET /api/proveedores
     */
    @GetMapping("/proveedores")
    public ResponseEntity<List<ProveedorDTO>> obtenerProveedores() {
        try {
            List<ProveedorDTO> proveedores = comprasService.obtenerProveedores();
            return ResponseEntity.ok(proveedores);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Calcula la deuda pendiente de un proveedor específico
     * GET /api/proveedores/{id}/deuda
     */
    @GetMapping("/proveedores/{id}/deuda")
    public ResponseEntity<DeudaProveedorDTO> calcularDeudaProveedor(@PathVariable String id) {
        try {
            DeudaProveedorDTO deuda = comprasService.calcularDeudaProveedor(id);
            return ResponseEntity.ok(deuda);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Obtiene productos de un proveedor específico con precios de compra
     * GET /api/productos/proveedor/{id}
     */
    @GetMapping("/productos/proveedor/{id}")
    public ResponseEntity<List<ProductosDTO>> obtenerProductosProveedor(@PathVariable String id) {
        try {
            List<ProductosDTO> productos = comprasService.obtenerProductosProveedor(id);
            return ResponseEntity.ok(productos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Crea una nueva orden de compra
     * POST /api/ordenes-compras
     */
    @PostMapping("/ordenes-compras")
    public ResponseEntity<OrdenesDeComprasDTO> crearOrdenCompra(@RequestBody CompraRequestDTO compraRequest) {
        try {
            // Validar datos de entrada
            if (compraRequest.getProveedorId() == null || compraRequest.getProveedorId().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
            
            if (compraRequest.getProductos() == null || compraRequest.getProductos().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
            
            OrdenesDeComprasDTO ordenCreada = comprasService.crearOrdenCompra(compraRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(ordenCreada);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Registra un pago a proveedor
     * POST /api/proveedores/{id}/pago
     */
    @PostMapping("/proveedores/{id}/pago")
    public ResponseEntity<HistorialCargosProveedoresDTO> realizarPago(@PathVariable String id, 
                                                                     @RequestBody PagoRequestDTO pagoRequest) {
        try {
            // Validar datos de entrada
            if (pagoRequest.getOrdenCompraId() == null || pagoRequest.getOrdenCompraId().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
            
            if (pagoRequest.getMetodoPagoId() == null || pagoRequest.getMetodoPagoId().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
            
            if (!pagoRequest.isPagarTodoElTotal() && 
                (pagoRequest.getMontoPagado() == null || pagoRequest.getMontoPagado().signum() <= 0)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
            
            // Asegurar que el proveedor del path coincida con el del request
            pagoRequest.setProveedorId(id);
            
            HistorialCargosProveedoresDTO pagoRegistrado = comprasService.realizarPago(pagoRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(pagoRegistrado);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Endpoint alternativo para crear orden de compra y obtener deuda actualizada
     * POST /api/proveedores/{id}/compra
     */
    @PostMapping("/proveedores/{id}/compra")
    public ResponseEntity<DeudaProveedorDTO> crearCompraYObtenerDeuda(@PathVariable String id,
                                                                     @RequestBody CompraRequestDTO compraRequest) {
        try {
            // Asegurar que el proveedor del path coincida con el del request
            compraRequest.setProveedorId(id);
            
            // Crear orden de compra
            comprasService.crearOrdenCompra(compraRequest);
            
            // Retornar deuda actualizada
            DeudaProveedorDTO deudaActualizada = comprasService.calcularDeudaProveedor(id);
            return ResponseEntity.status(HttpStatus.CREATED).body(deudaActualizada);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}