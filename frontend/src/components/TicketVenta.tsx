import React, { useState } from 'react';
import { workspaceService, metodoPagoService } from '../services/apiService';
import { inventarioService } from '../services/inventarioService';
import type { TicketVenta as TicketVentaData, MetodoPago, FinalizarVentaRequest, VentaFinalizada } from '../types';
import './TicketVenta.css';

interface TicketVentaProps {
  ticket: TicketVentaData;
  onVentaFinalizada: (venta: VentaFinalizada) => void;
  onCerrar: () => void;
}

const TicketVenta: React.FC<TicketVentaProps> = ({ ticket, onVentaFinalizada, onCerrar }) => {
  const [metodosPago, setMetodosPago] = useState<MetodoPago[]>([]);
  const [metodoPagoSeleccionado, setMetodoPagoSeleccionado] = useState<string>('');
  const [isLoading, setIsLoading] = useState(false);
  const [mostrarModalPago, setMostrarModalPago] = useState(false);

  // Cargar métodos de pago al mostrar el modal
  React.useEffect(() => {
    const cargarMetodosPago = async () => {
      try {
        const metodos = await metodoPagoService.getAll();
        setMetodosPago(metodos);
        if (metodos.length > 0) {
          setMetodoPagoSeleccionado(metodos[0].id);
        }
      } catch (error) {
        console.error('Error al cargar métodos de pago:', error);
      }
    };

    cargarMetodosPago();
  }, []);

  const confirmarPago = async () => {
    if (!metodoPagoSeleccionado) {
      alert('Por favor seleccione un método de pago');
      return;
    }

    try {
      setIsLoading(true);

      // Obtener dinámicamente el primer usuario disponible
      const usuarioId = await inventarioService.getFirstAvailableUser();

      const request: FinalizarVentaRequest = {
        metodoPagoId: metodoPagoSeleccionado,
        usuarioId: usuarioId,
        clienteId: undefined // Por ahora sin cliente específico
      };

      const ventaFinalizada = await workspaceService.finalizarVenta(ticket.workspaceId, request);
      onVentaFinalizada(ventaFinalizada);

    } catch (error) {
      console.error('Error al finalizar venta:', error);
      alert('Error al procesar el pago. Por favor, intente nuevamente.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="ticket-venta-overlay">
      <div className="ticket-venta-modal">
        <div className="ticket-header">
          <h2>🧾 Ticket de Venta</h2>
          <button className="btn-cerrar" onClick={onCerrar} disabled={isLoading}>
            ✕
          </button>
        </div>

        <div className="ticket-content">
          <div className="workspace-info">
            <h3>{ticket.workspaceNombre}</h3>
            <p>ID: {ticket.workspaceId}</p>
          </div>

          <div className="productos-lista">
            <div className="productos-header">
              <span>Producto</span>
              <span>Cantidad</span>
              <span>Precio Unit.</span>
              <span>Total</span>
            </div>
            
            {ticket.productos.map((producto, index) => (
              <div key={index} className="producto-item">
                <span className="producto-nombre">{producto.productoNombre}</span>
                <span className="producto-cantidad">
                  {producto.cantidadPz > 0 && `${producto.cantidadPz} pz`}
                  {producto.cantidadKg > 0 && `${producto.cantidadKg} kg`}
                </span>
                <span className="producto-precio">${producto.precioUnitario.toFixed(2)}</span>
                <span className="producto-total">${producto.totalPorProducto.toFixed(2)}</span>
              </div>
            ))}
          </div>

          <div className="ticket-resumen">
            <div className="resumen-item">
              <span>Productos:</span>
              <span>{ticket.cantidadProductos}</span>
            </div>
            <div className="resumen-item total">
              <span>Total:</span>
              <span>${ticket.totalGeneral.toFixed(2)}</span>
            </div>
          </div>

          <div className="ticket-acciones">
            {!mostrarModalPago ? (
              <>
                <button 
                  className="btn btn-secondary" 
                  onClick={onCerrar}
                  disabled={isLoading}
                >
                  Cancelar
                </button>
                <button 
                  className="btn btn-primary" 
                  onClick={() => setMostrarModalPago(true)}
                  disabled={isLoading}
                >
                  Procesar Pago
                </button>
              </>
            ) : (
              <div className="modal-pago">
                <h4>Seleccionar Método de Pago</h4>
                <select 
                  value={metodoPagoSeleccionado} 
                  onChange={(e) => setMetodoPagoSeleccionado(e.target.value)}
                  disabled={isLoading}
                >
                  {metodosPago.map(metodo => (
                    <option key={metodo.id} value={metodo.id}>
                      {metodo.metodoPago}
                    </option>
                  ))}
                </select>
                
                <div className="modal-pago-acciones">
                  <button 
                    className="btn btn-secondary" 
                    onClick={() => setMostrarModalPago(false)}
                    disabled={isLoading}
                  >
                    Volver
                  </button>
                  <button 
                    className="btn btn-success" 
                    onClick={confirmarPago}
                    disabled={isLoading || !metodoPagoSeleccionado}
                  >
                    {isLoading ? 'Procesando...' : 'Confirmar Pago'}
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default TicketVenta;
