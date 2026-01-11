import React, { useState, useEffect } from 'react';
import { inventarioService } from '../services/inventarioService';
import { workspaceService } from '../services/apiService';
import { useToast } from '../hooks/useToast';
import type { ProductoDTO, CategoriaDTO } from '../services/inventarioService';
import type { ItemCarrito } from '../types';
import axios from 'axios';
import SidebarNavigation from './SidebarNavigation';
import './PuntoDeVenta.css';

interface PuntoDeVentaProps {
  workspaceId: string;
  onBackToWorkspaces: () => void;
}

const PuntoDeVenta: React.FC<PuntoDeVentaProps> = ({ 
  workspaceId, 
  onBackToWorkspaces
}) => {
  // Hook para notificaciones toast
  const toast = useToast();

  // Verificar que workspaceId sea válido, usar Mesa 1 como fallback para desarrollo
  const workspaceIdFinal = workspaceId && workspaceId.trim() !== '' 
    ? workspaceId 
    : 'b63c0e93-62a7-483b-82dc-4e2e9430e7af'; // Mesa 1 por defecto

  // Estados principales
  const [productos, setProductos] = useState<ProductoDTO[]>([]);
  const [categorias, setCategorias] = useState<CategoriaDTO[]>([]);
  const [carrito, setCarrito] = useState<ItemCarrito[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false); // Estado separado para operaciones de guardado
  const [ordenGuardada, setOrdenGuardada] = useState(false); // ✅ NUEVO: Controlar si la orden actual está guardada
  const [error, setError] = useState<string | null>(null);
  const [workspaceName, setWorkspaceName] = useState<string>('');

  // Estados para filtros y selección
  const [categoriaSeleccionada, setCategoriaSeleccionada] = useState<string>('');
  const [productoSeleccionado, setProductoSeleccionado] = useState<string>('');
  const [cantidad, setCantidad] = useState<number>(1);

  // Estados para productos filtrados
  const [productosFiltrados, setProductosFiltrados] = useState<ProductoDTO[]>([]);

  // Cargar datos iniciales
  useEffect(() => {
    const cargarDatos = async () => {
      try {
        setIsLoading(true);
        console.log('🔄 Iniciando carga POS para workspace:', workspaceIdFinal);
        
        // Cargar productos activos con stock usando el nuevo servicio
        console.log('📦 Cargando productos con stock...');
        const productosConStock = await inventarioService.getProductosConStock();
        console.log('✅ Productos cargados:', productosConStock.length, 'productos');
        
        // Cargar categorías
        console.log('🏷️ Cargando categorías...');
        const categoriasData = await inventarioService.getAllCategorias();
        console.log('✅ Categorías cargadas:', categoriasData.length, 'categorías');

        // Cargar información del workspace
        console.log('🏢 Cargando información del workspace...');
        const workspaceData = await workspaceService.getById(workspaceIdFinal);
        console.log('✅ Workspace cargado:', workspaceData.nombre);

        // Actualizar estados con datos básicos primero
        setProductos(productosConStock);
        setCategorias(categoriasData);
        setProductosFiltrados(productosConStock);
        setWorkspaceName(workspaceData.nombre);

        // Cargar órdenes workspace existentes (con fallback)
        let carritoInicial: ItemCarrito[] = [];
        try {
          console.log('📋 Cargando órdenes existentes para workspace:', workspaceIdFinal);
          const ordenesExistentes = await inventarioService.getOrdenesWorkspace(workspaceIdFinal);
          console.log('✅ Órdenes cargadas:', ordenesExistentes.length, 'órdenes');
          
          // Convertir órdenes workspace existentes a items de carrito
          carritoInicial = ordenesExistentes.map(orden => ({
            productoId: orden.productoId,
            productoNombre: orden.productoNombre,
            precio: orden.precio,
            cantidadPz: orden.cantidadPz,
            cantidadKg: orden.cantidadKg,
            stockDisponiblePz: productosConStock.find(p => p.id === orden.productoId)?.cantidadInventario || 0,
            stockDisponibleKg: 0
          }));
          
        } catch (ordenesError: unknown) {
          console.warn('⚠️ No se pudieron cargar órdenes existentes:', ordenesError);
          console.warn('⚠️ Continuando con carrito vacío...');
          // No es un error crítico, continuar con carrito vacío
          carritoInicial = [];
        }
        
        setCarrito(carritoInicial);
        setOrdenGuardada(carritoInicial.length === 0); // ✅ Si carrito está vacío, no hay nada que guardar. Si hay productos, asumir que necesitan guardarse
        setError(null);
        console.log('🎉 Carga completa exitosa');
        
      } catch (error: unknown) {
        console.error('❌ Error detallado al cargar POS:', error);
        console.error('❌ Error response:', axios.isAxiosError(error) && error.response);
        console.error('❌ Error status:', axios.isAxiosError(error) && error.response?.status);
        console.error('❌ Error data:', axios.isAxiosError(error) && error.response?.data);
        
        // Manejo de errores más específico
        if (axios.isAxiosError(error) && error.response?.status === 404) {
          setError('Workspace no encontrado o algunos datos no están disponibles');
        } else if (axios.isAxiosError(error) && error.response?.status === 500) {
          setError('Error del servidor. Por favor intente nuevamente');
        } else if (error instanceof Error ? error.message : String(error)?.includes('Network Error')) {
          setError('Error de conexión. Verifique que el backend esté ejecutándose');
        } else {
          setError(`Error al cargar los datos del punto de venta: ${error instanceof Error ? error instanceof Error ? error.message : String(error) : 'Error desconocido'}`);
        }
      } finally {
        setIsLoading(false);
      }
    };

    cargarDatos();
  }, [workspaceIdFinal]);

  // Filtrar productos por categoría
  useEffect(() => {
    if (categoriaSeleccionada === '') {
      setProductosFiltrados(productos);
    } else {
      const filtrados = productos.filter(
        producto => producto.categoriasProductosId === categoriaSeleccionada
      );
      setProductosFiltrados(filtrados);
    }
    // Resetear producto seleccionado cuando cambia la categoría
    setProductoSeleccionado('');
    setCantidad(1);
  }, [categoriaSeleccionada, productos]);

  // Manejar selección de categoría
  const handleCategoriaSelect = (categoriaId: string) => {
    setCategoriaSeleccionada(categoriaId === categoriaSeleccionada ? '' : categoriaId);
  };

  // Manejar selección de producto
  const handleProductoSelect = (productoId: string) => {
    setProductoSeleccionado(productoId === productoSeleccionado ? '' : productoId);
    setCantidad(1);
  };

  // Obtener producto seleccionado
  const getProductoSeleccionado = () => {
    return productos.find(p => p.id === productoSeleccionado);
  };

  // Recargar datos del workspace
  const recargarDatos = async () => {
    try {
      // Recargar productos con stock actualizado
      const productosConStock = await inventarioService.getProductosConStock();
      setProductos(productosConStock);
      setProductosFiltrados(
        categoriaSeleccionada === '' 
          ? productosConStock 
          : productosConStock.filter(p => p.categoriasProductosId === categoriaSeleccionada)
      );

      // Recargar órdenes workspace existentes
      const ordenesExistentes = await inventarioService.getOrdenesWorkspace(workspaceIdFinal);
      
      // Actualizar carrito con datos frescos
      const carritoActualizado: ItemCarrito[] = ordenesExistentes.map(orden => ({
        productoId: orden.productoId,
        productoNombre: orden.productoNombre,
        precio: orden.precio,
        cantidadPz: orden.cantidadPz,
        cantidadKg: orden.cantidadKg,
        stockDisponiblePz: productosConStock.find(p => p.id === orden.productoId)?.cantidadInventario || 0,
        stockDisponibleKg: 0
      }));
      
      setCarrito(carritoActualizado);
      
    } catch (error) {
      console.error('Error al recargar datos:', error);
    }
  };

  // Agregar producto al carrito (mejorado con validaciones)
  const agregarAlCarrito = async () => {
    const producto = getProductoSeleccionado();
    if (!producto) return;

    try {
      // Verificar stock disponible en tiempo real
      const stockValido = await inventarioService.verificarStock(producto.id, cantidad);
      if (!stockValido) {
        toast.showError(`❌ STOCK INSUFICIENTE

Verifique la disponibilidad actual del producto.

👆 HAZ CLIC AQUÍ PARA CERRAR`);
        await recargarDatos(); // Recargar datos para mostrar stock actualizado
        return;
      }

      const stockDisponible = producto.cantidadInventario || 0;
      
      if (cantidad > stockDisponible) {
        toast.showError(`❌ STOCK INSUFICIENTE

Disponible: ${stockDisponible} unidades

👆 HAZ CLIC AQUÍ PARA CERRAR`);
        return;
      }

      // Buscar si el producto ya está en el carrito
      const itemExistente = carrito.find(item => item.productoId === producto.id);
      
      if (itemExistente) {
        // Actualizar cantidad del item existente
        const nuevaCantidad = itemExistente.cantidadPz + cantidad;
        if (nuevaCantidad > stockDisponible) {
          toast.showError(`❌ STOCK INSUFICIENTE

Ya tienes ${itemExistente.cantidadPz} en el carrito.
Máximo disponible: ${stockDisponible} unidades

👆 HAZ CLIC AQUÍ PARA CERRAR`);
          return;
        }
        
        setCarrito(carrito.map(item =>
          item.productoId === producto.id
            ? { ...item, cantidadPz: nuevaCantidad }
            : item
        ));
        setOrdenGuardada(false); // ✅ Marcar que la orden necesita guardarse nuevamente
      } else {
        // Agregar nuevo item al carrito
        const nuevoItem: ItemCarrito = {
          productoId: producto.id,
          productoNombre: producto.nombre,
          precio: producto.precioVentaActual || 0,
          cantidadPz: cantidad,
          cantidadKg: 0,
          stockDisponiblePz: stockDisponible,
          stockDisponibleKg: 0
        };
        
        setCarrito([...carrito, nuevoItem]);
        setOrdenGuardada(false); // ✅ Marcar que la orden necesita guardarse nuevamente
      }

      // Resetear selección
      setProductoSeleccionado('');
      setCantidad(1);
      
    } catch (error) {
      console.error('Error al agregar producto al carrito:', error);
      toast.showError(`❌ ERROR

No se pudo agregar el producto al carrito. Intente nuevamente.

👆 HAZ CLIC AQUÍ PARA CERRAR`);
    }
  };

  // Remover producto del carrito
  const removerDelCarrito = (productoId: string) => {
    setCarrito(carrito.filter(item => item.productoId !== productoId));
    setOrdenGuardada(false); // ✅ Marcar que la orden necesita guardarse nuevamente
  };

  // Calcular total del carrito
  const calcularTotal = () => {
    return carrito.reduce((total, item) => {
      return total + (item.precio * item.cantidadPz);
    }, 0);
  };

  // Guardar orden en workspace
  const guardarOrden = async () => {
    if (carrito.length === 0) {
      toast.showWarning(`⚠️ CARRITO VACÍO

Agregue productos antes de guardar la orden.

👆 HAZ CLIC AQUÍ PARA CERRAR`);
      return;
    }

    try {
      setIsSaving(true);
      
      // Limpiar órdenes workspace existentes antes de agregar nuevas
      await inventarioService.limpiarOrdenesWorkspace(workspaceIdFinal);
      
      // Agregar cada producto del carrito a ordenes_workspace
      for (const item of carrito) {
        await inventarioService.agregarProductoOrden(
          workspaceIdFinal,
          item.productoId,
          item.cantidadPz,
          item.cantidadKg
        );
      }
      
      // Mostrar confirmación
      toast.showSuccess(`✅ ORDEN GUARDADA EXITOSAMENTE

🏪 Mesa: ${workspaceName || workspaceIdFinal}
📦 Productos: ${carrito.length}
💰 Total: $${calcularTotal().toFixed(2)}

La orden se ha registrado correctamente en el sistema.

👆 HAZ CLIC AQUÍ PARA CERRAR`);
      
      // ✅ Marcar la orden como guardada
      setOrdenGuardada(true);
      
      console.log('✅ Orden guardada en workspace:', {
        workspaceId: workspaceIdFinal,
        productos: carrito.length,
        total: calcularTotal()
      });
      
      // ✅ ESPERAR 3 segundos antes de recargar para que el toast sea visible
      setTimeout(async () => {
        await recargarDatos();
      }, 3000);
      
    } catch (error) {
      console.error('❌ Error al guardar orden:', error);
      toast.showError(`❌ ERROR AL GUARDAR

No se pudo guardar la orden. Intente nuevamente.

👆 HAZ CLIC AQUÍ PARA CERRAR`);
    } finally {
      setIsSaving(false);
    }
  };

  // Solicitar cuenta (procesar venta final)
  const solicitarCuenta = async () => {
    if (carrito.length === 0) {
      toast.showWarning(`⚠️ CARRITO VACÍO

Agregue productos antes de solicitar la cuenta.

👆 HAZ CLIC AQUÍ PARA CERRAR`);
      return;
    }

    // ✅ NUEVA VALIDACIÓN: Verificar que la orden esté guardada antes de solicitar cuenta
    if (!ordenGuardada) {
      toast.showWarning(`⚠️ ORDEN NO GUARDADA

Debe guardar la orden antes de solicitar la cuenta.
Presione "Guardar Orden" primero.

👆 HAZ CLIC AQUÍ PARA CERRAR`);
      return;
    }

    try {
      setIsSaving(true);
      
      // Solo cambiar estado del workspace a "cuenta" - NO volver a guardar
      console.log('📋 Solicitando cuenta para workspace:', workspaceIdFinal);
      await workspaceService.cambiarSolicitudCuenta(workspaceIdFinal, true);
      
      toast.showSuccess(`✅ CUENTA SOLICITADA EXITOSAMENTE

El administrador puede proceder a generar el ticket de venta.

👆 HAZ CLIC AQUÍ PARA CERRAR`);
      
      // ✅ ESPERAR 4 segundos antes de redirigir para que el toast sea visible
      setTimeout(() => {
        onBackToWorkspaces();
      }, 4000);
      
    } catch (error) {
      console.error('❌ Error al solicitar cuenta:', error);
      toast.showError(`❌ ERROR AL SOLICITAR CUENTA

No se pudo procesar la solicitud. Intente nuevamente.

👆 HAZ CLIC AQUÍ PARA CERRAR`);
    } finally {
      setIsSaving(false);
    }
  };

  if (isLoading) {
    return (
      <div className="punto-venta__loading">
        <div className="loading-spinner"></div>
        <p>Cargando punto de venta...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="punto-venta__error">
        <p>{error}</p>
        <button onClick={onBackToWorkspaces} className="btn btn-primary">
          Volver a Workspaces
        </button>
      </div>
    );
  }

  return (
    <div className="punto-venta">
      <SidebarNavigation />
      <div className="punto-venta-content">
        {/* Header */}
        <header className="punto-venta__header">
          <div className="header-content">
            <div className="header-left">
              <button onClick={onBackToWorkspaces} className="btn btn-back">
                ← Regresar
              </button>
              <h1>Punto de Venta - {workspaceName || `Mesa ${workspaceIdFinal}`}</h1>
            </div>
          </div>
        </header>

        {/* Main Content */}
        <main className="punto-venta__main">
          {/* Panel Izquierdo - Menú de Productos */}
          <div className="panel-productos">
            <div className="panel-header">
              <h2>Menú de Productos</h2>
            </div>

            {/* Filtros de Categorías */}
            <div className="categorias-filter">
              <button
                onClick={() => handleCategoriaSelect('')}
                className={`categoria-btn ${categoriaSeleccionada === '' ? 'active' : ''}`}
              >
                Todas
              </button>
              {categorias.map(categoria => (
                <button
                  key={categoria.id}
                  onClick={() => handleCategoriaSelect(categoria.id)}
                  className={`categoria-btn ${categoriaSeleccionada === categoria.id ? 'active' : ''}`}
                >
                  {categoria.categoria}
                </button>
              ))}
            </div>

            {/* Lista de Productos */}
            <div className="productos-lista">
              {productosFiltrados.map(producto => (
                <div
                  key={producto.id}
                  onClick={() => handleProductoSelect(producto.id)}
                  className={`producto-item ${productoSeleccionado === producto.id ? 'selected' : ''}`}
                >
                  <div className="producto-info">
                    <h3>{producto.nombre}</h3>
                    <p className="stock">Stock: {producto.cantidadInventario || 0}</p>
                    <p className="precio">${producto.precioVentaActual || 0}</p>
                  </div>
                </div>
              ))}
            </div>

            {/* Controles de Cantidad */}
            {productoSeleccionado && (
              <div className="cantidad-controls">
                <h3>Cantidad</h3>
                <div className="cantidad-input">
                  <button
                    onClick={() => setCantidad(Math.max(1, cantidad - 1))}
                    className="btn btn-sm"
                  >
                    -
                  </button>
                  <input
                    type="number"
                    value={cantidad}
                    onChange={(e) => setCantidad(Math.max(1, parseInt(e.target.value) || 1))}
                    min="1"
                    max={getProductoSeleccionado()?.cantidadInventario || 1}
                  />
                  <button
                    onClick={() => {
                      const maxStock = getProductoSeleccionado()?.cantidadInventario || 1;
                      setCantidad(Math.min(maxStock, cantidad + 1));
                    }}
                    className="btn btn-sm"
                  >
                    +
                  </button>
                </div>
                <button onClick={agregarAlCarrito} className="btn btn-primary btn-block">
                  Agregar al Carrito
                </button>
              </div>
            )}
          </div>

          {/* Panel Derecho - Carrito de Compras */}
          <div className="panel-carrito">
            <div className="panel-header">
              <h2>Carrito de Compras</h2>
            </div>

            {/* Lista del Carrito */}
            <div className="carrito-lista">
              {carrito.length === 0 ? (
                <div className="carrito-vacio">
                  <p>El carrito está vacío</p>
                  <p>Selecciona productos del menú para agregar</p>
                </div>
              ) : (
                carrito.map(item => (
                  <div key={item.productoId} className="carrito-item">
                    <div className="item-info">
                      <h4>{item.productoNombre}</h4>
                      <p className="cantidad">{item.cantidadPz}x ${item.precio}</p>
                      <p className="subtotal">${(item.precio * item.cantidadPz).toFixed(2)}</p>
                    </div>
                    <button
                      onClick={() => removerDelCarrito(item.productoId)}
                      className="btn btn-danger btn-sm"
                    >
                      ×
                    </button>
                  </div>
                ))
              )}
            </div>

            {/* Total y Acciones */}
            <div className="carrito-footer">
              <div className="total">
                <h3>Total: ${calcularTotal().toFixed(2)}</h3>
              </div>
              <div className="acciones">
                <button
                  onClick={guardarOrden}
                  disabled={carrito.length === 0 || isSaving}
                  className="btn btn-success btn-block"
                >
                  {isSaving ? 'Guardando...' : 'Guardar Orden'}
                </button>
                <button
                  onClick={solicitarCuenta}
                  disabled={carrito.length === 0 || isSaving || !ordenGuardada}
                  className={`btn btn-block ${!ordenGuardada ? 'btn-secondary' : 'btn-primary'}`}
                  title={!ordenGuardada ? 'Debe guardar la orden primero' : 'Solicitar cuenta para proceder al pago'}
                >
                  {isSaving ? 'Procesando...' : !ordenGuardada ? 'Guardar Orden Primero' : 'Solicitar Cuenta'}
                </button>
              </div>
            </div>
          </div>
        </main>
        
        {/* Overlay sutil para operaciones de guardado - permite que los toasts sean visibles */}
        {isSaving && (
          <div className="saving-overlay">
            <div className="saving-indicator">
              <div className="loading-spinner"></div>
              <p>Procesando operación...</p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default PuntoDeVenta;
