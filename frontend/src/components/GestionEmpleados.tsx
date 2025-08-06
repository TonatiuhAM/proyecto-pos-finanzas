import React, { useState, useEffect } from 'react';
import './GestionEmpleados.css';
import { empleadoService } from '../services/empleadoService';
import { rolService } from '../services/rolService';
import { useToast } from '../hooks/useToast';
import type { 
  Empleado, 
  Rol, 
  EstadoCargaEmpleados 
} from '../types/index';
import ModalCrearEmpleado from './ModalCrearEmpleado';

const GestionEmpleados: React.FC = () => {
  // Estados principales
  const [empleados, setEmpleados] = useState<Empleado[]>([]);
  const [roles, setRoles] = useState<Rol[]>([]);
  const [estadoCarga, setEstadoCarga] = useState<EstadoCargaEmpleados>({
    cargando: true,
    error: null,
    creando: false,
    cambiandoEstado: false
  });

  // Estados para modal
  const [modalAbierto, setModalAbierto] = useState(false);

  // Hook personalizado para toasts
  const { showSuccess, showError } = useToast();

  /**
   * 🔄 Cargar empleados desde la API
   */
  const cargarEmpleados = async () => {
    try {
      setEstadoCarga(prev => ({ ...prev, cargando: true, error: null }));
      const empleadosData = await empleadoService.obtenerEmpleados();
      setEmpleados(empleadosData);
    } catch (error: any) {
      const errorMessage = error.message || 'Error al cargar empleados';
      setEstadoCarga(prev => ({ ...prev, error: errorMessage }));
      showError(errorMessage);
    } finally {
      setEstadoCarga(prev => ({ ...prev, cargando: false }));
    }
  };

  /**
   * 🔄 Cargar roles desde la API
   */
  const cargarRoles = async () => {
    try {
      const rolesData = await rolService.obtenerRoles();
      setRoles(rolesData);
    } catch (error: any) {
      const errorMessage = error.message || 'Error al cargar roles';
      showError(errorMessage);
    }
  };

  /**
   * 🔄 Cambiar estado de empleado (Activo/Inactivo)
   */
  const cambiarEstadoEmpleado = async (empleadoId: string, estadoActual: string) => {
    const nuevoEstado = estadoActual === 'Activo' ? 'Inactivo' : 'Activo';
    const nombreEmpleado = empleados.find(emp => emp.id === empleadoId)?.nombre || 'empleado';

    // Confirmación antes de cambiar estado
    if (!window.confirm(`¿Está seguro de cambiar el estado de ${nombreEmpleado} a ${nuevoEstado}?`)) {
      return;
    }

    try {
      setEstadoCarga(prev => ({ ...prev, cambiandoEstado: true }));
      
      const empleadoActualizado = await empleadoService.cambiarEstadoEmpleado(
        empleadoId, 
        nuevoEstado
      );

      // Actualizar el empleado en la lista local
      setEmpleados(prev => 
        prev.map(emp => 
          emp.id === empleadoId ? empleadoActualizado : emp
        )
      );

      showSuccess(`Estado de ${nombreEmpleado} cambiado a ${nuevoEstado}`);
    } catch (error: any) {
      const errorMessage = error.message || 'Error al cambiar estado del empleado';
      showError(errorMessage);
    } finally {
      setEstadoCarga(prev => ({ ...prev, cambiandoEstado: false }));
    }
  };

  /**
   * ➕ Manejar creación de nuevo empleado
   */
  const manejarEmpleadoCreado = (nuevoEmpleado: Empleado) => {
    setEmpleados(prev => [...prev, nuevoEmpleado]);
    setModalAbierto(false);
    showSuccess(`Empleado ${nuevoEmpleado.nombre} creado exitosamente`);
  };

  // Cargar datos iniciales
  useEffect(() => {
    cargarEmpleados();
    cargarRoles();
  }, []);

  return (
    <div className="gestion-empleados">
      {/* Header */}
      <div className="gestion-empleados__header">
        <h1 className="gestion-empleados__titulo">
          <i className="material-icons">people</i>
          Gestión de Empleados
        </h1>
        <button 
          className="btn-primary gestion-empleados__btn-crear"
          onClick={() => setModalAbierto(true)}
          disabled={estadoCarga.creando}
        >
          <i className="material-icons">person_add</i>
          Agregar Empleado
        </button>
      </div>

      {/* Estados de carga y error */}
      {estadoCarga.cargando && (
        <div className="gestion-empleados__loading">
          <div className="loading-spinner"></div>
          <p>Cargando empleados...</p>
        </div>
      )}

      {estadoCarga.error && (
        <div className="gestion-empleados__error">
          <i className="material-icons">error</i>
          <p>{estadoCarga.error}</p>
          <button 
            className="btn-secondary"
            onClick={cargarEmpleados}
          >
            Reintentar
          </button>
        </div>
      )}

      {/* Tabla de empleados */}
      {!estadoCarga.cargando && !estadoCarga.error && (
        <div className="gestion-empleados__tabla-container">
          <table className="gestion-empleados__tabla">
            <thead>
              <tr>
                <th>Nombre</th>
                <th>Teléfono</th>
                <th>Rol</th>
                <th>Estado</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {empleados.length === 0 ? (
                <tr>
                  <td colSpan={5} className="gestion-empleados__vacio">
                    <i className="material-icons">people_outline</i>
                    <p>No hay empleados registrados</p>
                  </td>
                </tr>
              ) : (
                empleados.map((empleado) => (
                  <tr key={empleado.id} className="gestion-empleados__fila">
                    <td className="gestion-empleados__nombre">
                      <i className="material-icons">person</i>
                      {empleado.nombre}
                    </td>
                    <td className="gestion-empleados__telefono">
                      {empleado.telefono || 'No especificado'}
                    </td>
                    <td className="gestion-empleados__rol">
                      <span className="rol-badge">
                        {empleado.rolNombre}
                      </span>
                    </td>
                    <td className="gestion-empleados__estado">
                      <span 
                        className={`estado-badge ${
                          empleado.estadoNombre === 'Activo' ? 'estado-activo' : 'estado-inactivo'
                        }`}
                      >
                        <i className="material-icons">
                          {empleado.estadoNombre === 'Activo' ? 'check_circle' : 'cancel'}
                        </i>
                        {empleado.estadoNombre}
                      </span>
                    </td>
                    <td className="gestion-empleados__acciones">
                      <button
                        className={`btn-toggle ${
                          empleado.estadoNombre === 'Activo' ? 'btn-toggle--activo' : 'btn-toggle--inactivo'
                        }`}
                        onClick={() => cambiarEstadoEmpleado(empleado.id, empleado.estadoNombre)}
                        disabled={estadoCarga.cambiandoEstado}
                        title={`Cambiar a ${empleado.estadoNombre === 'Activo' ? 'Inactivo' : 'Activo'}`}
                      >
                        {empleado.estadoNombre === 'Activo' ? 'Desactivar' : 'Activar'}
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      )}

      {/* Modal de creación */}
      {modalAbierto && (
        <ModalCrearEmpleado
          roles={roles}
          onEmpleadoCreado={manejarEmpleadoCreado}
          onCerrar={() => setModalAbierto(false)}
          estadoCarga={estadoCarga}
          setEstadoCarga={setEstadoCarga}
        />
      )}
    </div>
  );
};

export default GestionEmpleados;
