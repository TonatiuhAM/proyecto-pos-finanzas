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

// ==================== TIPOS PARA PUNTO DE VENTA ====================

// Interface para items en el carrito temporal (solo frontend)
export interface ItemCarrito {
  productoId: string;
  productoNombre: string;
  precio: number;
  cantidadPz: number;
  cantidadKg: number;
  stockDisponiblePz: number;
  stockDisponibleKg: number;
}

// Interface para órdenes workspace (backend)
export interface OrdenesWorkspace {
  id: string;
  workspaceId: string;
  workspaceNombre: string;
  productoId: string;
  productoNombre: string;
  historialPrecioId: string;
  precio: number;
  cantidadPz: number;
  cantidadKg: number;
}

// Interface para órdenes de ventas (backend)
export interface OrdenesDeVentas {
  id: string;
  personaId?: string;
  personaNombre?: string;
  usuarioId: string;
  usuarioNombre: string;
  fecha: string;
  metodoPagoId: string;
  metodoPagoNombre: string;
  total: number;
  detalles: DetallesOrdenesDeVentas[];
}

// Interface para detalles de órdenes de ventas (backend)
export interface DetallesOrdenesDeVentas {
  id: string;
  ordenVentaId: string;
  productoId: string;
  productoNombre: string;
  historialPrecioId: string;
  precio: number;
  cantidadKg: number;
  cantidadPz: number;
  subtotal: number;
}

// Interface para métodos de pago
export interface MetodoPago {
  id: string;
  metodoPago: string;
  descripcion?: string;
}

// Interface para personas/clientes
export interface Persona {
  id: string;
  nombre: string;
  apellidoPaterno?: string;
  apellidoMaterno?: string;
  telefono?: string;
  email?: string;
}
