import type { 
  Empleado, 
  EmpleadoCreate, 
  EmpleadoEstadoRequest 
} from '../types/index';
import apiService from './apiService';

export const empleadoService = {
  /**
   * 🔍 Obtener lista de todos los empleados
   * Endpoint: GET /api/empleados
   * Returns: Lista de empleados con información completa (nombre, teléfono, rol, estado)
   */
  async obtenerEmpleados(): Promise<Empleado[]> {
    try {
      const response = await apiService.get('/empleados');
      return response.data;
    } catch (error) {
      console.error('Error al obtener empleados:', error);
      throw new Error('No se pudieron cargar los empleados');
    }
  },

  /**
   * ➕ Crear nuevo empleado
   * Endpoint: POST /api/empleados
   * Body: EmpleadoCreate (nombre, contrasena, telefono, rolId)
   * Returns: Empleado creado con información completa
   */
  async crearEmpleado(empleadoData: EmpleadoCreate): Promise<Empleado> {
    try {
      const response = await apiService.post('/empleados', empleadoData);
      return response.data;
    } catch (error: unknown) {
      console.error('Error al crear empleado:', error);
      
      // Manejar errores específicos del servidor
      if (error && typeof error === 'object' && 'response' in error) {
        const axiosError = error as any;
        if (axiosError.response?.status === 400) {
          const errorMessage = axiosError.response.data || 'Datos inválidos para crear el empleado';
          throw new Error(errorMessage);
        }
      }
      
      throw new Error('No se pudo crear el empleado');
    }
  },

  /**
   * 🔄 Cambiar estado de empleado (Activo/Inactivo)
   * Endpoint: PUT /api/empleados/{id}/estado
   * Body: EmpleadoEstadoRequest (estado)
   * Returns: Empleado actualizado
   */
  async cambiarEstadoEmpleado(empleadoId: string, nuevoEstado: string): Promise<Empleado> {
    try {
      const estadoRequest: EmpleadoEstadoRequest = { estado: nuevoEstado };
      const response = await apiService.put(`/empleados/${empleadoId}/estado`, estadoRequest);
      return response.data;
    } catch (error: unknown) {
      console.error('Error al cambiar estado del empleado:', error);
      
      // Manejar errores específicos del servidor
      if (error && typeof error === 'object' && 'response' in error) {
        const axiosError = error as any;
        if (axiosError.response?.status === 400) {
          const errorMessage = axiosError.response.data || 'No se pudo cambiar el estado del empleado';
          throw new Error(errorMessage);
        }
        
        if (axiosError.response?.status === 404) {
          throw new Error('Empleado no encontrado');
        }
      }
      
      throw new Error('No se pudo cambiar el estado del empleado');
    }
  },

  /**
   * 🔍 Obtener empleado por ID
   * Endpoint: GET /api/empleados/{id}
   * Returns: Empleado específico con información completa
   */
  async obtenerEmpleadoPorId(empleadoId: number): Promise<Empleado> {
    try {
      const response = await apiService.get(`/empleados/${empleadoId}`);
      return response.data;
    } catch (error: unknown) {
      console.error('Error al obtener empleado por ID:', error);
      
      if (error && typeof error === 'object' && 'response' in error) {
        const axiosError = error as any;
        if (axiosError.response?.status === 404) {
          throw new Error('Empleado no encontrado');
        }
      }
      
      throw new Error('No se pudo obtener la información del empleado');
    }
  }
};

export default empleadoService;
