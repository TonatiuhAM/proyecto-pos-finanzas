import React, { useState, useEffect } from 'react';
import './Inventario.css';
import { inventarioService } from '../services/inventarioService';
import type { 
  InventarioDTO, 
  CreateInventarioRequest, 
  ProductoDTO, 
  UbicacionDTO 
} from '../services/inventarioService';

const Inventario: React.FC = () => {
  // Estados principales
  const [inventarios, setInventarios] = useState<InventarioDTO[]>([]);
  const [productos, setProductos] = useState<ProductoDTO[]>([]);
  const [ubicaciones, setUbicaciones] = useState<UbicacionDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Estados del modal
  const [showModal, setShowModal] = useState(false);
  const [editingInventario, setEditingInventario] = useState<InventarioDTO | null>(null);
  const [formData, setFormData] = useState<CreateInventarioRequest>({
    cantidadPz: 0,
    cantidadKg: 0,
    cantidadMinima: 0,
    cantidadMaxima: 0,
    producto: { id: '' },
    ubicacion: { id: '' }
  });

  // Cargar datos iniciales
  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      const [inventariosData, productosData, ubicacionesData] = await Promise.all([
        inventarioService.getAllInventarios(),
        inventarioService.getAllProductos(),
        inventarioService.getAllUbicaciones()
      ]);
      
      setInventarios(inventariosData);
      setProductos(productosData);
      setUbicaciones(ubicacionesData);
      setError(null);
    } catch (err) {
      setError('Error al cargar los datos');
      console.error('Error loading data:', err);
    } finally {
      setLoading(false);
    }
  };

  // Abrir modal para crear nuevo inventario
  const handleCrearNuevo = () => {
    setEditingInventario(null);
    setFormData({
      cantidadPz: 0,
      cantidadKg: 0,
      cantidadMinima: 0,
      cantidadMaxima: 0,
      producto: { id: '' },
      ubicacion: { id: '' }
    });
    setShowModal(true);
  };

  // Abrir modal para editar inventario
  const handleEditar = (inventario: InventarioDTO) => {
    setEditingInventario(inventario);
    setFormData({
      cantidadPz: inventario.cantidadPz || 0,
      cantidadKg: inventario.cantidadKg || 0,
      cantidadMinima: inventario.cantidadMinima,
      cantidadMaxima: inventario.cantidadMaxima,
      producto: { id: inventario.productoId },
      ubicacion: { id: inventario.ubicacionId }
    });
    setShowModal(true);
  };

  // Eliminar inventario
  const handleEliminar = async (id: string) => {
    if (window.confirm('¿Estás seguro de que deseas eliminar este registro de inventario?')) {
      try {
        await inventarioService.deleteInventario(id);
        await loadData(); // Recargar datos
      } catch (err) {
        setError('Error al eliminar el inventario');
        console.error('Error deleting inventario:', err);
      }
    }
  };

  // Manejar cambios en el formulario
  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    
    if (name === 'productoId') {
      setFormData(prev => ({
        ...prev,
        producto: { id: value }
      }));
    } else if (name === 'ubicacionId') {
      setFormData(prev => ({
        ...prev,
        ubicacion: { id: value }
      }));
    } else {
      setFormData(prev => ({
        ...prev,
        [name]: name.includes('cantidad') ? parseInt(value) || 0 : value
      }));
    }
  };

  // Guardar inventario (crear o actualizar)
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!formData.producto.id || !formData.ubicacion.id) {
      setError('Debe seleccionar un producto y una ubicación');
      return;
    }

    try {
      if (editingInventario) {
        await inventarioService.updateInventario(editingInventario.id, formData);
      } else {
        await inventarioService.createInventario(formData);
      }
      
      setShowModal(false);
      await loadData(); // Recargar datos
      setError(null);
    } catch (err) {
      setError('Error al guardar el inventario');
      console.error('Error saving inventario:', err);
    }
  };

  // Cerrar modal
  const handleCloseModal = () => {
    setShowModal(false);
    setEditingInventario(null);
    setError(null);
  };

  if (loading) {
    return <div className="loading">Cargando inventarios...</div>;
  }

  return (
    <div className="inventory-page">

      <div className="inventory-container">
        <header className="inventory-header">
          <h1 className="inventory-title">GESTIÓN DE INVENTARIOS</h1>
          <button
            className="create-product-btn"
            onClick={handleCrearNuevo}
          >
            AÑADIR NUEVO INVENTARIO
          </button>
        </header>

        {error && (
          <div className="error-message">
            {error}
          </div>
        )}

        <main className="inventory-table-container">
          <table className="inventory-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Producto</th>
                <th>Ubicación</th>
                <th>Cantidad (Pz)</th>
                <th>Cantidad (Kg)</th>
                <th>Cantidad Mínima</th>
                <th>Cantidad Máxima</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {inventarios.map((inventario) => (
                <tr key={inventario.id}>
                  <td>{inventario.id.substring(0, 8)}...</td>
                  <td>{inventario.productoNombre}</td>
                  <td>{inventario.ubicacionNombre}</td>
                  <td>{inventario.cantidadPz || 'N/A'}</td>
                  <td>{inventario.cantidadKg || 'N/A'}</td>
                  <td>{inventario.cantidadMinima}</td>
                  <td>{inventario.cantidadMaxima}</td>
                  <td>
                    <div className="action-buttons">
                      <button 
                        className="action-btn edit-btn" 
                        onClick={() => handleEditar(inventario)}
                      >
                        Editar
                      </button>
                      <button 
                        className="action-btn delete-btn" 
                        onClick={() => handleEliminar(inventario.id)}
                      >
                        Eliminar
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          {inventarios.length === 0 && (
            <div className="no-data">
              No hay registros de inventario disponibles.
            </div>
          )}
        </main>
      </div>

      {/* Modal para crear/editar inventario */}
      {showModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="modal-header">
              <h2>{editingInventario ? 'Editar Inventario' : 'Crear Nuevo Inventario'}</h2>
              <button className="close-btn" onClick={handleCloseModal}>×</button>
            </div>
            
            <form onSubmit={handleSubmit} className="inventory-form">
              <div className="form-group">
                <label htmlFor="productoId">Producto *</label>
                <select
                  id="productoId"
                  name="productoId"
                  value={formData.producto.id}
                  onChange={handleInputChange}
                  required
                >
                  <option value="">Seleccione un producto</option>
                  {productos.map((producto) => (
                    <option key={producto.id} value={producto.id}>
                      {producto.nombre}
                    </option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label htmlFor="ubicacionId">Ubicación *</label>
                <select
                  id="ubicacionId"
                  name="ubicacionId"
                  value={formData.ubicacion.id}
                  onChange={handleInputChange}
                  required
                >
                  <option value="">Seleccione una ubicación</option>
                  {ubicaciones.map((ubicacion) => (
                    <option key={ubicacion.id} value={ubicacion.id}>
                      {ubicacion.ubicacion}
                    </option>
                  ))}
                </select>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="cantidadPz">Cantidad (Piezas)</label>
                  <input
                    type="number"
                    id="cantidadPz"
                    name="cantidadPz"
                    value={formData.cantidadPz || ''}
                    onChange={handleInputChange}
                    min="0"
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="cantidadKg">Cantidad (Kg)</label>
                  <input
                    type="number"
                    id="cantidadKg"
                    name="cantidadKg"
                    value={formData.cantidadKg || ''}
                    onChange={handleInputChange}
                    min="0"
                  />
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="cantidadMinima">Cantidad Mínima *</label>
                  <input
                    type="number"
                    id="cantidadMinima"
                    name="cantidadMinima"
                    value={formData.cantidadMinima}
                    onChange={handleInputChange}
                    min="0"
                    required
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="cantidadMaxima">Cantidad Máxima *</label>
                  <input
                    type="number"
                    id="cantidadMaxima"
                    name="cantidadMaxima"
                    value={formData.cantidadMaxima}
                    onChange={handleInputChange}
                    min="0"
                    required
                  />
                </div>
              </div>

              <div className="form-actions">
                <button type="button" className="btn-cancel" onClick={handleCloseModal}>
                  Cancelar
                </button>
                <button type="submit" className="btn-save">
                  {editingInventario ? 'Actualizar' : 'Crear'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Inventario;
