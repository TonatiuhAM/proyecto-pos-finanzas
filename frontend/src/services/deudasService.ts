import axios from 'axios';
import type { DeudaProveedor, EstadisticasDeudas } from '../types/index';

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

// Configuración base de axios para deudas de proveedores
const deudasApi = axios.create({
  baseURL: `${backendUrl}/api/deudas-proveedores`,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptor para añadir el token JWT a todas las peticiones
deudasApi.interceptors.request.use(config => {
  // Buscar token en el nuevo sistema de autenticación primero
  const authData = localStorage.getItem('pos_auth_data');
  if (authData) {
    try {
      const userData = JSON.parse(authData);
      if (userData.token) {
        config.headers.Authorization = `Bearer ${userData.token}`;
        return config;
      }
    } catch (error) {
      console.warn('Error parsing auth data:', error);
    }
  }
  
  // Fallback al sistema legacy
  const legacyToken = localStorage.getItem('authToken');
  if (legacyToken) {
    config.headers.Authorization = `Bearer ${legacyToken}`;
  }
  
  return config;
}, error => {
  return Promise.reject(error);
});

// Interceptor para manejar respuestas de error
deudasApi.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      // Token expirado o inválido - limpiar todos los datos de autenticación
      localStorage.removeItem('authToken');
      localStorage.removeItem('pos_auth_data');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

/**
 * Servicio para gestionar las deudas de proveedores
 * Proporciona métodos para obtener información de deudas, estadísticas y reportes
 */
export const deudasProveedoresService = {
  /**
   * Obtiene la lista completa de proveedores con deudas pendientes
   * @returns Promise con array de DeudaProveedor
   */
  async obtenerDeudasProveedores(): Promise<DeudaProveedor[]> {
    try {
      const response = await deudasApi.get('');
      return response.data;
    } catch (error) {
      console.error('Error al obtener deudas de proveedores:', error);
      throw new Error('No se pudieron cargar las deudas de proveedores');
    }
  },

  /**
   * Obtiene la deuda específica de un proveedor
   * @param proveedorId ID del proveedor
   * @returns Promise con DeudaProveedor o null si no tiene deudas
   */
  async obtenerDeudaProveedor(proveedorId: string): Promise<DeudaProveedor | null> {
    try {
      const response = await deudasApi.get(`/${proveedorId}`);
      return response.data;
    } catch (error) {
      if (axios.isAxiosError(error) && error.response?.status === 204) {
        // Sin contenido significa que no hay deudas para este proveedor
        return null;
      }
      console.error('Error al obtener deuda del proveedor:', error);
      throw new Error('No se pudo cargar la información de deuda del proveedor');
    }
  },

  /**
   * Obtiene estadísticas generales de las deudas a proveedores
   * @returns Promise con EstadisticasDeudas
   */
  async obtenerEstadisticasDeudas(): Promise<EstadisticasDeudas> {
    try {
      const response = await deudasApi.get('/estadisticas');
      return {
        totalProveedoresConDeuda: Number(response.data.totalProveedoresConDeuda),
        totalDeudasPendientes: Number(response.data.totalDeudasPendientes),
        deudaPromedio: Number(response.data.deudaPromedio)
      };
    } catch (error) {
      console.error('Error al obtener estadísticas de deudas:', error);
      throw new Error('No se pudieron cargar las estadísticas de deudas');
    }
  },

  /**
   * Obtiene el total de todas las deudas pendientes
   * @returns Promise con el total de deudas pendientes
   */
  async obtenerTotalDeudasPendientes(): Promise<number> {
    try {
      const response = await deudasApi.get('/total');
      return Number(response.data.totalDeudasPendientes);
    } catch (error) {
      console.error('Error al obtener total de deudas pendientes:', error);
      throw new Error('No se pudo cargar el total de deudas pendientes');
    }
  },

  /**
   * Filtra las deudas por estado
   * @param deudas Array de deudas
   * @param estado Estado a filtrar ('verde' o 'amarillo')
   * @returns Array filtrado de deudas
   */
  filtrarPorEstado(deudas: DeudaProveedor[], estado: 'verde' | 'amarillo'): DeudaProveedor[] {
    return deudas.filter(deuda => deuda.estadoDeuda === estado);
  },

  /**
   * Ordena las deudas por cantidad adeudada (descendente)
   * @param deudas Array de deudas
   * @returns Array ordenado de deudas
   */
  ordenarPorDeuda(deudas: DeudaProveedor[]): DeudaProveedor[] {
    return [...deudas].sort((a, b) => b.deudaPendiente - a.deudaPendiente);
  },

  /**
   * Ordena las deudas por fecha de orden más antigua
   * @param deudas Array de deudas
   * @returns Array ordenado de deudas
   */
  ordenarPorFecha(deudas: DeudaProveedor[]): DeudaProveedor[] {
    return [...deudas].sort((a, b) => {
      if (!a.fechaOrdenMasAntigua) return 1;
      if (!b.fechaOrdenMasAntigua) return -1;
      return new Date(a.fechaOrdenMasAntigua).getTime() - new Date(b.fechaOrdenMasAntigua).getTime();
    });
  },

  /**
   * Busca deudas por nombre de proveedor
   * @param deudas Array de deudas
   * @param termino Término de búsqueda
   * @returns Array filtrado de deudas
   */
  buscarPorNombre(deudas: DeudaProveedor[], termino: string): DeudaProveedor[] {
    if (!termino.trim()) return deudas;
    const terminoLower = termino.toLowerCase();
    return deudas.filter(deuda => 
      deuda.proveedorNombre.toLowerCase().includes(terminoLower)
    );
  }
};