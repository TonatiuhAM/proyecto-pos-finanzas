import axios from 'axios';

// Obtener la URL del backend dinámicamente en el cliente
const getBackendUrl = () => {
  if (import.meta.env.PROD) {
    return 'https://pos-finanzas-q2ddz.ondigitalocean.app/api';
  }
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
  proveedorApellidoPaterno?: string;
  proveedorApellidoMaterno?: string;
  estadosId?: string;
  estadosEstado?: string;
  precioVentaActual?: number;
  precioCompraActual?: number;
  cantidadInventario?: number;
}

export interface UbicacionDTO {
  id: string;
  nombre: string;
  ubicacion: string;
  descripcion?: string;
}

export interface CategoriaDTO {
  id: string;
  categoria: string;
}

export interface ProveedorDTO {
  id: string;
  nombre: string;
  apellidoPaterno?: string;
  apellidoMaterno?: string;
}

export interface UsuarioDTO {
  id: string;
  nombreUsuario: string;
  nombre?: string;
  apellidoPaterno?: string;
  apellidoMaterno?: string;
}

// Interface para crear un producto completo
export interface ProductoCreacionRequest {
  nombre: string;
  categoriasProductosId: string;
  proveedorId: string;
  precioVenta: number;
  precioCompra: number;
  unidadMedida: string; // "piezas" o "kilogramos"
  stockInicial: number;
  ubicacionId: string;
  stockMinimo: number;
  stockMaximo: number;
  usuarioId: string;
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

  // Obtener categorías para dropdown
  getAllCategorias: async (): Promise<CategoriaDTO[]> => {
    const response = await api.get<CategoriaDTO[]>('/categorias-productos');
    return response.data;
  },

  // Obtener proveedores para dropdown (personas que son proveedores)
  getAllProveedores: async (): Promise<ProveedorDTO[]> => {
    const response = await api.get<ProveedorDTO[]>('/personas');
    return response.data;
  },

  // Obtener usuarios para dropdown
  getAllUsuarios: async (): Promise<UsuarioDTO[]> => {
    const response = await api.get<UsuarioDTO[]>('/usuarios');
    return response.data;
  },

  // Obtener el primer usuario disponible (solución temporal)
  getFirstAvailableUser: async (): Promise<string> => {
    const usuarios = await inventarioService.getAllUsuarios();
    if (usuarios.length === 0) {
      throw new Error('No hay usuarios disponibles en el sistema');
    }
    return usuarios[0].id;
  },

  // Crear producto completo con inventario inicial
  createProductoCompleto: async (producto: ProductoCreacionRequest): Promise<ProductoDTO> => {
    const response = await api.post<ProductoDTO>('/productos/completo', producto);
    return response.data;
  },

  // Desactivar producto
  desactivarProducto: async (id: string): Promise<ProductoDTO> => {
    console.log('🌐 API Call: PATCH /productos/' + id + '/desactivar');
    console.log('🌐 Backend URL:', `${backendUrl}/api`);
    try {
      const response = await api.patch<ProductoDTO>(`/productos/${id}/desactivar`);
      console.log('✅ API Response:', response.data);
      return response.data;
    } catch (error) {
      console.error('❌ API Error:', error);
      throw error;
    }
  },

  // Actualizar producto
  updateProducto: async (id: string, producto: Partial<ProductoDTO>): Promise<ProductoDTO> => {
    const response = await api.put<ProductoDTO>(`/productos/${id}`, producto);
    return response.data;
  },
};
