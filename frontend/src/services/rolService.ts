import type { Rol } from '../types/index';
import apiService from './apiService';

export const rolService = {
  /**
   * 🔍 Obtener lista de todos los roles para popular dropdown
   * Endpoint: GET /api/roles
   * Returns: Lista de roles con id y nombre
   */
  async obtenerRoles(): Promise<Rol[]> {
    try {
      const response = await apiService.get('/roles');
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
      const response = await apiService.get(`/roles/${rolId}`);
      return response.data;
    } catch (error: unknown) {
      console.error('Error al obtener rol por ID:', error);
      
      if (error && typeof error === 'object' && 'response' in error) {
        const axiosError = error as any;
        if (axiosError.response?.status === 404) {
          throw new Error('Rol no encontrado');
        }
      }
      
      throw new Error('No se pudo obtener la información del rol');
    }
  }
};

export default rolService;
