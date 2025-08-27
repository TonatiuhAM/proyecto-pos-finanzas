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

  return {
    showSuccess,
    showError,
    showInfo,
    showWarning,
    showConfirm
  };
};
