// Tipos para la autenticación
export interface LoginCredentials {
  nombre: string;
  contrasena: string;
}

export interface UsuarioDTO {
  id: string;
  nombre: string;
  telefono: string;
  rolesId?: string;
  rolesRoles?: string;
  estadosId?: string;
  estadosEstado?: string;
}

export interface LoginResponse {
  usuario: UsuarioDTO;
  token: string;
}

// Tipos para workspaces
export interface Workspace {
  id: string;
  nombre: string;
  permanente?: boolean;
}

export interface WorkspaceStatus {
  id: string;
  nombre: string;
  estado: 'disponible' | 'ocupado' | 'cuenta';
  cantidadOrdenes: number;
  permanente?: boolean;
  ultimaActividad?: string;
}

export interface CreateWorkspaceRequest {
  nombre: string;
  permanente?: boolean;
}
