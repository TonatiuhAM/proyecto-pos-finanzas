import React, { useState, useEffect, useMemo } from 'react';
import { inventarioService } from '../services/inventarioService';
import { workspaceService } from '../services/apiService';
import { useToast } from '../hooks/useToast';
import type { ProductoDTO, CategoriaDTO } from '../services/inventarioService';
import type { ItemCarrito } from '../types';
import axios from 'axios';
import SidebarNavigation from './SidebarNavigation';
import { useAuth } from '../hooks/useAuth';
import { 
  Search,
  Plus, 
  Minus, 
  Trash2, 
  Save, 
  Send,
  UtensilsCrossed,
  Coffee,
  IceCream,
  Beer,
  Package,
  LayoutGrid,
  ShoppingBag,
  ArrowRight
} from 'lucide-react';
import './PuntoDeVenta.css';

interface PuntoDeVentaProps {
  workspaceId: string;
  onBackToWorkspaces: () => void;
  onNavigate?: (section: string) => void;
}

const PuntoDeVenta: React.FC<PuntoDeVentaProps> = ({ 
  workspaceId, 
  onBackToWorkspaces,
  onNavigate
}) => {
  const toast = useToast();
  const { logout } = useAuth();

  // Verificar que workspaceId sea válido, usar Mesa 1 como fallback para desarrollo
  const workspaceIdFinal = workspaceId && workspaceId.trim() !== '' 
    ? workspaceId 
    : 'b63c0e93-62a7-483b-82dc-4e2e9430e7af'; // Mesa 1 por defecto

  // Estados principales
  const [productos, setProductos] = useState<ProductoDTO[]>([]);
  const [filteredProductos, setFilteredProductos] = useState<ProductoDTO[]>([]);
  const [categorias, setCategorias] = useState<CategoriaDTO[]>([]);
  const [carrito, setCarrito] = useState<ItemCarrito[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [ordenGuardada, setOrdenGuardada] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [workspaceName, setWorkspaceName] = useState<string>('');

  // Estados para filtros y búsqueda
  const [selectedCategory, setSelectedCategory] = useState<string>('all');
  const [searchQuery, setSearchQuery] = useState<string>('');
  
  // Estado para toggle móvil
  const [showMobileCart, setShowMobileCart] = useState(false);

  // Función para obtener icono de producto según categoría
  const getProductIcon = (categoria: string) => {
    const categoryName = categoria?.toLowerCase() || '';
    
    if (categoryName.includes('taco') || categoryName.includes('quesadilla') || categoryName.includes('torta')) {
      return '🌮';
    }
    if (categoryName.includes('bebida') || categoryName.includes('refresco') || categoryName.includes('agua') || categoryName.includes('jugo')) {
      return '🥤';
    }
    if (categoryName.includes('postre') || categoryName.includes('helado') || categoryName.includes('dulce')) {
      return '🍦';
    }
    if (categoryName.includes('cerveza') || categoryName.includes('alcohol') || categoryName.includes('vino')) {
      return '🍺';
    }
    
    // Default para productos sin categoría específica
    return '🍽️';
  };

  // Mapear categorías del backend a formato de UI
  const categoriasConTodo = useMemo(() => {
    const categoriasBackend = categorias.map(cat => ({
      id: cat.id,
      name: cat.categoria,
      icon: cat.categoria?.toLowerCase().includes('taco') ? <UtensilsCrossed className="w-4 h-4" /> :
            cat.categoria?.toLowerCase().includes('bebida') ? <Coffee className="w-4 h-4" /> :
            cat.categoria?.toLowerCase().includes('postre') ? <IceCream className="w-4 h-4" /> :
            cat.categoria?.toLowerCase().includes('cerveza') ? <Beer className="w-4 h-4" /> :
            <Package className="w-4 h-4" />
    }));

    return [
      { id: 'all', name: 'Todo', icon: <LayoutGrid className="w-4 h-4" /> },
      ...categoriasBackend
    ];
  }, [categorias]);

  // Filtrado de productos con búsqueda y categoría
  useEffect(() => {
    let filtered = productos;
    
    // Filtrar por categoría
    if (selectedCategory !== 'all') {
      filtered = filtered.filter(producto => 
        producto.categoriasProductosId === selectedCategory
      );
    }
    
    // Filtrar por búsqueda
    if (searchQuery.trim()) {
      filtered = filtered.filter(producto =>
        producto.nombre.toLowerCase().includes(searchQuery.toLowerCase())
      );
    }
    
    setFilteredProductos(filtered);
  }, [productos, selectedCategory, searchQuery]);

  // Obtener contadores para filtros
  const getFilterCounts = () => {
    const all = productos.length;
    const tacos = productos.filter(p => categorias.find(c => c.id === p.categoriasProductosId)?.categoria.toLowerCase().includes('taco')).length;
    const bebidas = productos.filter(p => categorias.find(c => c.id === p.categoriasProductosId)?.categoria.toLowerCase().includes('bebida')).length;
    const postres = productos.filter(p => categorias.find(c => c.id === p.categoriasProductosId)?.categoria.toLowerCase().includes('postre')).length;
    
    return {
      all,
      tacos,
      bebidas,
      postres
    };
  };

  const filterCounts = getFilterCounts();

  // --- LÓGICA DEL CARRITO ---
  const addToCart = async (producto: ProductoDTO) => {
    try {
      const cantidad = 1;
      
      // Verificar stock disponible en tiempo real
      const stockValido = await inventarioService.verificarStock(producto.id, cantidad);
      if (!stockValido) {
        toast.showError(`❌ STOCK INSUFICIENTE
Verifique la disponibilidad actual del producto.
👆 HAZ CLIC AQUÍ PARA CERRAR`);
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
        setOrdenGuardada(false);
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
        setOrdenGuardada(false);
      }
      
    } catch (error) {
      console.error('Error al agregar producto al carrito:', error);
      toast.showError(`❌ ERROR
No se pudo agregar el producto al carrito. Intente nuevamente.
👆 HAZ CLIC AQUÍ PARA CERRAR`);
    }
  };

  const updateQuantity = (productoId: string, delta: number) => {
    setCarrito((prev) =>
      prev.map((item) => {
        if (item.productoId === productoId) {
          const nuevaCantidad = Math.max(0, item.cantidadPz + delta);
          return { ...item, cantidadPz: nuevaCantidad };
        }
        return item;
      }).filter((item) => item.cantidadPz > 0)
    );
    setOrdenGuardada(false);
  };

  const removeFromCart = (productoId: string) => {
    setCarrito(carrito.filter(item => item.productoId !== productoId));
    setOrdenGuardada(false);
  };

  // Cálculos de totales
  const subtotal = carrito.reduce((acc, item) => acc + item.precio * item.cantidadPz, 0);
  const tax = subtotal * 0.16; // IVA ejemplo 16%
  const total = subtotal + tax;

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
      
      toast.showSuccess(`✅ ORDEN GUARDADA EXITOSAMENTE
🏪 Mesa: ${workspaceName || workspaceIdFinal}
📦 Productos: ${carrito.length}
💰 Total: $${total.toFixed(2)}

La orden se ha registrado correctamente en el sistema.

👆 HAZ CLIC AQUÍ PARA CERRAR`);
      
      setOrdenGuardada(true);
      
      // ✅ ESPERAR 3 segundos antes de redirigir para que el toast sea visible
      setTimeout(() => {
        onBackToWorkspaces();
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

    if (!ordenGuardada) {
      toast.showWarning(`⚠️ ORDEN NO GUARDADA
Debe guardar la orden antes de solicitar la cuenta.
Presione "Guardar" primero.
👆 HAZ CLIC AQUÍ PARA CERRAR`);
      return;
    }

    try {
      setIsSaving(true);
      
      await workspaceService.cambiarSolicitudCuenta(workspaceIdFinal, true);
      
      toast.showSuccess(`✅ CUENTA SOLICITADA EXITOSAMENTE
El administrador puede proceder a generar el ticket de venta.
👆 HAZ CLIC AQUÍ PARA CERRAR`);
      
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

  // Cargar datos iniciales
  const loadData = async () => {
    try {
      setIsLoading(true);
      console.log('🔄 Iniciando carga POS para workspace:', workspaceIdFinal);
      
      // Cargar productos activos con stock
      const productosConStock = await inventarioService.getProductosConStock();
      
      // Cargar categorías
      const categoriasData = await inventarioService.getAllCategorias();

      // Cargar información del workspace
      const workspaceData = await workspaceService.getById(workspaceIdFinal);

      setProductos(productosConStock);
      setCategorias(categoriasData);
      setWorkspaceName(workspaceData.nombre);

      // Cargar órdenes workspace existentes (con fallback)
      let carritoInicial: ItemCarrito[] = [];
      try {
        const ordenesExistentes = await inventarioService.getOrdenesWorkspace(workspaceIdFinal);
        
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
        carritoInicial = [];
      }

      setCarrito(carritoInicial);
      setOrdenGuardada(carritoInicial.length === 0);
      setError(null);
      
    } catch (error: unknown) {
      console.error('❌ Error detallado al cargar POS:', error);
      
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

  useEffect(() => {
    loadData();
  }, [workspaceIdFinal]);

  // Navegación del sidebar
  const handleSidebarNavigate = (section: string) => {
    if (onNavigate) {
      onNavigate(section);
    } else {
      console.warn('No hay función de navegación disponible para la sección:', section);
    }
  };

  if (isLoading) {
    return (
      <div className="modern-workspaces">
        <SidebarNavigation 
          activeSection="workspaces"
          onNavigate={handleSidebarNavigate}
          onLogout={logout}
        />
        <div className="modern-workspaces__loading">
          <div className="modern-workspaces__loading-spinner">
            <div className="modern-workspaces__loading-icon"></div>
          </div>
          <h2 className="modern-workspaces__loading-text">Cargando punto de venta...</h2>
        </div>
      </div>
    );
  }

  return (
    <div className="modern-workspaces">
      {/* Sidebar Navigation */}
      <SidebarNavigation 
        activeSection="workspaces"
        onNavigate={handleSidebarNavigate}
        onLogout={logout}
      />

      {/* Main Content */}
      <main className="modern-workspaces__main">
        {/* Header */}
        <header className="modern-workspaces__header">
          <div className="modern-workspaces__header-content">
            <div className="modern-workspaces__title-section">
              <h1 className="modern-workspaces__title">Punto de Venta - {workspaceName}</h1>
            </div>
            <div className="pos-search">
              <Search className="pos-search__icon" />
              <input 
                type="text" 
                placeholder="Buscar productos..." 
                className="pos-search__input pl-12"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>
          </div>
        </header>

        {/* Error Message */}
        {error && (
          <div className="modern-workspaces__error" role="alert">
            <div className="modern-workspaces__error-content">
              <span>{error}</span>
              <button onClick={loadData} className="modern-workspaces__retry-btn">
                Reintentar
              </button>
            </div>
          </div>
        )}

        {/* Filters */}
        <div className="pos-categories-carousel">
          {categoriasConTodo.map((cat) => (
            <button
              key={cat.id}
              onClick={() => setSelectedCategory(cat.id)}
              className={`pos-category-chip ${
                selectedCategory === cat.id ? 'pos-category-chip--active' : ''
              }`}
            >
              <span className="modern-workspaces__filter-dot"></span>
              {cat.name}
              <span className="modern-workspaces__filter-count">
                ({cat.id === 'all' ? filterCounts.all : productos.filter(p => p.categoriasProductosId === cat.id).length})
              </span>
            </button>
          ))}
        </div>

        {/* Layout con Grid de Productos y Carrito */}
        <div className="pos-layout">
          
          {/* Grid de Productos */}
          <div className="pos-products">
            {/* Empty State */}
            {filteredProductos.length === 0 && !error && (
              <div className="modern-workspaces__empty">
                <div className="modern-workspaces__empty-icon">
                  <Package className="w-16 h-16" />
                </div>
                <h2 className="modern-workspaces__empty-title">No hay productos disponibles</h2>
                <p className="modern-workspaces__empty-subtitle">
                  {selectedCategory === 'all' ? 'No se encontraron productos' : 'Cambia el filtro para ver otros productos'}
                </p>
              </div>
            )}

            {/* Products Grid */}
            {filteredProductos.length > 0 && (
              <div className="grid grid-cols-2 md:grid-cols-3 xl:grid-cols-4 gap-4 w-full overflow-y-auto p-4">
                {filteredProductos.map((producto) => (
                  <button
                    key={producto.id}
                    onClick={() => addToCart(producto)}
                    className="pos-product-card"
                  >
                    {/* Zona Superior (Header) - Indicador de Stock */}
                    <div className="pos-product-card__header">
                      <div className="pos-product-card__status-indicator"></div>
                      <span className="pos-product-card__stock">
                        Stock: {producto.cantidadInventario || 0}
                      </span>
                    </div>
                    
                    {/* Zona Central (Body) - Icono, Nombre y Precio */}
                    <div className="pos-product-card__body">
                      <div className="pos-product-card__icon">
                        {getProductIcon(categorias.find(c => c.id === producto.categoriasProductosId)?.categoria || '')}
                      </div>
                      <h3 className="pos-product-card__title">
                        {producto.nombre}
                      </h3>
                      <div className="pos-product-card__price">
                        ${(producto.precioVentaActual || 0).toFixed(2)}
                      </div>
                    </div>
                    
                    {/* Zona Inferior (Footer) - Categoría y Botón */}
                    <div className="pos-product-card__footer">
                      <span className="pos-product-card__category">
                        {categorias.find(c => c.id === producto.categoriasProductosId)?.categoria || 'Sin categoría'}
                      </span>
                      <div className="pos-product-card__add-button">
                        <Plus className="pos-product-card__add-icon" />
                      </div>
                    </div>
                  </button>
                ))}
              </div>
            )}
          </div>

          {/* Carrito Lateral */}
          <aside className={`pos-cart ${showMobileCart ? 'pos-cart--visible' : ''}`}>
            <div className="pos-cart__header">
              <h2 className="pos-cart__title">Orden Actual</h2>
              <span className="pos-cart__count">{carrito.length} productos</span>
            </div>

            <div className="pos-cart__items">
              {carrito.length === 0 ? (
                <div className="pos-cart__empty">
                  <Package className="w-16 h-16 text-gray-200" />
                  <p>La orden está vacía</p>
                  <span>Selecciona productos</span>
                </div>
              ) : (
                carrito.map((item) => (
                  <div key={item.productoId} className="pos-cart__item">
                    <div className="pos-cart__item-info">
                      <div className="pos-cart__item-icon">
                        {getProductIcon(categorias.find(c => 
                          productos.find(p => p.id === item.productoId)?.categoriasProductosId === c.id
                        )?.categoria || '')}
                      </div>
                      <div className="pos-cart__item-details">
                        <span className="pos-cart__item-name">{item.productoNombre}</span>
                        <span className="pos-cart__item-price">${(item.precio * item.cantidadPz).toFixed(2)}</span>
                      </div>
                    </div>
                    
                    <div className="pos-cart__item-controls">
                      <button 
                        onClick={(e) => { 
                          e.stopPropagation(); 
                          if (item.cantidadPz === 1) {
                            removeFromCart(item.productoId);
                          } else {
                            updateQuantity(item.productoId, -1);
                          }
                        }}
                        className="pos-cart__btn pos-cart__btn--minus"
                      >
                        {item.cantidadPz === 1 ? <Trash2 className="w-3 h-3" /> : <Minus className="w-3 h-3" />}
                      </button>
                      <span className="pos-cart__item-quantity">{item.cantidadPz}</span>
                      <button 
                        onClick={(e) => { e.stopPropagation(); updateQuantity(item.productoId, 1); }}
                        className="pos-cart__btn pos-cart__btn--plus"
                      >
                        <Plus className="w-3 h-3" />
                      </button>
                    </div>
                  </div>
                ))
              )}
            </div>

            <div className="pos-cart__footer">
              <div className="pos-cart__total">
                <div className="pos-cart__total-line">
                  <span>Total</span>
                  <span>${total.toFixed(2)}</span>
                </div>
              </div>

              <div className="pos-cart__actions">
                <button 
                  onClick={guardarOrden}
                  disabled={isSaving || carrito.length === 0}
                  className="pos-cart__btn-action pos-cart__btn-action--save"
                >
                  <Save className="w-4 h-4" />
                  <span>{isSaving ? 'Guardando...' : 'Guardar'}</span>
                </button>

                <button 
                  onClick={solicitarCuenta}
                  disabled={isSaving || carrito.length === 0 || !ordenGuardada}
                  className="pos-cart__btn-action pos-cart__btn-action--request"
                >
                  <Send className="w-4 h-4" />
                  <span>Solicitar</span>
                </button>
              </div>
            </div>
          </aside>
        </div>

        {/* BOTÓN TOGGLE MÓVIL */}
        <button
          onClick={() => setShowMobileCart(!showMobileCart)}
          className="pos-mobile-toggle"
        >
          {showMobileCart ? (
            <ArrowRight className="w-5 h-5 text-white" />
          ) : (
            <ShoppingBag className="w-5 h-5 text-white" />
          )}
        </button>
      </main>
    </div>
  );
};

export default PuntoDeVenta;