import axios from 'axios';
import type { 
  Empleado, 
  EmpleadoCreate, 
  EmpleadoEstadoRequest 
} from '../types/index';

// Obtener la URL del backend dinámicamente en el cliente
const getBackendUrl = () => {
  // En desarrollo con Docker, usar variable de entorno o localhost
  if (import.meta.env.VITE_API_URL) {
    return import.meta.env.VITE_API_URL;
  }
  
  // En producción real
  if (import.meta.env.PROD && !import.meta.env.VITE_API_URL) {
    return 'https://pos-finanzas-q2ddz.ondigitalocean.app';
  }
  
  // Fallback para desarrollo local
  return 'http://localhost:8080';
};

const backendUrl = getBackendUrl();

const axiosInstance = axios.create({
  baseURL: backendUrl,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const empleadoService = {
  /**
   * 🔍 Obtener lista de todos los empleados
   * Endpoint: GET /api/empleados
   * Returns: Lista de empleados con información completa (nombre, teléfono, rol, estado)
   */
  async obtenerEmpleados(): Promise<Empleado[]> {
    try {
      const response = await axiosInstance.get('/api/empleados');
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
      const response = await axiosInstance.post('/api/empleados', empleadoData);
      return response.data;
    } catch (error: unknown) {
      console.error('Error al crear empleado:', error);
      
      // Manejar errores específicos del servidor
      if (axios.isAxiosError(error) && error.response?.status === 400) {
        const errorMessage = error.response.data || 'Datos inválidos para crear el empleado';
        throw new Error(errorMessage);
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
      const response = await axiosInstance.put(`/api/empleados/${empleadoId}/estado`, estadoRequest);
      return response.data;
    } catch (error: unknown) {
      console.error('Error al cambiar estado del empleado:', error);
      
      // Manejar errores específicos del servidor
      if (axios.isAxiosError(error) && error.response?.status === 400) {
        const errorMessage = error.response.data || 'No se pudo cambiar el estado del empleado';
        throw new Error(errorMessage);
      }
      
      if (axios.isAxiosError(error) && error.response?.status === 404) {
        throw new Error('Empleado no encontrado');
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
      const response = await axiosInstance.get(`/api/empleados/${empleadoId}`);
      return response.data;
    } catch (error: unknown) {
      console.error('Error al obtener empleado por ID:', error);
      
      if (axios.isAxiosError(error) && error.response?.status === 404) {
        throw new Error('Empleado no encontrado');
      }
      
      throw new Error('No se pudo obtener la información del empleado');
    }
  }
};

export default empleadoService;
