package com.posfin.pos_finanzas_backend.services;

import com.posfin.pos_finanzas_backend.dtos.*;
import com.posfin.pos_finanzas_backend.models.*;
import com.posfin.pos_finanzas_backend.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar lógica de compras a proveedores y cálculo de deudas
 */
@Service
@Transactional
public class ComprasService {

    @Autowired
    private PersonasRepository personasRepository;
    
    @Autowired
    private OrdenesDeComprasRepository ordenesDeComprasRepository;
    
    @Autowired
    private DetallesOrdenesDeComprasRepository detallesOrdenesDeComprasRepository;
    
    @Autowired
    private HistorialCargosProveedoresRepository historialCargosProveedoresRepository;
    
    @Autowired
    private ProductosRepository productosRepository;
    
    @Autowired
    private EstadosRepository estadosRepository;
    
    @Autowired
    private MetodosPagoRepository metodosPagoRepository;
    
    @Autowired
    private HistorialCostosRepository historialCostosRepository;
    
    @Autowired
    private InventarioRepository inventarioRepository;
    
    @Autowired
    private MovimientosInventariosRepository movimientosInventariosRepository;
    
    @Autowired
    private TIpoMovimientosRepository tipoMovimientosRepository;
    
    @Autowired
    private UbicacionesRepository ubicacionesRepository;

    @Autowired
    private UsuariosRepository usuariosRepository;

    /**
     * Obtiene todos los proveedores activos con su estado de deuda
     */
    public List<ProveedorDTO> obtenerProveedores() {
        List<Personas> proveedores = personasRepository.findProveedoresActivos();
        List<ProveedorDTO> proveedoresDTO = new ArrayList<>();
        
        for (Personas proveedor : proveedores) {
            DeudaProveedorDTO deuda = calcularDeudaProveedor(proveedor.getId());
            
            ProveedorDTO proveedorDTO = new ProveedorDTO(
                proveedor.getId(),
                proveedor.getNombre(),
                proveedor.getApellidoPaterno(),
                proveedor.getApellidoMaterno(),
                proveedor.getTelefono(),
                proveedor.getEmail(),
                proveedor.getEstados().getEstado(),
                deuda.getDeudaPendiente()
            );
            
            proveedoresDTO.add(proveedorDTO);
        }
        
        return proveedoresDTO;
    }

    /**
     * Calcula la deuda pendiente de un proveedor específico
     */
    public DeudaProveedorDTO calcularDeudaProveedor(String proveedorId) {
        Optional<Personas> proveedorOpt = personasRepository.findById(proveedorId);
        if (!proveedorOpt.isPresent()) {
            throw new IllegalArgumentException("Proveedor no encontrado: " + proveedorId);
        }
        
        Personas proveedor = proveedorOpt.get();
        
        // Calcular total de compras
        BigDecimal totalCompras = ordenesDeComprasRepository.calcularTotalComprasProveedor(proveedorId);
        
        // Calcular total de pagos
        BigDecimal totalPagos = historialCargosProveedoresRepository.calcularTotalPagosProveedor(proveedorId);
        
        return new DeudaProveedorDTO(
            proveedorId,
            proveedor.getNombre(),
            totalCompras,
            totalPagos
        );
    }

    /**
     * Obtiene productos de un proveedor específico con precios de compra
     */
    public List<ProductosDTO> obtenerProductosProveedor(String proveedorId) {
        List<Productos> productos = productosRepository.findByProveedorIdAndEstadoActivo(proveedorId);
        List<ProductosDTO> productosDTO = new ArrayList<>();
        
        for (Productos producto : productos) {
            ProductosDTO productoDTO = convertirProductoADTO(producto);
            productosDTO.add(productoDTO);
        }
        
        return productosDTO;
    }

