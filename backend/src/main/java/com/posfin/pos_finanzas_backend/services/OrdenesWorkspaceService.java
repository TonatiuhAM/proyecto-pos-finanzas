package com.posfin.pos_finanzas_backend.services;

import com.posfin.pos_finanzas_backend.models.OrdenesWorkspace;
import com.posfin.pos_finanzas_backend.models.Workspaces;
import com.posfin.pos_finanzas_backend.models.Productos;
import com.posfin.pos_finanzas_backend.models.HistorialPrecios;
import com.posfin.pos_finanzas_backend.models.Inventarios;
import com.posfin.pos_finanzas_backend.dtos.OrdenesWorkspaceDTO;
import com.posfin.pos_finanzas_backend.repositories.OrdenesWorkspaceRepository;
import com.posfin.pos_finanzas_backend.repositories.WorkspacesRepository;
import com.posfin.pos_finanzas_backend.repositories.ProductosRepository;
import com.posfin.pos_finanzas_backend.repositories.HistorialPreciosRepository;
import com.posfin.pos_finanzas_backend.repositories.InventarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.math.BigDecimal;

/**
 * Servicio para gestionar las órdenes de workspace en el punto de venta.
 * Maneja la lógica de negocio para agregar productos al carrito temporal
 * y validar disponibilidad de stock.
 */
@Service
@Transactional
public class OrdenesWorkspaceService {

    @Autowired
    private OrdenesWorkspaceRepository ordenesWorkspaceRepository;

    @Autowired
    private WorkspacesRepository workspacesRepository;

    @Autowired
    private ProductosRepository productosRepository;

    @Autowired
    private HistorialPreciosRepository historialPreciosRepository;

    @Autowired
    private InventarioRepository inventarioRepository;

