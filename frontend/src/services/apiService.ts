import axios from 'axios';
import type { LoginCredentials, UsuarioDTO, WorkspaceStatus, Workspace, CreateWorkspaceRequest } from '../types/index';

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

// Servicios de autenticación
export const authService = {
  async login(credentials: LoginCredentials): Promise<UsuarioDTO> {
    const response = await api.post('/auth/login', credentials);
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
