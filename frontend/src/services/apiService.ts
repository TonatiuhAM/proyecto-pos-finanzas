import axios from 'axios';
import type { 
  LoginCredentials, 
  LoginResponse, 
  WorkspaceStatus, 
  Workspace, 
  CreateWorkspaceRequest,
  TicketVenta,
  FinalizarVentaRequest,
  VentaFinalizada,
  MetodoPago
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

// Configuración base de axios
const api = axios.create({
  baseURL: `${backendUrl}/api`,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptor para añadir el token JWT a todas las peticiones
api.interceptors.request.use(config => {
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
      // Limpiar datos corruptos
      localStorage.removeItem('pos_auth_data');
    }
  }
  
  // Fallback al sistema legacy
  const legacyToken = localStorage.getItem('authToken');
  if (legacyToken) {
    try {
      // Verificar que el token no esté obviously corrupto
      if (legacyToken.includes('.') && legacyToken.length > 50) {
        config.headers.Authorization = `Bearer ${legacyToken}`;
      } else {
        // Token corrupto, remover
        localStorage.removeItem('authToken');
      }
    } catch (error) {
      console.warn('Error processing legacy token:', error);
      localStorage.removeItem('authToken');
    }
  }
  
  return config;
}, error => {
  return Promise.reject(error);
});

// Interceptor para manejar respuestas de error (token expirado, etc.)
api.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401 || error.response?.status === 403) {
      // Token expirado, inválido o permisos insuficientes - limpiar todos los datos de autenticación
      localStorage.removeItem('authToken'); // Legacy
      localStorage.removeItem('pos_auth_data'); // Nuevo sistema
      
      // Solo redirigir a login en 401 (no autenticado), no en 403 (no autorizado)
      if (error.response?.status === 401) {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

// Servicios de autenticación
export const authService = {
  async login(credentials: LoginCredentials): Promise<LoginResponse> {
    const response = await api.post('/auth/login', credentials);
    
    // La respuesta ahora incluye: token, usuario, rolNombre, rolId, expiresIn
    const loginData: LoginResponse = response.data;
    
    // Guardar el token en localStorage (compatibilidad legacy)
    if (loginData.token) {
      localStorage.setItem('authToken', loginData.token);
    }
    
    // Los datos del usuario se manejarán en el AuthContext
    return loginData;
  },
};

// Servicios de workspaces
export const workspaceService = {
  async getAllWithStatus(): Promise<WorkspaceStatus[]> {
    const response = await api.get('/workspaces/status');
    return response.data;
  },

  async getAll(): Promise<Workspace[]> {
    const response = await api.get('/workspaces');
    return response.data;
  },

  async create(workspace: CreateWorkspaceRequest): Promise<Workspace> {
    const response = await api.post('/workspaces', {
      nombre: workspace.nombre,
      permanente: workspace.permanente || false
    });
    return response.data;
  },

  async delete(id: string): Promise<void> {
    await api.delete(`/workspaces/${id}`);
  },

  async update(id: string, workspace: Partial<Workspace>): Promise<Workspace> {
    const response = await api.put(`/workspaces/${id}`, workspace);
    return response.data;
  },

  async getById(id: string): Promise<Workspace> {
    const response = await api.get(`/workspaces/${id}`);
    return response.data;
  },

  // ===== SERVICIOS PARA FLUJO DE CUENTA =====
  
  async cambiarSolicitudCuenta(id: string, solicitudCuenta: boolean): Promise<WorkspaceStatus> {
    const response = await api.patch(`/workspaces/${id}/solicitar-cuenta`, {
      solicitudCuenta: solicitudCuenta
    });
    return response.data;
  },

  async generarTicket(id: string): Promise<TicketVenta> {
    const response = await api.get(`/workspaces/${id}/ticket`);
    return response.data;
  },

  async finalizarVenta(id: string, request: FinalizarVentaRequest): Promise<VentaFinalizada> {
    const response = await api.post(`/workspaces/${id}/finalizar-venta`, request);
    return response.data;
  },
};

// Servicios para métodos de pago
export const metodoPagoService = {
  async getAll(): Promise<MetodoPago[]> {
    const response = await api.get('/metodos_pago');
    return response.data;
  },
};

export default api;
