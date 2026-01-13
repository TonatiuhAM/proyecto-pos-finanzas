import React, { useState, useEffect, useCallback } from 'react';
import { 
  Edit3,
  Trash2
} from 'lucide-react';
import SidebarNavigation from './SidebarNavigation';
import { inventarioService } from '../services/inventarioService';
import { stockService } from '../services/stockService';
import { useToast } from '../hooks/useToast';
import ModalCrearProducto from './ModalCrearProducto';
import ModalEditarProducto from './ModalEditarProducto';
import ModalPredicciones from './ModalPredicciones';
import type { ProductoDTO } from '../services/inventarioService';
import type { ProductoStockBajo } from '../types/index';

interface InventarioProps {
  onNavigateToCompras?: () => void;
  onNavigate?: (section: string) => void;
}

const Inventario: React.FC<InventarioProps> = ({ onNavigateToCompras, onNavigate }) => {
  // Estados principales
  const [productos, setProductos] = useState<ProductoDTO[]>([]);
  const [filteredProductos, setFilteredProductos] = useState<ProductoDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');

  // Estados de los modales
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showPredictionsModal, setShowPredictionsModal] = useState(false);
  const [selectedProduct, setSelectedProduct] = useState<ProductoDTO | null>(null);

  // Estados para alertas de stock
  const [stockVerificado, setStockVerificado] = useState(false);

  // Hook de toasts
  const toast = useToast();

  // Cargar datos iniciales
  useEffect(() => {
    loadProductos();
  }, []);

  // Filtrar productos en base a búsqueda
  useEffect(() => {
    if (!searchQuery.trim()) {
      setFilteredProductos(productos);
    } else {
      const query = searchQuery.toLowerCase();
      const filtered = productos.filter(producto =>
        producto.nombre.toLowerCase().includes(query) ||
        producto.categoriasProductosCategoria?.toLowerCase().includes(query) ||
        producto.proveedorNombre?.toLowerCase().includes(query)
      );
      setFilteredProductos(filtered);
    }
  }, [productos, searchQuery]);

  const loadProductos = async () => {
    try {
      setLoading(true);
      const productosData = await inventarioService.getAllProductos();
      const productosActivos = productosData.filter(producto => 
        producto.estadosEstado?.toLowerCase() === 'activo'
      );
      setProductos(productosActivos);
      verificarStockBajo(productosActivos);
    } catch (err) {
      console.error('Error loading productos:', err);
      toast.showError('Error al cargar los productos');
    } finally {
      setLoading(false);
    }
  };

  const verificarStockBajo = useCallback((productosParaVerificar?: ProductoDTO[]) => {
    const productosActuales = productosParaVerificar || productos;
    
    if (productosActuales.length === 0 || stockVerificado) {
      return;
    }

    const resultado = stockService.verificarStockBajo(productosActuales);

    if (resultado.tieneProductosBajos) {
      const productosParaAlertar = stockService.obtenerProductosParaAlertar(resultado.productosBajos);
      
      if (productosParaAlertar.length > 0) {
        mostrarAlertasStock(productosParaAlertar);
      }
    }

    setStockVerificado(true);
  }, [productos, stockVerificado, toast]);

  const mostrarAlertasStock = (productosConStockBajo: ProductoStockBajo[]) => {
    const tipoAlerta = stockService.determinarTipoAlerta(productosConStockBajo);
    
    if (tipoAlerta === 'agrupada') {
      toast.showMultipleStockWarning(
        productosConStockBajo.map(p => ({ nombre: p.nombre, cantidad: p.cantidadActual }))
      );
    } else {
      productosConStockBajo.forEach((producto, index) => {
        setTimeout(() => {
          toast.showStockWarning(producto.nombre, producto.cantidadActual);
        }, index * 300);
      });
    }
  };

  const handleCrearNuevo = () => {
    setShowCreateModal(true);
  };

  const handleComprarProducto = () => {
    if (onNavigateToCompras) {
      onNavigateToCompras();
    }
  };

  const handleEditarProducto = (producto: ProductoDTO) => {
    setSelectedProduct(producto);
    setShowEditModal(true);
  };

  const handleEliminarProducto = async (id: string, nombre: string) => {
    if (window.confirm(`¿Estás seguro de que deseas eliminar el producto "${nombre}"?`)) {
      try {
        await inventarioService.desactivarProducto(id);
        await loadProductos();
        toast.showSuccess('Producto eliminado correctamente');
      } catch (err) {
        console.error('Error deleting product:', err);
        toast.showError('Error al eliminar el producto');
      }
    }
  };

  const handleModalSuccess = () => {
    setStockVerificado(false);
    loadProductos();
  };

  const handleShowPredictions = () => {
    setShowPredictionsModal(true);
  };

  const handleCreatePurchaseOrder = (productosSeleccionados: any[]) => {
    alert(`Orden de compra creada para ${productosSeleccionados.length} productos`);
    
    if (onNavigateToCompras) {
      onNavigateToCompras();
    }
  };

  const formatPrice = (price: number | undefined) => {
    if (price === undefined || price === null) return 'N/A';
    return new Intl.NumberFormat('es-MX', {
      style: 'currency',
      currency: 'MXN'
    }).format(price);
  };

  const getStatusBadge = (cantidadInventario: number | undefined, estadosEstado: string | undefined) => {
    const cantidad = cantidadInventario || 0;
    
    if (estadosEstado?.toLowerCase() === 'inactivo') {
      return <span className="px-3 py-1 rounded-full text-xs font-bold bg-gray-100 text-gray-600 border border-gray-200">Inactivo</span>;
    }
    
    if (cantidad === 0) {
      return <span className="px-3 py-1 rounded-full text-xs font-bold bg-red-100 text-red-600 border border-red-200">Agotado</span>;
    } else if (cantidad <= 10) {
      return <span className="px-3 py-1 rounded-full text-xs font-bold bg-yellow-100 text-yellow-700 border border-yellow-200">Bajo</span>;
    } else if (cantidad <= 50) {
      return <span className="px-3 py-1 rounded-full text-xs font-bold bg-orange-100 text-orange-600 border border-orange-200">Medio</span>;
    } else {
      return <span className="px-3 py-1 rounded-full text-xs font-bold bg-green-100 text-green-600 border border-green-200">En Stock</span>;
    }
  };

  const handleNavigation = (section: string) => {
    if (onNavigate) {
      onNavigate(section);
    }
  };

  if (loading) {
    return (
      <div className="flex h-screen bg-gray-50 font-sans">
        <SidebarNavigation activeSection="inventario" onNavigate={handleNavigation} />
        <main className="flex-1 flex items-center justify-center">
          <div className="flex flex-col items-center gap-4">
            <div className="w-12 h-12 border-4 border-orange-200 border-t-orange-500 rounded-full animate-spin"></div>
            <p className="text-gray-600 font-medium">Cargando productos...</p>
          </div>
        </main>
      </div>
    );
  }

  return (
    <div className="flex h-screen bg-gray-50 font-sans">
      {/* Sidebar Navigation */}
      <SidebarNavigation activeSection="inventario" onNavigate={handleNavigation} />

      {/* Main Content Area - Ocupa el resto del espacio */}
      <main className="flex-1 flex flex-col h-screen overflow-hidden relative">
        
        {/* Header Superior */}
        <header className="px-8 py-6 bg-white shadow-sm border-b border-gray-100 z-10 flex justify-between items-center">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Gestión de Inventario</h1>
            <p className="text-sm text-gray-500 mt-1">Administra tus productos, precios y stock en tiempo real.</p>
          </div>
          {/* Barra de búsqueda (Compacta y redonda) */}
          <div className="relative w-96">
            <input 
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Buscar producto, categoría o SKU..." 
              className="w-full pl-10 pr-4 py-2 bg-gray-100 border-none rounded-full text-sm focus:ring-2 focus:ring-orange-500 outline-none transition-all"
            />
            <span className="absolute left-3 top-2.5 text-gray-400">🔍</span>
          </div>
        </header>

        {/* Scrollable Content */}
        <div className="flex-1 overflow-y-auto p-8">
          
          {/* GRID DE TARJETAS SUPERIORES (KPIs) */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
            
            {/* Card 1: Nuevo Producto (Naranja) */}
            <div 
              onClick={handleCrearNuevo}
              className="bg-orange-500 rounded-2xl p-6 text-white shadow-lg shadow-orange-500/20 relative overflow-hidden group cursor-pointer hover:shadow-orange-500/40 transition-all"
            >
              <div className="relative z-10 flex flex-col h-full justify-between">
                <div className="p-2 bg-white/20 w-fit rounded-lg mb-4">➕</div>
                <div>
                  <h3 className="text-xl font-bold">Nuevo Producto</h3>
                  <p className="text-orange-100 text-sm mt-1">Agregar al catálogo</p>
                </div>
              </div>
              {/* Decoración de fondo */}
              <div className="absolute -bottom-6 -right-6 text-orange-400 opacity-30 transform rotate-12 group-hover:scale-110 transition-transform text-6xl">
                📦
              </div>
            </div>

            {/* Card 2: Realizar Compra (Blanco) */}
            <div 
              onClick={handleComprarProducto}
              className="bg-white rounded-2xl p-6 shadow-sm border border-gray-100 hover:shadow-md transition-all relative overflow-hidden group cursor-pointer"
            >
              <div className="flex flex-col h-full justify-between">
                <div className="p-2 bg-orange-50 w-fit rounded-lg text-orange-500 mb-4 text-2xl">🛒</div>
                <div>
                  <h3 className="text-lg font-bold text-gray-800">Realizar Compra</h3>
                  <p className="text-gray-400 text-sm mt-1">Reabastecer stock</p>
                </div>
              </div>
            </div>

            {/* Card 3: Predicciones IA (Gradiente Morado) */}
            <div 
              onClick={handleShowPredictions}
              className="bg-gradient-to-r from-violet-600 to-indigo-600 rounded-2xl p-6 text-white shadow-lg shadow-indigo-500/20 relative overflow-hidden cursor-pointer hover:shadow-indigo-500/40 transition-all"
            >
              <div className="flex flex-col h-full justify-between relative z-10">
                <div className="p-2 bg-white/20 w-fit rounded-lg mb-4">✨</div>
                <div>
                  <h3 className="text-xl font-bold">Predicciones IA</h3>
                  <p className="text-indigo-100 text-sm mt-1">Análisis de compra</p>
                </div>
              </div>
            </div>
          </div>

          {/* SECCIÓN DE LA TABLA (CARD BLANCA FLOTANTE) */}
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
            <div className="p-6 border-b border-gray-50 flex items-center justify-between">
              <h2 className="text-lg font-bold text-gray-800">Todos los Productos</h2>
              <span className="bg-orange-100 text-orange-600 px-3 py-1 rounded-full text-xs font-bold">
                {filteredProductos.length}
              </span>
            </div>
            
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-gray-400 uppercase tracking-wider">Producto</th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-gray-400 uppercase tracking-wider">Categoría</th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-gray-400 uppercase tracking-wider">Proveedor</th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-gray-400 uppercase tracking-wider">P. Venta</th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-gray-400 uppercase tracking-wider">Stock</th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-gray-400 uppercase tracking-wider">Estado</th>
                    <th className="px-6 py-4 text-right text-xs font-semibold text-gray-400 uppercase tracking-wider">Acciones</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                  {filteredProductos.map((producto) => (
                    <tr key={producto.id} className="hover:bg-gray-50 transition-colors group">
                      {/* Producto */}
                      <td className="px-6 py-4">
                        <div className="flex items-center gap-3">
                          <div className="w-10 h-10 rounded-xl bg-orange-50 flex items-center justify-center text-xl border border-orange-100">
                            📦
                          </div>
                          <div>
                            <p className="font-semibold text-gray-900 text-sm">{producto.nombre}</p>
                            <p className="text-xs text-gray-400">ID: #{producto.id.substring(0, 8)}</p>
                          </div>
                        </div>
                      </td>

                      {/* Categoría */}
                      <td className="px-6 py-4">
                        <span className="text-sm font-medium text-gray-600 bg-gray-100 px-3 py-1 rounded-lg">
                          {producto.categoriasProductosCategoria || 'Sin categoría'}
                        </span>
                      </td>

                      {/* Proveedor */}
                      <td className="px-6 py-4">
                        <div className="flex items-center gap-2">
                          <span className="text-sm text-gray-600">
                            {producto.proveedorNombre || 'Sin proveedor'}
                            {producto.proveedorApellidoPaterno && ` ${producto.proveedorApellidoPaterno}`}
                          </span>
                        </div>
                      </td>

                      {/* Precio Venta */}
                      <td className="px-6 py-4">
                        <span className="text-sm font-bold text-gray-900">
                          {formatPrice(producto.precioVentaActual)}
                        </span>
                      </td>

                      {/* Stock */}
                      <td className="px-6 py-4">
                        <div className="text-center">
                          <span className="text-sm font-bold text-gray-700">
                            {producto.cantidadInventario || 0}
                          </span>
                          <span className="text-xs text-gray-400 block">unidades</span>
                        </div>
                      </td>

                      {/* Estado */}
                      <td className="px-6 py-4">
                        {getStatusBadge(producto.cantidadInventario, producto.estadosEstado)}
                      </td>

                      {/* Acciones */}
                      <td className="px-6 py-4 text-right">
                        <div className="flex items-center justify-end gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
                          {/* Editar */}
                          <button 
                            onClick={() => handleEditarProducto(producto)}
                            className="p-2 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                            title="Editar Producto"
                          >
                            <Edit3 className="w-4 h-4" />
                          </button>

                          {/* Eliminar */}
                          <button 
                            onClick={() => handleEliminarProducto(producto.id, producto.nombre)}
                            className="p-2 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                            title="Eliminar"
                          >
                            <Trash2 className="w-4 h-4" />
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>

              {filteredProductos.length === 0 && (
                <div className="flex flex-col items-center justify-center py-16 px-8 text-center">
                  <div className="w-20 h-20 bg-gray-100 rounded-full flex items-center justify-center mb-4 text-4xl">
                    📦
                  </div>
                  <h3 className="text-lg font-semibold text-gray-600 mb-2">
                    {searchQuery ? 'No se encontraron productos' : 'No hay productos registrados'}
                  </h3>
                  <p className="text-gray-400 text-sm mb-4">
                    {searchQuery 
                      ? `No encontramos productos que coincidan con "${searchQuery}"`
                      : 'Comienza creando tu primer producto con el botón "Nuevo Producto"'
                    }
                  </p>
                  {!searchQuery && (
                    <button 
                      onClick={handleCrearNuevo}
                      className="bg-orange-500 text-white px-6 py-2 rounded-xl font-medium hover:bg-orange-600 transition-colors shadow-lg shadow-orange-200"
                    >
                      Crear Primer Producto
                    </button>
                  )}
                </div>
              )}
            </div>
          </div>
        </div>
      </main>

      {/* Modales */}
      <ModalCrearProducto
        isOpen={showCreateModal}
        onClose={() => setShowCreateModal(false)}
        onSuccess={handleModalSuccess}
      />

      <ModalEditarProducto
        isOpen={showEditModal}
        onClose={() => setShowEditModal(false)}
        onSuccess={handleModalSuccess}
        producto={selectedProduct}
      />

      <ModalPredicciones
        isOpen={showPredictionsModal}
        onClose={() => setShowPredictionsModal(false)}
        onCreatePurchaseOrder={handleCreatePurchaseOrder}
      />
    </div>
  );
};

export default Inventario;
