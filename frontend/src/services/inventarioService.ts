import axios from 'axios';

// Configuración base de axios
const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interfaces TypeScript basadas en el DTO del backend
export interface InventarioDTO {
  id: string;
  cantidadPz?: number;
  cantidadKg?: number;
  cantidadMinima: number;
  cantidadMaxima: number;
  productoId: string;
  productoNombre: string;
  ubicacionId: string;
  ubicacionNombre: string;
}

// Interface para crear/actualizar inventario (formato que espera el backend)
export interface CreateInventarioRequest {
  cantidadPz?: number;
  cantidadKg?: number;
  cantidadMinima: number;
  cantidadMaxima: number;
  producto: {
    id: string;
  };
  ubicacion: {
    id: string;
  };
}

// Interfaces para los datos de los dropdowns
export interface ProductoDTO {
  id: string;
  nombre: string;
  categoriasProductosId?: string;
  categoriasProductosCategoria?: string;
  proveedorId?: string;
  proveedorNombre?: string;
  estadosId?: string;
  estadosEstado?: string;
}

export interface UbicacionDTO {
  id: string;
  ubicacion: string;
  descripcion?: string;
}

// Servicios para inventarios
export const inventarioService = {
  // Obtener todos los inventarios
  getAllInventarios: async (): Promise<InventarioDTO[]> => {
    const response = await api.get<InventarioDTO[]>('/inventarios');
    return response.data;
  },

  // Obtener inventario por ID
  getInventarioById: async (id: string): Promise<InventarioDTO> => {
    const response = await api.get<InventarioDTO>(`/inventarios/${id}`);
    return response.data;
  },

  // Crear nuevo inventario
  createInventario: async (inventario: CreateInventarioRequest): Promise<InventarioDTO> => {
    const response = await api.post<InventarioDTO>('/inventarios', inventario);
    return response.data;
  },

  // Actualizar inventario
  updateInventario: async (id: string, inventario: CreateInventarioRequest): Promise<InventarioDTO> => {
    const response = await api.put<InventarioDTO>(`/inventarios/${id}`, inventario);
    return response.data;
  },

  // Eliminar inventario
  deleteInventario: async (id: string): Promise<void> => {
    await api.delete(`/inventarios/${id}`);
  },

  // Obtener productos para dropdown
  getAllProductos: async (): Promise<ProductoDTO[]> => {
    const response = await api.get<ProductoDTO[]>('/productos');
    return response.data;
  },

  // Obtener ubicaciones para dropdown
  getAllUbicaciones: async (): Promise<UbicacionDTO[]> => {
    const response = await api.get<UbicacionDTO[]>('/ubicaciones');
    return response.data;
  },
};
