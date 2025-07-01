import axios from 'axios';
import type { LoginCredentials, UsuarioDTO, WorkspaceStatus, Workspace, CreateWorkspaceRequest } from '../types';

// Configuración base de axios
const api = axios.create({
  baseURL: '/api',
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