    /**
     * Crea una nueva orden de compra
     */
    public OrdenesDeComprasDTO crearOrdenCompra(CompraRequestDTO compraRequest) {
        // Validar proveedor
        Optional<Personas> proveedorOpt = personasRepository.findById(compraRequest.getProveedorId());
        if (!proveedorOpt.isPresent()) {
            throw new IllegalArgumentException("Proveedor no encontrado: " + compraRequest.getProveedorId());
        }
        
        // Obtener estado "activo"
        Estados estadoActivo = estadosRepository.findByEstado("Activo")
            .orElseThrow(() -> new IllegalStateException("Estado 'Activo' no encontrado"));
        
        // Obtener método de pago por defecto (Efectivo)
        MetodosPago metodoPago;
        if (compraRequest.getMetodoPagoId() != null) {
            metodoPago = metodosPagoRepository.findById(compraRequest.getMetodoPagoId())
                .orElseThrow(() -> new IllegalArgumentException("Método de pago no encontrado"));
        } else {
            metodoPago = metodosPagoRepository.findByMetodoPago("Efectivo")
                .orElseThrow(() -> new IllegalStateException("Método de pago 'Efectivo' no encontrado"));
        }
        
        // Calcular total de la compra
        BigDecimal totalCompra = BigDecimal.ZERO;
        List<DetallesOrdenesDeCompras> detalles = new ArrayList<>();
        
        for (CompraRequestDTO.ProductoCompraDTO productoCompra : compraRequest.getProductos()) {
            Productos producto = productosRepository.findById(productoCompra.getProductoId())
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + productoCompra.getProductoId()));
            
            // Obtener precio de compra más reciente
            Optional<HistorialCostos> historialCostoOpt = historialCostosRepository.findLatestByProducto(producto);
            if (!historialCostoOpt.isPresent()) {
                throw new IllegalStateException("No hay historial de costos para el producto: " + producto.getNombre());
            }
            
            HistorialCostos historialCosto = historialCostoOpt.get();
            BigDecimal precioUnitario = historialCosto.getCosto();
            
            // Calcular subtotal según tipo de cantidad
            BigDecimal subtotal = BigDecimal.ZERO;
            if (productoCompra.getCantidadPz() != null && productoCompra.getCantidadPz().compareTo(BigDecimal.ZERO) > 0) {
                subtotal = subtotal.add(precioUnitario.multiply(productoCompra.getCantidadPz()));
            }
            if (productoCompra.getCantidadKg() != null && productoCompra.getCantidadKg().compareTo(BigDecimal.ZERO) > 0) {
                subtotal = subtotal.add(precioUnitario.multiply(productoCompra.getCantidadKg()));
            }
            
            totalCompra = totalCompra.add(subtotal);
            
            // Crear detalle de orden
            DetallesOrdenesDeCompras detalle = new DetallesOrdenesDeCompras();
            detalle.setProducto(producto);
            detalle.setCantidadPz(productoCompra.getCantidadPz() != null ? productoCompra.getCantidadPz() : BigDecimal.ZERO);
            detalle.setCantidadKg(productoCompra.getCantidadKg() != null ? productoCompra.getCantidadKg() : BigDecimal.ZERO);
            detalle.setHistorialCosto(historialCosto);
            
