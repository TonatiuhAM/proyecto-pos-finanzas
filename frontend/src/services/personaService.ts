import type { 
  PersonaCreateRequest, 
  PersonaResponse, 
  CategoriaPersona,
  CambiarEstadoPersonaRequest 
} from '../types';
import { TipoCategoriaPersona } from '../types';
import apiService from './apiService';

const BASE_URL = '/personas';
const CATEGORIAS_URL = '/categorias-personas';

export const personaService = {
  // ==================== GESTIÓN DE PERSONAS ====================
  
  /**
   * Crear una nueva persona
   */
  async crearPersona(persona: PersonaCreateRequest): Promise<PersonaResponse> {
    const response = await apiService.post<PersonaResponse>(BASE_URL, persona);
    return response.data;
  },

  /**
   * Obtener todas las personas por categoría
   */
  async obtenerPersonasPorCategoria(idCategoria: string): Promise<PersonaResponse[]> {
    const response = await apiService.get<PersonaResponse[]>(`${BASE_URL}/categoria/${idCategoria}`);
    return response.data;
  },

  /**
   * Obtener todas las personas activas por categoría
   */
  async obtenerPersonasActivasPorCategoria(idCategoria: string): Promise<PersonaResponse[]> {
    const response = await apiService.get<PersonaResponse[]>(`${BASE_URL}/categoria/${idCategoria}/activos`);
    return response.data;
  },

  /**
   * Obtener una persona por ID
   */
  async obtenerPersonaPorId(id: string): Promise<PersonaResponse> {
    const response = await apiService.get<PersonaResponse>(`${BASE_URL}/${id}`);
    return response.data;
  },

  /**
   * Actualizar el estado de una persona
   */
  async actualizarEstadoPersona(id: string, request: CambiarEstadoPersonaRequest): Promise<PersonaResponse> {
    const response = await apiService.patch<PersonaResponse>(`${BASE_URL}/${id}/estado`, request);
    return response.data;
  },

  /**
   * Eliminar una persona (soft delete)
   */
  async eliminarPersona(id: string): Promise<void> {
    await apiService.delete(`${BASE_URL}/${id}`);
  },

  // ==================== GESTIÓN DE CATEGORÍAS ====================

  /**
   * Obtener todas las categorías de personas
   */
  async obtenerCategorias(): Promise<CategoriaPersona[]> {
    const response = await apiService.get<CategoriaPersona[]>(CATEGORIAS_URL);
    return response.data;
  },

  /**
   * Obtener una categoría por ID
   */
  async obtenerCategoriaPorId(id: string): Promise<CategoriaPersona> {
    const response = await apiService.get<CategoriaPersona>(`${CATEGORIAS_URL}/${id}`);
    return response.data;
  },

  // ==================== MÉTODOS DE CONVENIENCIA ====================

  /**
   * Obtener empleados activos (categoría 1)
   */
  async obtenerEmpleadosActivos(): Promise<PersonaResponse[]> {
    return this.obtenerPersonasActivasPorCategoria(TipoCategoriaPersona.EMPLEADO);
  },

  /**
   * Obtener proveedores activos (categoría 2)
   */
  async obtenerProveedoresActivos(): Promise<PersonaResponse[]> {
    return this.obtenerPersonasActivasPorCategoria(TipoCategoriaPersona.PROVEEDOR);
  },

  /**
   * Obtener clientes activos (categoría 3)
   */
  async obtenerClientesActivos(): Promise<PersonaResponse[]> {
    return this.obtenerPersonasActivasPorCategoria(TipoCategoriaPersona.CLIENTE);
  },

  /**
   * Activar una persona (cambiar estado a activo)
   */
  async activarPersona(id: string): Promise<PersonaResponse> {
    return this.actualizarEstadoPersona(id, { estadoNombre: "Activo" });
  },

  /**
   * Desactivar una persona (cambiar estado a inactivo)
   */
  async desactivarPersona(id: string): Promise<PersonaResponse> {
    return this.actualizarEstadoPersona(id, { estadoNombre: "Inactivo" });
  }
};

export default personaService;