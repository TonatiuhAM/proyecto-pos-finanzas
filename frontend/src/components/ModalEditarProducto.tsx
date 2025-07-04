import React, { useState, useEffect } from 'react';
import { inventarioService } from '../services/inventarioService';
import type { ProductoDTO, CategoriaDTO, ProveedorDTO } from '../services/inventarioService';

interface ModalEditarProductoProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
  producto: ProductoDTO | null;
}

const ModalEditarProducto: React.FC<ModalEditarProductoProps> = ({ 
  isOpen, 
  onClose, 
  onSuccess, 
  producto 
}) => {
  const [formData, setFormData] = useState<Partial<ProductoDTO>>({});
  const [categorias, setCategorias] = useState<CategoriaDTO[]>([]);
  const [proveedores, setProveedores] = useState<ProveedorDTO[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Load form data when modal opens or producto changes
  useEffect(() => {
    if (isOpen && producto) {
      setFormData({
        id: producto.id,
        nombre: producto.nombre,
        categoriasProductosId: producto.categoriasProductosId || '',
        proveedorId: producto.proveedorId || '',
        precioVentaActual: producto.precioVentaActual || 0,
        precioCompraActual: producto.precioCompraActual || 0,
      });
      loadDropdownData();
    }
  }, [isOpen, producto]);

  const loadDropdownData = async () => {
    try {
      const [categoriasData, proveedoresData] = await Promise.all([
        inventarioService.getAllCategorias(),
        inventarioService.getAllProveedores()
      ]);
      
      setCategorias(categoriasData);
      setProveedores(proveedoresData);
    } catch (err) {
      console.error('Error loading dropdown data:', err);
      setError('Error al cargar los datos del formulario');
    }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    
    setFormData(prev => ({
      ...prev,
      [name]: ['precioVentaActual', 'precioCompraActual'].includes(name)
        ? parseFloat(value) || 0
        : value
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!producto) return;
    
    setError(null);
    setLoading(true);

    // Validate required fields
    if (!formData.nombre?.trim()) {
      setError('El nombre del producto es requerido');
      setLoading(false);
      return;
    }

    if (!formData.categoriasProductosId) {
      setError('Debe seleccionar una categoría');
      setLoading(false);
      return;
    }

    if (!formData.proveedorId) {
      setError('Debe seleccionar un proveedor');
      setLoading(false);
      return;
    }

    try {
      await inventarioService.updateProducto(producto.id, formData);
      onSuccess();
      onClose();
    } catch (err) {
      console.error('Error updating product:', err);
      setError('Error al actualizar el producto. Por favor, intente nuevamente.');
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    setFormData({});
    setError(null);
    onClose();
  };

  if (!isOpen || !producto) return null;

  return (
    <div className="modal-overlay">
      <div className="modal-content modal-lg">
        <div className="modal-header">
          <h2 className="modal-title">Editar Producto</h2>
          <button 
            className="close-btn" 
            onClick={handleClose}
            disabled={loading}
          >
            ×
          </button>
        </div>
        
        <form onSubmit={handleSubmit} className="product-form">
          {error && (
            <div className="error-message mb-4">
              {error}
            </div>
          )}

          {/* Product Basic Info */}
          <div className="form-section">
            <h3 className="form-section-title">Información Básica</h3>
            
            <div className="form-group">
              <label htmlFor="nombre" className="form-label">
                Nombre del Producto *
              </label>
              <input
                type="text"
                id="nombre"
                name="nombre"
                value={formData.nombre || ''}
                onChange={handleInputChange}
                className="form-input"
                placeholder="Ingrese el nombre del producto"
                required
                disabled={loading}
              />
            </div>

            <div className="form-row">
              <div className="form-group">
                <label htmlFor="categoriasProductosId" className="form-label">
                  Categoría *
                </label>
                <select
                  id="categoriasProductosId"
                  name="categoriasProductosId"
                  value={formData.categoriasProductosId || ''}
                  onChange={handleInputChange}
                  className="form-select"
                  required
                  disabled={loading}
                >
                  <option value="">Seleccione una categoría</option>
                  {categorias.map((categoria) => (
                    <option key={categoria.id} value={categoria.id}>
                      {categoria.categoria}
                    </option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label htmlFor="proveedorId" className="form-label">
                  Proveedor *
                </label>
                <select
                  id="proveedorId"
                  name="proveedorId"
                  value={formData.proveedorId || ''}
                  onChange={handleInputChange}
                  className="form-select"
                  required
                  disabled={loading}
                >
                  <option value="">Seleccione un proveedor</option>
                  {proveedores.map((proveedor) => (
                    <option key={proveedor.id} value={proveedor.id}>
                      {proveedor.nombre} {proveedor.apellidoPaterno || ''} {proveedor.apellidoMaterno || ''}
                    </option>
                  ))}
                </select>
              </div>
            </div>
          </div>

          {/* Current Pricing (Read-only info) */}
          <div className="form-section">
            <h3 className="form-section-title">Precios Actuales</h3>
            <p className="form-help-text">
              Los precios se gestionan a través del historial de precios. Estos son valores de referencia.
            </p>
            
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="precioCompraActual" className="form-label">
                  Precio de Compra Actual
                </label>
                <input
                  type="number"
                  id="precioCompraActual"
                  name="precioCompraActual"
                  value={formData.precioCompraActual || 0}
                  onChange={handleInputChange}
                  className="form-input"
                  min="0"
                  step="0.01"
                  placeholder="0.00"
                  disabled={loading}
                />
              </div>

              <div className="form-group">
                <label htmlFor="precioVentaActual" className="form-label">
                  Precio de Venta Actual
                </label>
                <input
                  type="number"
                  id="precioVentaActual"
                  name="precioVentaActual"
                  value={formData.precioVentaActual || 0}
                  onChange={handleInputChange}
                  className="form-input"
                  min="0"
                  step="0.01"
                  placeholder="0.00"
                  disabled={loading}
                />
              </div>
            </div>
          </div>

          {/* Current Status (Read-only info) */}
          <div className="form-section">
            <h3 className="form-section-title">Estado Actual</h3>
            <div className="form-row">
              <div className="form-group">
                <label className="form-label">Estado</label>
                <div className="form-readonly">
                  <span className={`status-badge ${producto.estadosEstado?.toLowerCase()}`}>
                    {producto.estadosEstado || 'No definido'}
                  </span>
                </div>
              </div>

              <div className="form-group">
                <label className="form-label">Inventario Actual</label>
                <div className="form-readonly">
                  {producto.cantidadInventario || 0} unidades
                </div>
              </div>
            </div>
          </div>

          <div className="form-actions">
            <button 
              type="button" 
              className="btn btn-cancel" 
              onClick={handleClose}
              disabled={loading}
            >
              Cancelar
            </button>
            <button 
              type="submit" 
              className="btn btn-primary" 
              disabled={loading}
            >
              {loading ? 'Actualizando...' : 'Actualizar Producto'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default ModalEditarProducto;
