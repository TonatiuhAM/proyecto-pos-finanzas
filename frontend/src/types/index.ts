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

// NUEVO: Respuesta de login con información de rol
export interface LoginResponse {
  token: string;
  usuario: string;
  rolNombre: string;
  rolId: string;
  expiresIn: number;
}

// NUEVO: Usuario autenticado para el contexto
export interface UsuarioAutenticado {
  usuario: string;
  rolNombre: string;
  rolId: string;
  token: string;
  expiresIn: number;
}

// NUEVO: Tipos de roles del sistema
export type RolUsuario = 'Administrador' | 'Empleado';

// LEGACY: Mantener compatibilidad con respuesta anterior (deprecado)
export interface LoginResponseLegacy {
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
  rolId: string;
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

// ==================== TIPOS PARA SISTEMA DE COMPRAS ====================

// Interface para proveedor (corresponde a ProveedorDTO del backend)
export interface Proveedor {
  id: string;
  nombre: string;
  apellidoPaterno?: string;
  apellidoMaterno?: string;
  telefono?: string;
  email?: string;
  nombreCompleto: string;
  deudaTotal: number;
}

// Interface para productos en orden de compra (corresponde a ProductoCompraDTO del backend)
export interface ProductoCompraDTO {
  productoId: string;
  cantidadKg: number;
  cantidadPz: number;
}

// Interface para request de compra (corresponde a CompraRequestDTO del backend)
export interface CompraRequest {
  proveedorId: string;
  metodoPagoId?: string;
  productos: ProductoCompraDTO[];
}

// Interface para request de pago (corresponde a PagoRequestDTO del backend)
export interface PagoRequest {
  proveedorId: string;
  ordenCompraId: string;
  montoPagado: number;
  metodoPagoId: string;
  pagarTodoElTotal: boolean;
}

// Interface para deuda de proveedor mejorada (corresponde a DeudaProveedorDTO del backend)
export const EstadoDeuda = {
  PENDIENTE: 'PENDIENTE',
  PARCIAL: 'PARCIAL',
  PAGADA: 'PAGADA'
} as const;

export type EstadoDeuda = typeof EstadoDeuda[keyof typeof EstadoDeuda];

// Interface para la estructura actual de DeudaProveedor (usada por el servicio)
export interface DeudaProveedor {
  proveedorId: string;
  proveedorNombre: string;
  telefonoProveedor?: string;
  emailProveedor?: string;
  totalCompras: number;
  totalPagos: number;
  deudaPendiente: number;
  estadoDeuda: 'verde' | 'amarillo';
  fechaOrdenMasAntigua?: string;
  cantidadOrdenesPendientes: number;
}

// Interface para la nueva estructura de DeudaProveedor (para futura compatibilidad)
export interface DeudaProveedorNueva {
  id: number;
  proveedor: {
    id: number;
    nombreCompleto: string;
    email: string;
    telefono: string;
  };
  montoDeuda: number;
  fechaVencimiento: string;
  estado: EstadoDeuda;
  descripcion?: string;
}

// Interface para estadísticas de deudas
export interface EstadisticasDeudas {
  totalProveedoresConDeuda: number;
  totalDeudasPendientes: number;
  deudaPromedio: number;
}

// Interface para producto en compras
export interface ProductoCompra {
  id: string;
  nombre: string;
  stockActualKg: number;
  stockActualPz: number;
  categoria: string;
  costoPromedioKg: number;
  costoPromedioPz: number;
}

// Interface para orden de compra
export interface OrdenCompra {
  id: string;
  proveedorId: string;
  proveedorNombre: string;
  fecha: string;
  total: number;
  usuarioId: string;
  usuarioNombre: string;
  detalles: DetalleOrdenCompra[];
}

// Interface para detalle de orden de compra
export interface DetalleOrdenCompra {
  id: string;
  ordenCompraId: string;
  productoId: string;
  productoNombre: string;
  cantidadKg: number;
  cantidadPz: number;
  costoUnitarioKg: number;
  costoUnitarioPz: number;
  subtotal: number;
}

// Interface para item en carrito de compras (solo frontend)
export interface ItemCarritoCompra {
  productoId: string;
  productoNombre: string;
  cantidadKg: number;
  cantidadPz: number;
  costoUnitarioKg: number;
  costoUnitarioPz: number;
  subtotal: number;
}

// Interface para formulario de compra
export interface FormularioCompra {
  proveedorId: string;
  productos: ItemCarritoCompra[];
  total: number;
  metodoPagoId?: string;
  pagoInmediato: boolean;
  montoPago?: number;
}

// ==================== TIPOS PARA GESTIÓN UNIFICADA DE PERSONAS ====================

// Interface para categorías de personas (corresponde a CategoriaPersonaDTO del backend)
export interface CategoriaPersona {
  id: string;
  nombre: string;
}

// Interface para crear nueva persona (corresponde a PersonaCreateRequestDTO del backend)
export interface PersonaCreateRequest {
  nombre: string;
  apellidos: string;
  rfc?: string;
  email?: string;
  telefono?: string;
  direccion?: string;
  idCategoriaPersona: string;
}

// Interface para mostrar personas en tabla (corresponde a PersonaResponseDTO del backend)
export interface PersonaResponse {
  id: string;
  nombre: string;
  apellidoPaterno?: string;
  apellidoMaterno?: string;
  apellidos?: string; // Campo de compatibilidad
  nombreCompleto: string;
  rfc?: string;
  email?: string;
  telefono?: string;
  direccion?: string;
  idCategoriaPersona: string;
  nombreCategoria: string;
  idEstado: string;
  nombreEstado: string;
}

// Interface para formulario de creación de persona (estado local del frontend)
export interface FormularioPersona {
  nombre: string;
  apellidos: string;
  rfc: string;
  email: string;
  telefono: string;
  direccion: string;
  idCategoriaPersona: string;
  errores: {
    nombre?: string;
    apellidos?: string;
    rfc?: string;
    email?: string;
    telefono?: string;
    direccion?: string;
    idCategoriaPersona?: string;
  };
}

// Interface para gestión de estado de carga
export interface EstadoCargaPersonas {
  cargando: boolean;
  cargandoCategorias: boolean;
  error: string | null;
  creando: boolean;
  cambiandoEstado: boolean;
}

// Interface para request de cambio de estado
export interface CambiarEstadoPersonaRequest {
  estadoNombre: string;
}

// Enum para tipos de categorías de personas (para facilitar el manejo en frontend)
export const TipoCategoriaPersona = {
  EMPLEADO: "a1c85197-a54f-4686-9964-73f3d0965d4f", // UUID real de empleados
  PROVEEDOR: "50887317-1DD8-4DE4-AAC5-62A342AC7FD4", // UUID real de proveedores
  CLIENTE: "39348296-3d59-419f-94fd-7681276e47fc" // UUID real de clientes
} as const;

export type TipoCategoriaPersona = typeof TipoCategoriaPersona[keyof typeof TipoCategoriaPersona];

// Interface para filtros de personas
export interface FiltrosPersonas {
  categoria: string | null;
  estado: string | null;
  busqueda: string;
}