            detalles.add(detalle);
        }
        
        // Crear orden de compra
        OrdenesDeCompras orden = new OrdenesDeCompras();
        orden.setPersona(proveedorOpt.get());
        orden.setFechaOrden(OffsetDateTime.now());
        orden.setEstado(estadoActivo);
        orden.setTotalCompra(totalCompra);
        
        // Guardar orden
        orden = ordenesDeComprasRepository.save(orden);
        
        // Guardar detalles y actualizar inventario
        for (DetallesOrdenesDeCompras detalle : detalles) {
            detalle.setOrdenDeCompra(orden);
            detallesOrdenesDeComprasRepository.save(detalle);
            
            // Actualizar inventario (incrementar stock)
            actualizarInventarioCompra(detalle.getProducto(), detalle.getCantidadPz(), detalle.getCantidadKg());
            
            // Registrar movimiento de inventario
            registrarMovimientoCompra(detalle.getProducto(), detalle.getCantidadPz(), detalle.getCantidadKg(), orden.getId());
        }
        
        return convertirOrdenADTO(orden);
    }

    /**
     * Registra un pago a proveedor
     */
    public HistorialCargosProveedoresDTO realizarPago(PagoRequestDTO pagoRequest) {
        // Validar proveedor
        Optional<Personas> proveedorOpt = personasRepository.findById(pagoRequest.getProveedorId());
        if (!proveedorOpt.isPresent()) {
            throw new IllegalArgumentException("Proveedor no encontrado: " + pagoRequest.getProveedorId());
        }
        
        // Validar orden de compra
        Optional<OrdenesDeCompras> ordenOpt = ordenesDeComprasRepository.findById(pagoRequest.getOrdenCompraId());
        if (!ordenOpt.isPresent()) {
            throw new IllegalArgumentException("Orden de compra no encontrada: " + pagoRequest.getOrdenCompraId());
        }
        
        // Validar método de pago
        MetodosPago metodoPago = metodosPagoRepository.findById(pagoRequest.getMetodoPagoId())
            .orElseThrow(() -> new IllegalArgumentException("Método de pago no encontrado: " + pagoRequest.getMetodoPagoId()));
        
        OrdenesDeCompras orden = ordenOpt.get();
        BigDecimal montoPago;
        
        if (pagoRequest.isPagarTodoElTotal()) {
            // Calcular deuda pendiente para esta orden específica
            BigDecimal totalPagadoOrden = historialCargosProveedoresRepository.calcularTotalPagosOrdenCompra(orden.getId());
            montoPago = orden.getTotalCompra().subtract(totalPagadoOrden);
            
            if (montoPago.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Esta orden ya está completamente pagada");
            }
        } else {
            montoPago = pagoRequest.getMontoPagado();
            
            // Validar que el monto no sea mayor a la deuda pendiente
            BigDecimal totalPagadoOrden = historialCargosProveedoresRepository.calcularTotalPagosOrdenCompra(orden.getId());
            BigDecimal deudaPendienteOrden = orden.getTotalCompra().subtract(totalPagadoOrden);
            
            if (montoPago.compareTo(deudaPendienteOrden) > 0) {
                throw new IllegalArgumentException("El monto a pagar no puede ser mayor a la deuda pendiente: $" + deudaPendienteOrden);
            }
            
            if (montoPago.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("El monto a pagar debe ser mayor a cero");
            }
        }
        
        // Crear registro de pago
        HistorialCargosProveedores pago = new HistorialCargosProveedores();
        pago.setPersona(proveedorOpt.get());
        pago.setOrdenDeCompra(orden);
        pago.setMontoPagado(montoPago);
        pago.setFecha(OffsetDateTime.now());
        pago.setMetodoPago(metodoPago);  // Asignar método de pago
        
        // Guardar pago
        pago = historialCargosProveedoresRepository.save(pago);
        
        return convertirPagoADTO(pago);
    }

    // Métodos auxiliares privados
    
    private ProductosDTO convertirProductoADTO(Productos producto) {
        ProductosDTO dto = new ProductosDTO();
        dto.setId(producto.getId());
        dto.setNombre(producto.getNombre());
        dto.setEstadosEstado(producto.getEstados().getEstado());
        
        // Obtener precio de compra más reciente
        Optional<HistorialCostos> historialCostoOpt = historialCostosRepository.findLatestByProducto(producto);
        if (historialCostoOpt.isPresent()) {
            dto.setPrecioCompraActual(historialCostoOpt.get().getCosto());
        } else {
            dto.setPrecioCompraActual(BigDecimal.ZERO);
        }
        
        // Obtener stock actual
        Optional<Inventarios> inventarioOpt = inventarioRepository.findByProducto(producto);
        if (inventarioOpt.isPresent()) {
            Inventarios inventario = inventarioOpt.get();
            int totalStock = (inventario.getCantidadPz() != null ? inventario.getCantidadPz() : 0) + 
                           (inventario.getCantidadKg() != null ? inventario.getCantidadKg() : 0);
            dto.setCantidadInventario(totalStock);
        } else {
            dto.setCantidadInventario(0);
        }
        
        return dto;
    }
    
    private OrdenesDeComprasDTO convertirOrdenADTO(OrdenesDeCompras orden) {
        OrdenesDeComprasDTO dto = new OrdenesDeComprasDTO();
        dto.setId(orden.getId());
        dto.setPersonaId(orden.getPersona().getId());
        dto.setPersonaNombre(orden.getPersona().getNombre());
        dto.setPersonaTelefono(orden.getPersona().getTelefono());
        dto.setFechaOrden(orden.getFechaOrden());
        dto.setEstadoNombre(orden.getEstado().getEstado());
        dto.setTotalCompra(orden.getTotalCompra());
        
        return dto;
    }
    
    private HistorialCargosProveedoresDTO convertirPagoADTO(HistorialCargosProveedores pago) {
        HistorialCargosProveedoresDTO dto = new HistorialCargosProveedoresDTO();
        dto.setId(pago.getId());
        dto.setPersonaId(pago.getPersona().getId());
        dto.setPersonaNombre(pago.getPersona().getNombre());
        dto.setOrdenDeCompraId(pago.getOrdenDeCompra().getId());
        dto.setMontoPagado(pago.getMontoPagado());
        dto.setFecha(pago.getFecha());
        
        return dto;
    }
    
    private void actualizarInventarioCompra(Productos producto, BigDecimal cantidadPz, BigDecimal cantidadKg) {
        Optional<Inventarios> inventarioOpt = inventarioRepository.findByProducto(producto);
        
        Inventarios inventario;
        if (inventarioOpt.isPresent()) {
            inventario = inventarioOpt.get();
        } else {
            // Crear nuevo inventario si no existe
            inventario = new Inventarios();
            inventario.setProducto(producto);
            inventario.setCantidadPz(0);
            inventario.setCantidadKg(0);
        }
        
        // Incrementar stock (compra añade inventario)
        if (cantidadPz != null && cantidadPz.compareTo(BigDecimal.ZERO) > 0) {
            Integer stockActualPz = inventario.getCantidadPz() != null ? inventario.getCantidadPz() : 0;
            inventario.setCantidadPz(stockActualPz + cantidadPz.intValue());
        }
        
        if (cantidadKg != null && cantidadKg.compareTo(BigDecimal.ZERO) > 0) {
            Integer stockActualKg = inventario.getCantidadKg() != null ? inventario.getCantidadKg() : 0;
            inventario.setCantidadKg(stockActualKg + cantidadKg.intValue());
        }
        
        inventarioRepository.save(inventario);
    }
    
    private void registrarMovimientoCompra(Productos producto, BigDecimal cantidadPz, BigDecimal cantidadKg, String ordenCompraId) {
        // Obtener usuario autenticado
        String usuarioAutenticado = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuarios usuario = usuariosRepository.findByNombre(usuarioAutenticado)
            .orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado: " + usuarioAutenticado));
        
        // Obtener o crear tipo de movimiento "Compra"
        TipoMovimientos tipoCompra = tipoMovimientosRepository.findByMovimiento("Compra")
            .orElseGet(() -> {
                TipoMovimientos nuevoTipo = new TipoMovimientos();
                nuevoTipo.setMovimiento("Compra");
                return tipoMovimientosRepository.save(nuevoTipo);
            });
        
        // Obtener primera ubicación disponible
        Ubicaciones ubicacion = ubicacionesRepository.findAll().stream()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No hay ubicaciones disponibles en el sistema"));
        
        // Crear movimiento de inventario
        MovimientosInventarios movimiento = new MovimientosInventarios();
        movimiento.setProducto(producto);
        movimiento.setTipoMovimiento(tipoCompra);
        movimiento.setUbicacion(ubicacion);
        movimiento.setUsuario(usuario); // ✅ Asignar usuario autenticado
        movimiento.setFechaMovimiento(OffsetDateTime.now());
        movimiento.setClaveMovimiento("COMPRA-" + ordenCompraId + "-" + producto.getId());
        
        // Establecer cantidad según el tipo
        if (cantidadPz != null && cantidadPz.compareTo(BigDecimal.ZERO) > 0) {
            movimiento.setCantidad(cantidadPz);
        } else if (cantidadKg != null && cantidadKg.compareTo(BigDecimal.ZERO) > 0) {
            movimiento.setCantidad(cantidadKg);
        } else {
            movimiento.setCantidad(BigDecimal.ZERO);
        }
        
        movimientosInventariosRepository.save(movimiento);
    }
}