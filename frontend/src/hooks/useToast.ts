import { toast } from 'react-toastify';
import type { ToastOptions } from 'react-toastify';

const defaultOptions: ToastOptions = {
  position: "top-center", // ✅ Cambiar a centro superior para máxima visibilidad
  autoClose: false, // ✅ NO cierre automático - requiere clic manual
  hideProgressBar: true, // Ocultar barra de progreso ya que no hay autoclose
  closeOnClick: true, // ✅ Cerrar al hacer clic
  pauseOnHover: false, // No necesario sin autoclose
  draggable: true,
  progress: undefined,
  style: {
    fontSize: '18px', // ✅ Tamaño más grande para mayor visibilidad
    fontWeight: '700',
    padding: '24px', // ✅ Más padding
    borderRadius: '16px', // ✅ Bordes más redondeados
    boxShadow: '0 12px 48px rgba(0, 0, 0, 0.5)', // ✅ Sombra más prominente
    zIndex: 99999, // ✅ z-index extremadamente alto
    cursor: 'pointer', // Indicar que es clickeable
    userSelect: 'none', // Evitar selección de texto
    minWidth: '400px', // ✅ Ancho mínimo para mejor lectura
    textAlign: 'center', // ✅ Centrar texto
  },
};

export const useToast = () => {
  const showSuccess = (message: string, options?: ToastOptions) => {
    toast.success(message, { 
      ...defaultOptions, 
      style: {
        ...defaultOptions.style,
        backgroundColor: '#4caf50',
        color: 'white',
        fontSize: '17px',
        fontWeight: '700',
        border: '3px solid #2e7d32',
      },
      ...options 
    });
  };

  const showError = (message: string, options?: ToastOptions) => {
    toast.error(message, { 
      ...defaultOptions, 
      style: {
        ...defaultOptions.style,
        backgroundColor: '#f44336',
        color: 'white',
        fontSize: '17px',
        fontWeight: '700',
        border: '3px solid #c62828',
      },
      ...options 
    });
  };

  const showInfo = (message: string, options?: ToastOptions) => {
    toast.info(message, { 
      ...defaultOptions, 
      style: {
        ...defaultOptions.style,
        backgroundColor: '#2196f3',
        color: 'white',
        fontSize: '17px',
        fontWeight: '700',
        border: '3px solid #1565c0',
      },
      ...options 
    });
  };

  const showWarning = (message: string, options?: ToastOptions) => {
    toast.warning(message, { 
      ...defaultOptions, 
      style: {
        ...defaultOptions.style,
        backgroundColor: '#ff9800',
        color: 'white',
        fontSize: '17px',
        fontWeight: '700',
        border: '3px solid #e65100',
      },
      ...options 
    });
  };

  const showConfirm = (message: string, onConfirm: () => void, onCancel?: () => void) => {
    // Usando window.confirm como fallback ya que custom toast confirm es complejo
    const result = window.confirm(message);
    if (result) {
      onConfirm();
    } else if (onCancel) {
      onCancel();
    }
    return result;
  };

  /**
   * Toast especializado para alertas de stock bajo
   * @param nombreProducto - Nombre del producto con stock bajo
   * @param cantidadActual - Cantidad actual en inventario
   * @param options - Opciones adicionales de toast
   */
   const showStockWarning = (nombreProducto: string, cantidadActual: number, options?: ToastOptions) => {
     const message = `⚠️ STOCK BAJO

📦 ${nombreProducto}
📊 Cantidad: ${cantidadActual} unidades
🔴 Stock crítico

Considera reabastecer este producto pronto.

👆 Haz clic para cerrar`;

    toast.warning(message, { 
      ...defaultOptions, 
      style: {
        ...defaultOptions.style,
        backgroundColor: '#ff9800',
        color: 'white',
        fontSize: '16px',
        fontWeight: '700',
        border: '3px solid #e65100',
        lineHeight: '1.4',
      },
      ...options 
    });
  };

  /**
   * Toast para múltiples productos con stock bajo (agrupado)
   * @param productos - Array de productos con stock bajo
   * @param options - Opciones adicionales de toast
   */
   const showMultipleStockWarning = (productos: Array<{nombre: string, cantidad: number}>, options?: ToastOptions) => {
     const count = productos.length;
     const message = `⚠️ ${count} PRODUCTOS CON STOCK BAJO

${productos.slice(0, 3).map(p => `📦 ${p.nombre}: ${p.cantidad} unidades`).join('\n')}
${count > 3 ? `\n... y ${count - 3} productos más` : ''}

🔍 Revisa el inventario para reabastecer.

👆 Haz clic para cerrar`;

    toast.warning(message, { 
      ...defaultOptions, 
      style: {
        ...defaultOptions.style,
        backgroundColor: '#ff9800',
        color: 'white',
        fontSize: '15px',
        fontWeight: '700',
        border: '3px solid #e65100',
        lineHeight: '1.4',
        minWidth: '450px',
      },
      ...options 
    });
  };

  return {
    showSuccess,
    showError,
    showInfo,
    showWarning,
    showConfirm,
    showStockWarning,
    showMultipleStockWarning
  };
};
