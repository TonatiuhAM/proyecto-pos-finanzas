import React, { useState, useEffect } from 'react';
import './InventarioModerno.css';
import { inventarioService } from '../services/inventarioService';
import ModalCrearProducto from './ModalCrearProducto';
import ModalEditarProducto from './ModalEditarProducto';
import type { ProductoDTO } from '../services/inventarioService';

interface InventarioProps {
  onNavigateToCompras?: () => void;
}

const Inventario: React.FC<InventarioProps> = ({ onNavigateToCompras }) => {
  // Estados principales
  const [productos, setProductos] = useState<ProductoDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Estados de los modales
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [selectedProduct, setSelectedProduct] = useState<ProductoDTO | null>(null);

  // Cargar datos iniciales
  useEffect(() => {
    loadProductos();
  }, []);

  const loadProductos = async () => {
    try {
      setLoading(true);
      const productosData = await inventarioService.getAllProductos();
      // Filtrar solo productos activos
      const productosActivos = productosData.filter(producto => 
        producto.estadosEstado?.toLowerCase() === 'activo'
      );
      setProductos(productosActivos);
      setError(null);
    } catch (err) {
      setError('Error al cargar los productos');
      console.error('Error loading productos:', err);
    } finally {
      setLoading(false);
    }
  };

  // Abrir modal para crear nuevo producto
  const handleCrearNuevo = () => {
    setShowCreateModal(true);
  };

  // Navegar a compras a proveedores
  const handleComprarProducto = () => {
    console.log('🛒 Botón Comprar Producto clickeado!');
    console.log('onNavigateToCompras:', onNavigateToCompras);
    if (onNavigateToCompras) {
      onNavigateToCompras();
    } else {
      console.warn('onNavigateToCompras no está definido');
    }
  };

  // Abrir modal para editar producto
  const handleEditarProducto = (producto: ProductoDTO) => {
    setSelectedProduct(producto);
    setShowEditModal(true);
  };

  // Desactivar producto
  const handleDesactivarProducto = async (id: string, nombre: string) => {
    console.log('🚀 handleDesactivarProducto called with:', { id, nombre });
    
    if (window.confirm(`¿Estás seguro de que deseas desactivar el producto "${nombre}"?`)) {
      console.log('✅ User confirmed deactivation');
      try {
        console.log('🔄 Calling inventarioService.desactivarProducto...');
        const result = await inventarioService.desactivarProducto(id);
        console.log('✅ Deactivation successful:', result);
        
        console.log('🔄 Reloading products...');
        await loadProductos(); // Recargar datos
        console.log('✅ Products reloaded');
        setError(null);
      } catch (err) {
        console.error('❌ Error deactivating product:', err);
        setError('Error al desactivar el producto');
      }
    } else {
      console.log('❌ User cancelled deactivation');
    }
  };

  // Manejar éxito en creación/edición
  const handleModalSuccess = () => {
    loadProductos();
  };

  // Función para formatear precio
  const formatPrice = (price: number | undefined) => {
    if (price === undefined || price === null) return 'N/A';
    return new Intl.NumberFormat('es-MX', {
      style: 'currency',
      currency: 'MXN'
    }).format(price);
  };

  // Función para obtener clase CSS del estado
  const getStatusClass = (estado: string | undefined) => {
    if (!estado) return 'status-unknown';
    switch (estado.toLowerCase()) {
      case 'activo':
        return 'status-active';
      case 'inactivo':
        return 'status-inactive';
      case 'agotado':
        return 'status-out-of-stock';
      default:
        return 'status-unknown';
    }
  };

  if (loading) {
    return (
      <div className="loading-container">
        <div className="loading-spinner"></div>
        <p>Cargando productos...</p>
      </div>
    );
  }

  return (
    <div className="inventory-content">
      {/* 🚨 DEBUG: Mensaje visual temporal para confirmar renderizado */}
      <div style={{
        backgroundColor: '#ff0000',
        color: 'white',
        padding: '20px',
        fontSize: '20px',
        fontWeight: 'bold',
        textAlign: 'center',
        border: '5px solid #ffff00',
        margin: '10px 0'
      }}>
        🚨 DEBUG: El componente Inventario se está renderizando correctamente! 🚨
      </div>

      {error && (
        <div className="error-message">
          <span className="error-icon">⚠️</span>
          {error}
        </div>
      )}

      <div className="inventory-controls">
        {/* 🚨 DEBUG: Botón de compras con estilos súper llamativos */}
        <div style={{
          backgroundColor: '#ff0000',
          border: '10px solid #ffff00',
          padding: '20px',
          margin: '20px 0'
        }}>
          <h2 style={{ color: 'white', fontSize: '24px', margin: '0 0 10px 0' }}>
            🚨 DEBUG: AQUÍ ESTÁ EL BOTÓN DE COMPRAS 🚨
          </h2>
          <button
            className="purchase-btn"
            onClick={handleComprarProducto}
            title="Ir a compras a proveedores"
            style={{
              backgroundColor: '#4CAF50',
              color: 'white',
              border: '5px solid #000000',
              padding: '20px 40px',
              borderRadius: '8px',
              fontWeight: 'bold',
              fontSize: '18px',
              cursor: 'pointer',
              boxShadow: '0 8px 16px rgba(0,0,0,0.5)',
              minWidth: '300px',
              display: 'flex',
              alignItems: 'center',
              gap: '8px'
            }}
          >
            <span className="btn-icon">🛒</span>
            COMPRAR PRODUCTO (DEBUG)
          </button>
        </div>
        <button
          className="create-product-btn"
          onClick={handleCrearNuevo}
        >
          <span className="btn-icon">+</span>
          CREAR NUEVO PRODUCTO
        </button>
      </div>

      <main className="inventory-table-container">
          <div className="table-wrapper">
            <table className="inventory-table">
              <thead>
                <tr>
                  <th>Producto</th>
                  <th>Categoría</th>
                  <th>Proveedor</th>
                  <th>Precio Compra</th>
                  <th>Precio Venta</th>
                  <th>Stock</th>
                  <th>Estado</th>
                  <th>Acciones</th>
                </tr>
              </thead>
              <tbody>
                {productos.map((producto) => (
                  <tr key={producto.id} className="table-row">
                    <td className="product-name-cell">
                      <div className="product-info">
                        <span className="product-name">{producto.nombre}</span>
                      </div>
                    </td>
                    <td>
                      <span className="category-badge">
                        {producto.categoriasProductosCategoria || 'Sin categoría'}
                      </span>
                    </td>
                    <td>
                      <div className="supplier-info">
                        <span className="supplier-name">
                          {producto.proveedorNombre || 'Sin proveedor'}
                          {producto.proveedorApellidoPaterno && ` ${producto.proveedorApellidoPaterno}`}
                        </span>
                      </div>
                    </td>
                    <td>
                      <span className="price-value">
                        {formatPrice(producto.precioCompraActual)}
                      </span>
                    </td>
                    <td>
                      <span className="price-value">
                        {formatPrice(producto.precioVentaActual)}
                      </span>
                    </td>
                    <td>
                      <div className="stock-info">
                        <span className={`stock-value ${(producto.cantidadInventario || 0) <= 10 ? 'low-stock' : ''}`}>
                          {producto.cantidadInventario || 0}
                        </span>
                        <span className="stock-unit">unidades</span>
                      </div>
                    </td>
                    <td>
                      <span className={`status-badge ${getStatusClass(producto.estadosEstado)}`}>
                        {producto.estadosEstado || 'No definido'}
                      </span>
                    </td>
                    <td>
                      <div className="action-buttons">
                        <button 
                          className="action-btn edit-btn" 
                          onClick={() => handleEditarProducto(producto)}
                          title="Editar producto"
                        >
                          <span className="btn-icon">✏️</span>
                          Editar
                        </button>
                        {producto.estadosEstado !== 'inactivo' && (
                          <button 
                            className="action-btn deactivate-btn" 
                            onClick={() => handleDesactivarProducto(producto.id, producto.nombre)}
                            title="Desactivar producto"
                          >
                            <span className="btn-icon">🚫</span>
                            Desactivar
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>

            {productos.length === 0 && (
              <div className="no-data">
                <div className="no-data-icon">📦</div>
                <h3>No hay productos registrados</h3>
                <p>Comienza creando tu primer producto con el botón "Crear Nuevo Producto"</p>
              </div>
            )}
          </div>
        </main>

        {/* Estadísticas rápidas */}
        <div className="quick-stats">
          <div className="stat-card">
            <div className="stat-value">{productos.length}</div>
            <div className="stat-label">Total Productos</div>
          </div>
          <div className="stat-card">
            <div className="stat-value">
              {productos.filter(p => {
                const estado = p.estadosEstado?.toLowerCase() || '';
                return estado === 'activo' || estado === 'active';
              }).length}
            </div>
            <div className="stat-label">Activos</div>
          </div>
          <div className="stat-card">
            <div className="stat-value">
              {productos.filter(p => (p.cantidadInventario || 0) <= 10).length}
            </div>
            <div className="stat-label">Stock Bajo</div>
          </div>
        </div>

        {/* Modal para crear producto */}
        <ModalCrearProducto
          isOpen={showCreateModal}
          onClose={() => setShowCreateModal(false)}
          onSuccess={handleModalSuccess}
        />

        {/* Modal para editar producto */}
        <ModalEditarProducto
          isOpen={showEditModal}
          onClose={() => setShowEditModal(false)}
          onSuccess={handleModalSuccess}
          producto={selectedProduct}
        />
      </div>
    );
  };

export default Inventario;
