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

// ==================== TIPOS PARA FLUJO DE CUENTA FINAL ====================

// Interface para productos en el ticket de venta
export interface ProductoTicket {
  productoId: string;
  productoNombre: string;
  cantidadPz: number;
  cantidadKg: number;
  precioUnitario: number;
  totalPorProducto: number;
}

// Interface para ticket de venta
export interface TicketVenta {
  workspaceId: string;
  workspaceNombre: string;
  productos: ProductoTicket[];
  totalGeneral: number;
  cantidadProductos: number;
}

// Interface para solicitud de finalizar venta
export interface FinalizarVentaRequest {
  metodoPagoId: string;
  clienteId?: string; // Opcional
  usuarioId: string;
}

// Interface para respuesta de venta finalizada
export interface VentaFinalizada {
  ventaId: string;
  workspaceNombre: string;
  totalVenta: number;
  fechaVenta: string;
  metodoPagoNombre: string;
  usuarioNombre: string;
  clienteNombre?: string;
  cantidadProductos: number;
  mensaje: string;
}

// Interface para cambiar estado de workspace
export interface CambiarEstadoWorkspaceRequest {
  solicitudCuenta: boolean;
}

// ==================== TIPOS PARA GESTIÓN DE EMPLEADOS ====================

// Interface para mostrar empleados en la tabla (corresponde a EmpleadoResponseDTO del backend)
export interface Empleado {
  id: string;
  nombre: string;
  telefono: string;
  rolId: string;
  rolNombre: string;
  estadoId: string;
  estadoNombre: string;
}

// Interface para crear nuevos empleados (corresponde a EmpleadoCreateRequestDTO del backend)
export interface EmpleadoCreate {
  nombre: string;
  contrasena: string;
  telefono: string;
  rolId: number;
}

// Interface para cambiar estado de empleado (corresponde a EmpleadoEstadoRequestDTO del backend)
export interface EmpleadoEstadoRequest {
  estado: string; // "Activo" o "Inactivo"
}

// Interface para roles en dropdown (corresponde a RolResponseDTO del backend)
export interface Rol {
  id: string;
  nombre: string;
}

// Interface para el estado de un empleado (para indicadores visuales)
export interface EstadoEmpleado {
  activo: boolean;
  texto: 'Activo' | 'Inactivo';
  color: 'success' | 'error';
}

// Interface para formulario de creación de empleado (estado local del formulario)
export interface FormularioEmpleado {
  nombre: string;
  contrasena: string;
  telefono: string;
  rolId: string;
  errores: {
    nombre?: string;
    contrasena?: string;
    telefono?: string;
    rolId?: string;
  };
}

// Interface para estado de carga de empleados (loading states)
export interface EstadoCargaEmpleados {
  cargando: boolean;
  error: string | null;
  creando: boolean;
  cambiandoEstado: boolean;
}
