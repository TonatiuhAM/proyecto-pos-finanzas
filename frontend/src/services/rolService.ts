import axios from 'axios';
import type { Rol } from '../types/index';

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

export const rolService = {
  /**
   * 🔍 Obtener lista de todos los roles para popular dropdown
   * Endpoint: GET /api/roles
   * Returns: Lista de roles con id y nombre
   */
  async obtenerRoles(): Promise<Rol[]> {
    try {
      const response = await axiosInstance.get('/api/roles');
      return response.data;
    } catch (error) {
      console.error('Error al obtener roles:', error);
      throw new Error('No se pudieron cargar los roles');
    }
  },

  /**
   * 🔍 Obtener rol por ID
   * Endpoint: GET /api/roles/{id}
   * Returns: Rol específico con información completa
   */
  async obtenerRolPorId(rolId: number): Promise<Rol> {
    try {
      const response = await axiosInstance.get(`/api/roles/${rolId}`);
      return response.data;
    } catch (error: unknown) {
      console.error('Error al obtener rol por ID:', error);
      
      if (axios.isAxiosError(error) && error.response?.status === 404) {
        throw new Error('Rol no encontrado');
      }
      
      throw new Error('No se pudo obtener la información del rol');
    }
  }
};

export default rolService;
