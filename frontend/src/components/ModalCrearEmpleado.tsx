import React, { useState } from 'react';
import { empleadoService } from '../services/empleadoService';
import type { 
  Empleado, 
  EmpleadoCreate, 
  Rol, 
  FormularioEmpleado,
  EstadoCargaEmpleados 
} from '../types/index';

interface ModalCrearEmpleadoProps {
  roles: Rol[];
  onEmpleadoCreado: (empleado: Empleado) => void;
  onCerrar: () => void;
  estadoCarga: EstadoCargaEmpleados;
  setEstadoCarga: React.Dispatch<React.SetStateAction<EstadoCargaEmpleados>>;
}

const ModalCrearEmpleado: React.FC<ModalCrearEmpleadoProps> = ({
  roles,
  onEmpleadoCreado,
  onCerrar,
  estadoCarga,
  setEstadoCarga
}) => {
  // Estado del formulario
  const [formulario, setFormulario] = useState<FormularioEmpleado>({
    nombre: '',
    contrasena: '',
    telefono: '',
    rolId: '',
    errores: {}
  });

  /**
   * 📝 Validar formulario
   */
  const validarFormulario = (): boolean => {
    const errores: FormularioEmpleado['errores'] = {};

    // Validar nombre
    if (!formulario.nombre.trim()) {
      errores.nombre = 'El nombre es requerido';
    } else if (formulario.nombre.trim().length < 2) {
      errores.nombre = 'El nombre debe tener al menos 2 caracteres';
    }

    // Validar contraseña
    if (!formulario.contrasena) {
      errores.contrasena = 'La contraseña es requerida';
    } else if (formulario.contrasena.length < 4) {
      errores.contrasena = 'La contraseña debe tener al menos 4 caracteres';
    }

    // Validar teléfono (opcional pero si se proporciona debe tener formato válido)
    if (formulario.telefono && !/^\d{10}$/.test(formulario.telefono.replace(/\s/g, ''))) {
      errores.telefono = 'El teléfono debe tener 10 dígitos';
    }

    // Validar rol
    if (!formulario.rolId) {
      errores.rolId = 'Debe seleccionar un rol';
    }

    setFormulario(prev => ({ ...prev, errores }));
    return Object.keys(errores).length === 0;
  };

  /**
   * 📝 Manejar cambios en inputs
   */
  const manejarCambio = (campo: keyof FormularioEmpleado, valor: string) => {
    setFormulario(prev => ({
      ...prev,
      [campo]: valor,
      errores: {
        ...prev.errores,
        [campo]: undefined // Limpiar error al escribir
      }
    }));
  };

  /**
   * 📤 Manejar envío del formulario
   */
  const manejarEnvio = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validarFormulario()) {
      return;
    }

    try {
      setEstadoCarga(prev => ({ ...prev, creando: true }));

      const empleadoData: EmpleadoCreate = {
        nombre: formulario.nombre.trim(),
        contrasena: formulario.contrasena,
        telefono: formulario.telefono.trim() || '',
        rolId: parseInt(formulario.rolId)
      };

      const nuevoEmpleado = await empleadoService.crearEmpleado(empleadoData);
      onEmpleadoCreado(nuevoEmpleado);
    } catch (error: any) {
      // Mostrar error específico del servidor
      const errorMessage = error.message || 'Error al crear empleado';
      
      // Si es un error de validación específico, mostrarlo en el campo correspondiente
      if (errorMessage.toLowerCase().includes('nombre')) {
        setFormulario(prev => ({
          ...prev,
          errores: { ...prev.errores, nombre: errorMessage }
        }));
      } else if (errorMessage.toLowerCase().includes('rol')) {
        setFormulario(prev => ({
          ...prev,
          errores: { ...prev.errores, rolId: errorMessage }
        }));
      } else {
        // Error general
        alert(errorMessage);
      }
    } finally {
      setEstadoCarga(prev => ({ ...prev, creando: false }));
    }
  };

  /**
   * 🔄 Resetear formulario
   */
  const resetearFormulario = () => {
    setFormulario({
      nombre: '',
      contrasena: '',
      telefono: '',
      rolId: '',
      errores: {}
    });
  };

  /**
   * ❌ Cerrar modal
   */
  const cerrarModal = () => {
    resetearFormulario();
    onCerrar();
  };

  return (
    <div className="modal-overlay" onClick={cerrarModal}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        {/* Header del modal */}
        <div className="modal-header">
          <h2 className="modal-titulo">
            <span className="material-icons">person_add</span>
            Crear Nuevo Empleado
          </h2>
          <button 
            className="modal-cerrar"
            onClick={cerrarModal}
            disabled={estadoCarga.creando}
          >
            <span className="material-icons">close</span>
          </button>
        </div>

        {/* Formulario */}
        <form onSubmit={manejarEnvio} className="modal-formulario">
          {/* Campo Nombre */}
          <div className="campo-formulario">
            <label htmlFor="nombre" className="campo-label">
              <span className="material-icons">person</span>
              Nombre Completo *
            </label>
            <input
              type="text"
              id="nombre"
              value={formulario.nombre}
              onChange={(e) => manejarCambio('nombre', e.target.value)}
              className={`campo-input ${formulario.errores.nombre ? 'campo-input--error' : ''}`}
              placeholder="Ingrese el nombre completo"
              disabled={estadoCarga.creando}
              maxLength={100}
            />
            {formulario.errores.nombre && (
              <span className="campo-error">{formulario.errores.nombre}</span>
            )}
          </div>

          {/* Campo Contraseña */}
          <div className="campo-formulario">
            <label htmlFor="contrasena" className="campo-label">
              <span className="material-icons">lock</span>
              Contraseña *
            </label>
            <input
              type="password"
              id="contrasena"
              value={formulario.contrasena}
              onChange={(e) => manejarCambio('contrasena', e.target.value)}
              className={`campo-input ${formulario.errores.contrasena ? 'campo-input--error' : ''}`}
              placeholder="Ingrese la contraseña"
              disabled={estadoCarga.creando}
              maxLength={50}
            />
            {formulario.errores.contrasena && (
              <span className="campo-error">{formulario.errores.contrasena}</span>
            )}
          </div>

          {/* Campo Teléfono */}
          <div className="campo-formulario">
            <label htmlFor="telefono" className="campo-label">
              <span className="material-icons">phone</span>
              Teléfono
            </label>
            <input
              type="tel"
              id="telefono"
              value={formulario.telefono}
              onChange={(e) => manejarCambio('telefono', e.target.value)}
              className={`campo-input ${formulario.errores.telefono ? 'campo-input--error' : ''}`}
              placeholder="1234567890"
              disabled={estadoCarga.creando}
              maxLength={15}
            />
            {formulario.errores.telefono && (
              <span className="campo-error">{formulario.errores.telefono}</span>
            )}
          </div>

          {/* Campo Rol */}
          <div className="campo-formulario">
            <label htmlFor="rolId" className="campo-label">
              <span className="material-icons">work</span>
              Rol *
            </label>
            <select
              id="rolId"
              value={formulario.rolId}
              onChange={(e) => manejarCambio('rolId', e.target.value)}
              className={`campo-input campo-select ${formulario.errores.rolId ? 'campo-input--error' : ''}`}
              disabled={estadoCarga.creando}
            >
              <option value="">Seleccione un rol</option>
              {roles.map((rol) => (
                <option key={rol.id} value={rol.id}>
                  {rol.nombre}
                </option>
              ))}
            </select>
            {formulario.errores.rolId && (
              <span className="campo-error">{formulario.errores.rolId}</span>
            )}
          </div>

          {/* Información adicional */}
          <div className="info-adicional">
            <span className="material-icons">info</span>
            <p>
              El empleado será creado con estado <strong>Activo</strong> por defecto.
              La contraseña será hasheada de forma segura.
            </p>
          </div>

          {/* Botones */}
          <div className="modal-botones">
            <button
              type="button"
              className="btn-secondary"
              onClick={cerrarModal}
              disabled={estadoCarga.creando}
            >
              Cancelar
            </button>
            <button
              type="submit"
              className="btn-primary"
              disabled={estadoCarga.creando}
            >
              {estadoCarga.creando ? (
                <>
                  <div className="loading-spinner-small"></div>
                  Creando...
                </>
              ) : (
                <>
                  <span className="material-icons">save</span>
                  Crear Empleado
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default ModalCrearEmpleado;
