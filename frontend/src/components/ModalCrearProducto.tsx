import React, { useState, useEffect } from 'react';
import { inventarioService } from '../services/inventarioService';
import type { ProductoCreacionRequest, CategoriaDTO, ProveedorDTO, UbicacionDTO } from '../services/inventarioService';

interface ModalCrearProductoProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

const ModalCrearProducto: React.FC<ModalCrearProductoProps> = ({ isOpen, onClose, onSuccess }) => {
  const [formData, setFormData] = useState<ProductoCreacionRequest>({
    nombre: '',
    categoriasProductosId: '',
    proveedorId: '',
    precioVenta: 0,
    precioCompra: 0,
    unidadMedida: 'piezas',
    stockInicial: 0,
    ubicacionId: '',
    stockMinimo: 0,
    stockMaximo: 0,
    usuarioId: 'current-user-id' // TODO: Get from auth context
  });

  const [categorias, setCategorias] = useState<CategoriaDTO[]>([]);
  const [proveedores, setProveedores] = useState<ProveedorDTO[]>([]);
  const [ubicaciones, setUbicaciones] = useState<UbicacionDTO[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Load dropdown data when modal opens
  useEffect(() => {
    if (isOpen) {
      loadDropdownData();
    }
  }, [isOpen]);

  const loadDropdownData = async () => {
    try {
      const [categoriasData, proveedoresData, ubicacionesData] = await Promise.all([
        inventarioService.getAllCategorias(),
        inventarioService.getAllProveedores(),
        inventarioService.getAllUbicaciones()
      ]);
      
      setCategorias(categoriasData);
      setProveedores(proveedoresData);
      setUbicaciones(ubicacionesData);
    } catch (err) {
      console.error('Error loading dropdown data:', err);
      setError('Error al cargar los datos del formulario');
    }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    
    setFormData(prev => ({
      ...prev,
      [name]: ['precioVenta', 'precioCompra', 'stockInicial', 'stockMinimo', 'stockMaximo'].includes(name)
        ? parseFloat(value) || 0
        : value
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    // Validate required fields
    if (!formData.nombre.trim()) {
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

    if (!formData.ubicacionId) {
      setError('Debe seleccionar una ubicación');
      setLoading(false);
      return;
    }

    if (formData.precioVenta <= 0) {
      setError('El precio de venta debe ser mayor a 0');
      setLoading(false);
      return;
    }

    if (formData.precioCompra <= 0) {
      setError('El precio de compra debe ser mayor a 0');
      setLoading(false);
      return;
    }

    try {
      await inventarioService.createProductoCompleto(formData);
      onSuccess();
      onClose();
      resetForm();
    } catch (err) {
      console.error('Error creating product:', err);
      setError('Error al crear el producto. Por favor, intente nuevamente.');
    } finally {
      setLoading(false);
    }
  };

  const resetForm = () => {
    setFormData({
      nombre: '',
      categoriasProductosId: '',
      proveedorId: '',
      precioVenta: 0,
      precioCompra: 0,
      unidadMedida: 'piezas',
      stockInicial: 0,
      ubicacionId: '',
      stockMinimo: 0,
      stockMaximo: 0,
      usuarioId: 'current-user-id'
    });
    setError(null);
  };

  const handleClose = () => {
    resetForm();
    onClose();
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay">
      <div className="modal-content modal-lg">
        <div className="modal-header">
          <h2 className="modal-title">Crear Nuevo Producto</h2>
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
                value={formData.nombre}
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
                  value={formData.categoriasProductosId}
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
                  value={formData.proveedorId}
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

          {/* Pricing */}
          <div className="form-section">
            <h3 className="form-section-title">Precios</h3>
            
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="precioCompra" className="form-label">
                  Precio de Compra *
                </label>
                <input
                  type="number"
                  id="precioCompra"
                  name="precioCompra"
                  value={formData.precioCompra}
                  onChange={handleInputChange}
                  className="form-input"
                  min="0"
                  step="0.01"
                  placeholder="0.00"
                  required
                  disabled={loading}
                />
              </div>

              <div className="form-group">
                <label htmlFor="precioVenta" className="form-label">
                  Precio de Venta *
                </label>
                <input
                  type="number"
                  id="precioVenta"
                  name="precioVenta"
                  value={formData.precioVenta}
                  onChange={handleInputChange}
                  className="form-input"
                  min="0"
                  step="0.01"
                  placeholder="0.00"
                  required
                  disabled={loading}
                />
              </div>
            </div>
          </div>

          {/* Inventory */}
          <div className="form-section">
            <h3 className="form-section-title">Inventario</h3>
            
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="unidadMedida" className="form-label">
                  Unidad de Medida *
                </label>
                <select
                  id="unidadMedida"
                  name="unidadMedida"
                  value={formData.unidadMedida}
                  onChange={handleInputChange}
                  className="form-select"
                  required
                  disabled={loading}
                >
                  <option value="piezas">Piezas</option>
                  <option value="kilogramos">Kilogramos</option>
                </select>
              </div>

              <div className="form-group">
                <label htmlFor="ubicacionId" className="form-label">
                  Ubicación *
                </label>
                <select
                  id="ubicacionId"
                  name="ubicacionId"
                  value={formData.ubicacionId}
                  onChange={handleInputChange}
                  className="form-select"
                  required
                  disabled={loading}
                >
                  <option value="">Seleccione una ubicación</option>
                  {ubicaciones.map((ubicacion) => (
                    <option key={ubicacion.id} value={ubicacion.id}>
                      {ubicacion.ubicacion}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label htmlFor="stockInicial" className="form-label">
                  Stock Inicial
                </label>
                <input
                  type="number"
                  id="stockInicial"
                  name="stockInicial"
                  value={formData.stockInicial}
                  onChange={handleInputChange}
                  className="form-input"
                  min="0"
                  placeholder="0"
                  disabled={loading}
                />
              </div>

              <div className="form-group">
                <label htmlFor="stockMinimo" className="form-label">
                  Stock Mínimo
                </label>
                <input
                  type="number"
                  id="stockMinimo"
                  name="stockMinimo"
                  value={formData.stockMinimo}
                  onChange={handleInputChange}
                  className="form-input"
                  min="0"
                  placeholder="0"
                  disabled={loading}
                />
              </div>

              <div className="form-group">
                <label htmlFor="stockMaximo" className="form-label">
                  Stock Máximo
                </label>
                <input
                  type="number"
                  id="stockMaximo"
                  name="stockMaximo"
                  value={formData.stockMaximo}
                  onChange={handleInputChange}
                  className="form-input"
                  min="0"
                  placeholder="0"
                  disabled={loading}
                />
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
              {loading ? 'Creando...' : 'Crear Producto'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default ModalCrearProducto;