    /**
     * Obtiene todas las órdenes de un workspace específico.
     */
    public List<OrdenesWorkspaceDTO> obtenerOrdenesPorWorkspace(String workspaceId) {
        List<OrdenesWorkspace> ordenes = ordenesWorkspaceRepository.findByWorkspaceId(workspaceId);

        return ordenes.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Agrega un producto al workspace o actualiza la cantidad si ya existe.
     * Implementa la lógica de suma de cantidades para productos duplicados.
     */
    public OrdenesWorkspaceDTO agregarOActualizarProducto(String workspaceId, String productoId,
            BigDecimal cantidadPz, BigDecimal cantidadKg) {
        // Validar entidades requeridas
        Workspaces workspace = validarWorkspace(workspaceId);
        Productos producto = validarProducto(productoId);

        // Obtener el precio más reciente del producto
        HistorialPrecios precioActual = obtenerPrecioActual(producto);

        // Validar stock disponible antes de cualquier operación
        validarStockDisponible(producto, cantidadPz, cantidadKg);

        // Buscar si ya existe una orden para este producto en este workspace
        Optional<OrdenesWorkspace> ordenExistente = buscarOrdenExistente(workspaceId, productoId);

        OrdenesWorkspace orden;

        if (ordenExistente.isPresent()) {
            // Actualizar orden existente sumando las cantidades
            orden = ordenExistente.get();

            // Obtener cantidades anteriores para calcular el incremento
            BigDecimal cantidadAnteriorPz = orden.getCantidadPz() != null ? orden.getCantidadPz() : BigDecimal.ZERO;
            BigDecimal cantidadAnteriorKg = orden.getCantidadKg() != null ? orden.getCantidadKg() : BigDecimal.ZERO;

            BigDecimal nuevaCantidadPz = cantidadAnteriorPz.add(cantidadPz != null ? cantidadPz : BigDecimal.ZERO);
            BigDecimal nuevaCantidadKg = cantidadAnteriorKg.add(cantidadKg != null ? cantidadKg : BigDecimal.ZERO);

            // ✅ NOTA: No se necesita segunda validación aquí porque el stock ya fue
            // validado arriba
            // La primera validación ya verificó que hay suficiente stock disponible para la
            // cantidad adicional

            orden.setCantidadPz(nuevaCantidadPz);
            orden.setCantidadKg(nuevaCantidadKg);
            // Actualizar al precio más reciente
            orden.setHistorialPrecio(precioActual);

            // ✅ CORRECCIÓN: Decrementar inventario SOLO por la cantidad adicional (no la
            // total)
            decrementarInventario(producto, cantidadPz, cantidadKg);
        } else {
            // Crear nueva orden
            orden = new OrdenesWorkspace();
            orden.setWorkspace(workspace);
            orden.setProducto(producto);
            orden.setHistorialPrecio(precioActual);
            orden.setCantidadPz(cantidadPz != null ? cantidadPz : BigDecimal.ZERO);
            orden.setCantidadKg(cantidadKg != null ? cantidadKg : BigDecimal.ZERO);

            // Decrementar inventario por la cantidad total (nueva orden)
            decrementarInventario(producto, cantidadPz, cantidadKg);
        }

        OrdenesWorkspace ordenGuardada = ordenesWorkspaceRepository.save(orden);
        return convertirADTO(ordenGuardada);
    }

    /**
     * Elimina todas las órdenes de un workspace específico y restaura el
     * inventario.
     */
    public void limpiarOrdenesWorkspace(String workspaceId) {
        List<OrdenesWorkspace> ordenes = ordenesWorkspaceRepository.findByWorkspaceId(workspaceId);

        // Restaurar inventario antes de eliminar las órdenes
        for (OrdenesWorkspace orden : ordenes) {
            restaurarInventario(orden.getProducto(), orden.getCantidadPz(), orden.getCantidadKg());
        }

        ordenesWorkspaceRepository.deleteAll(ordenes);
    }

    /**
     * Elimina una orden específica del workspace.
     */
    public boolean eliminarOrden(String ordenId) {
        if (ordenesWorkspaceRepository.existsById(ordenId)) {
            ordenesWorkspaceRepository.deleteById(ordenId);
            return true;
        }
        return false;
    }

    // Métodos privados de utilidad

    private Workspaces validarWorkspace(String workspaceId) {
        return workspacesRepository.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Workspace no encontrado: " + workspaceId));
    }

    private Productos validarProducto(String productoId) {
        return productosRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + productoId));
    }

    private HistorialPrecios obtenerPrecioActual(Productos producto) {
        Optional<HistorialPrecios> precioOpt = historialPreciosRepository.findLatestByProducto(producto);
        if (precioOpt.isEmpty()) {
            throw new IllegalStateException("No hay precio disponible para el producto: " + producto.getNombre());
        }
        return precioOpt.get();
    }

    private void validarStockDisponible(Productos producto, BigDecimal cantidadPz, BigDecimal cantidadKg) {
        Optional<Inventarios> inventarioOpt = inventarioRepository.findByProducto(producto);

        if (inventarioOpt.isEmpty()) {
            throw new IllegalStateException("No hay inventario disponible para el producto: " + producto.getNombre());
        }

        Inventarios inventario = inventarioOpt.get();

        // Obtener stock disponible actual en inventario
        int stockDisponiblePz = inventario.getCantidadPz() != null ? inventario.getCantidadPz() : 0;
        int stockDisponibleKg = inventario.getCantidadKg() != null ? inventario.getCantidadKg() : 0;

        // Validar si hay suficiente stock para la cantidad solicitada
        if (cantidadPz != null && cantidadPz.intValue() > stockDisponiblePz) {
            throw new IllegalStateException(
                    String.format("Stock insuficiente en Piezas. Disponible: %d pz, Solicitado: %d pz",
                            stockDisponiblePz, cantidadPz.intValue()));
        }

        if (cantidadKg != null && cantidadKg.intValue() > stockDisponibleKg) {
            throw new IllegalStateException(
                    String.format("Stock insuficiente en Kilogramos. Disponible: %d kg, Solicitado: %d kg",
                            stockDisponibleKg, cantidadKg.intValue()));
        }
    }

    private Optional<OrdenesWorkspace> buscarOrdenExistente(String workspaceId, String productoId) {
        // Buscar entre todas las órdenes del workspace
        List<OrdenesWorkspace> ordenes = ordenesWorkspaceRepository.findByWorkspaceId(workspaceId);
        return ordenes.stream()
                .filter(orden -> orden.getProducto().getId().equals(productoId))
                .findFirst();
    }

    private OrdenesWorkspaceDTO convertirADTO(OrdenesWorkspace orden) {
        OrdenesWorkspaceDTO dto = new OrdenesWorkspaceDTO();
        dto.setId(orden.getId());
        dto.setCantidadPz(orden.getCantidadPz());
        dto.setCantidadKg(orden.getCantidadKg());

        // Mapear workspace
        dto.setWorkspaceId(orden.getWorkspace().getId());
        dto.setWorkspaceNombre(orden.getWorkspace().getNombre());

        // Mapear producto
        dto.setProductoId(orden.getProducto().getId());
        dto.setProductoNombre(orden.getProducto().getNombre());

        // Mapear precio
        dto.setHistorialPrecioId(orden.getHistorialPrecio().getId());
        dto.setPrecio(orden.getHistorialPrecio().getPrecio());

        return dto;
    }

    /**
     * Decrementa el inventario para reflejar que los productos están "reservados"
     * en una orden.
     * Este método se llama cuando se agregan productos al carrito del workspace.
     */
    private void decrementarInventario(Productos producto, BigDecimal cantidadPz, BigDecimal cantidadKg) {
        Optional<Inventarios> inventarioOpt = inventarioRepository.findByProducto(producto);

        if (inventarioOpt.isEmpty()) {
            throw new IllegalStateException("No hay inventario disponible para el producto: " + producto.getNombre());
        }

        Inventarios inventario = inventarioOpt.get();

        // Decrementar las cantidades del inventario
        if (cantidadPz != null && cantidadPz.compareTo(BigDecimal.ZERO) > 0) {
            int stockActualPz = inventario.getCantidadPz() != null ? inventario.getCantidadPz() : 0;
            inventario.setCantidadPz(stockActualPz - cantidadPz.intValue());
        }

        if (cantidadKg != null && cantidadKg.compareTo(BigDecimal.ZERO) > 0) {
            int stockActualKg = inventario.getCantidadKg() != null ? inventario.getCantidadKg() : 0;
            inventario.setCantidadKg(stockActualKg - cantidadKg.intValue());
        }

        inventarioRepository.save(inventario);
    }

    /**
     * Restaura el inventario cuando se cancelan órdenes o se limpian workspace.
     * Este método devuelve el stock al inventario disponible.
     */
    private void restaurarInventario(Productos producto, BigDecimal cantidadPz, BigDecimal cantidadKg) {
        Optional<Inventarios> inventarioOpt = inventarioRepository.findByProducto(producto);

        if (inventarioOpt.isEmpty()) {
            // Si no existe inventario, lo creamos con las cantidades restauradas
            Inventarios inventario = new Inventarios();
            inventario.setProducto(producto);
            inventario.setCantidadPz(cantidadPz != null ? cantidadPz.intValue() : 0);
            inventario.setCantidadKg(cantidadKg != null ? cantidadKg.intValue() : 0);
            inventarioRepository.save(inventario);
            return;
        }

        Inventarios inventario = inventarioOpt.get();

        // Incrementar las cantidades del inventario (restaurar)
        if (cantidadPz != null && cantidadPz.compareTo(BigDecimal.ZERO) > 0) {
            int stockActualPz = inventario.getCantidadPz() != null ? inventario.getCantidadPz() : 0;
            inventario.setCantidadPz(stockActualPz + cantidadPz.intValue());
        }

        if (cantidadKg != null && cantidadKg.compareTo(BigDecimal.ZERO) > 0) {
            int stockActualKg = inventario.getCantidadKg() != null ? inventario.getCantidadKg() : 0;
            inventario.setCantidadKg(stockActualKg + cantidadKg.intValue());
        }

        inventarioRepository.save(inventario);
    }
}
