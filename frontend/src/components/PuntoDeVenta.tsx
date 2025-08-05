import React, { useState, useEffect } from 'react';
import { inventarioService } from '../services/inventarioService';
import { workspaceService } from '../services/apiService';
import type { ProductoDTO, CategoriaDTO } from '../services/inventarioService';
import type { ItemCarrito } from '../types';
import './PuntoDeVenta.css';

interface PuntoDeVentaProps {
  workspaceId: string;
  onBackToWorkspaces: () => void;
  onLogout: () => void;
}

const PuntoDeVenta: React.FC<PuntoDeVentaProps> = ({ 
  workspaceId, 
  onBackToWorkspaces, 
  onLogout 
}) => {
  // Verificar que workspaceId sea válido, usar Mesa 1 como fallback para desarrollo
  const workspaceIdFinal = workspaceId && workspaceId.trim() !== '' 
    ? workspaceId 
    : 'b63c0e93-62a7-483b-82dc-4e2e9430e7af'; // Mesa 1 por defecto

  // Estados principales
  const [productos, setProductos] = useState<ProductoDTO[]>([]);
  const [categorias, setCategorias] = useState<CategoriaDTO[]>([]);
  const [carrito, setCarrito] = useState<ItemCarrito[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

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

        // Actualizar estados con datos básicos primero
        setProductos(productosConStock);
        setCategorias(categoriasData);
        setProductosFiltrados(productosConStock);

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
          
        } catch (ordenesError: any) {
          console.warn('⚠️ No se pudieron cargar órdenes existentes:', ordenesError);
          console.warn('⚠️ Continuando con carrito vacío...');
          // No es un error crítico, continuar con carrito vacío
          carritoInicial = [];
        }
        
        setCarrito(carritoInicial);
        setError(null);
        console.log('🎉 Carga completa exitosa');
        
      } catch (error: any) {
        console.error('❌ Error detallado al cargar POS:', error);
        console.error('❌ Error response:', error.response);
        console.error('❌ Error status:', error.response?.status);
        console.error('❌ Error data:', error.response?.data);
        
        // Manejo de errores más específico
        if (error.response?.status === 404) {
          setError('Workspace no encontrado o algunos datos no están disponibles');
        } else if (error.response?.status === 500) {
          setError('Error del servidor. Por favor intente nuevamente');
        } else if (error.message?.includes('Network Error')) {
          setError('Error de conexión. Verifique que el backend esté ejecutándose');
        } else {
          setError(`Error al cargar los datos del punto de venta: ${error.message || 'Error desconocido'}`);
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
        alert(`Stock insuficiente. Verifique la disponibilidad actual del producto.`);
        await recargarDatos(); // Recargar datos para mostrar stock actualizado
        return;
      }

      const stockDisponible = producto.cantidadInventario || 0;
      
      if (cantidad > stockDisponible) {
        alert(`Stock insuficiente. Disponible: ${stockDisponible} unidades`);
        return;
      }

      // Buscar si el producto ya está en el carrito
      const itemExistente = carrito.find(item => item.productoId === producto.id);
      
      if (itemExistente) {
        // Actualizar cantidad del item existente
        const nuevaCantidad = itemExistente.cantidadPz + cantidad;
        if (nuevaCantidad > stockDisponible) {
          alert(`Stock insuficiente. Ya tienes ${itemExistente.cantidadPz} en el carrito. Máximo disponible: ${stockDisponible}`);
          return;
        }
        
        setCarrito(carrito.map(item =>
          item.productoId === producto.id
            ? { ...item, cantidadPz: nuevaCantidad }
            : item
        ));
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
      }

      // Resetear selección
      setProductoSeleccionado('');
      setCantidad(1);
      
    } catch (error) {
      console.error('Error al agregar producto al carrito:', error);
      alert('Error al agregar producto al carrito');
    }
  };

  // Remover producto del carrito
  const removerDelCarrito = (productoId: string) => {
    setCarrito(carrito.filter(item => item.productoId !== productoId));
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
      alert('El carrito está vacío');
      return;
    }

    try {
      setIsLoading(true);
      
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
      alert(`✅ Orden guardada exitosamente!\n\nWorkspace: ${workspaceIdFinal}\nProductos: ${carrito.length}\nTotal: $${calcularTotal().toFixed(2)}`);
      
      console.log('✅ Orden guardada en workspace:', {
        workspaceId: workspaceIdFinal,
        productos: carrito.length,
        total: calcularTotal()
      });
      
      // Recargar datos para mostrar stock actualizado
      await recargarDatos();
      
    } catch (error) {
      console.error('❌ Error al guardar orden:', error);
      alert('Error al guardar la orden. Por favor, intente nuevamente.');
    } finally {
      setIsLoading(false);
    }
  };

  // Solicitar cuenta (procesar venta final)
  const solicitarCuenta = async () => {
    if (carrito.length === 0) {
      alert('El carrito está vacío');
      return;
    }

    try {
      setIsLoading(true);
      
      // Primero guardar la orden actual si hay productos en el carrito
      if (carrito.length > 0) {
        console.log('💾 Guardando orden antes de solicitar cuenta...');
        await guardarOrden();
      }
      
      // Cambiar estado del workspace a "cuenta"
      console.log('📋 Solicitando cuenta para workspace:', workspaceIdFinal);
      await workspaceService.cambiarSolicitudCuenta(workspaceIdFinal, true);
      
      alert('✅ Cuenta solicitada exitosamente. El administrador puede generar el ticket de venta.');
      
      // Opcional: redirigir a workspaces para que el administrador vea el estado "cuenta"
      onBackToWorkspaces();
      
    } catch (error) {
      console.error('❌ Error al solicitar cuenta:', error);
      alert('Error al solicitar cuenta. Por favor, intente nuevamente.');
    } finally {
      setIsLoading(false);
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
      {/* Header */}
      <header className="punto-venta__header">
        <div className="header-content">
          <div className="header-left">
            <button onClick={onBackToWorkspaces} className="btn btn-secondary">
              ← Volver a Workspaces
            </button>
            <h1>Punto de Venta - Mesa {workspaceIdFinal}</h1>
          </div>
          <button onClick={onLogout} className="btn btn-danger">
            Cerrar Sesión
          </button>
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
                disabled={carrito.length === 0 || isLoading}
                className="btn btn-success btn-block"
              >
                {isLoading ? 'Guardando...' : 'Guardar Orden'}
              </button>
              <button
                onClick={solicitarCuenta}
                disabled={carrito.length === 0 || isLoading}
                className="btn btn-primary btn-block"
              >
                {isLoading ? 'Procesando...' : 'Solicitar Cuenta'}
              </button>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};

export default PuntoDeVenta;
