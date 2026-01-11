// Interfaces para órdenes de venta y actividad reciente
export interface OrdenVentaReciente {
  id: string;
  usuario: string;
  mesa: string | null;
  fechaVenta: string;
  hora: string;
  total: number;
  totalFormateado: string;
}

export interface OrdenVentaResponse {
  id: string;
  fechaVenta: string;
  total: number;
  mesa?: string;
  usuarioNombre: string;
  estado: string;
}

export interface ActividadRecienteResponse {
  ordenes: OrdenVentaReciente[];
  total: number;
}