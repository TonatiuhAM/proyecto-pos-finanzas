import axios from 'axios';
import type { LoginCredentials, UsuarioDTO, LoginResponse, WorkspaceStatus, Workspace, CreateWorkspaceRequest } from '../types/index';

// Obtener la URL del backend dinámicamente en el cliente
const getBackendUrl = () => {
  // En producción, la API está en el mismo host, pero en un subdominio o ruta diferente.
  // Asumimos que el frontend se sirve desde un dominio y el backend desde otro.
  // La URL del backend de producción se proporciona directamente.
  if (import.meta.env.PROD) {
    return 'https://sc-pos-finanzas-backend.azuremicroservices.io';
  }
  // En desarrollo, usamos la URL del proxy de Vite.
  return window.location.origin;
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
  const token = localStorage.getItem('authToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
}, error => {
  return Promise.reject(error);
});

// Interceptor para manejar respuestas de error (token expirado, etc.)
api.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      // Token expirado o inválido
      localStorage.removeItem('authToken');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// Servicios de autenticación
export const authService = {
  async login(credentials: LoginCredentials): Promise<LoginResponse> {
    const response = await api.post('/auth/login', credentials);
    
    // Guardar el token en localStorage
    if (response.data.token) {
      localStorage.setItem('authToken', response.data.token);
    }
    
    return response.data;
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
};
