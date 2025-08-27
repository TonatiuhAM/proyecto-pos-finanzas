import React from 'react';
import Inventario from './Inventario';
import './InventarioModerno.css';

const TestInventario: React.FC = () => {
  const handleNavigateToCompras = () => {
    console.log('🚀 Navegando a compras!');
    alert('Botón de compras funcionando correctamente!');
  };

  return (
    <div style={{ padding: '20px', backgroundColor: '#f5f5f5', minHeight: '100vh' }}>
      <h1>Test del Componente Inventario</h1>
      <p>Si puedes ver el botón "🛒 COMPRAR PRODUCTO" verde, entonces está funcionando.</p>
      <div style={{ marginTop: '20px', backgroundColor: 'white', padding: '20px', borderRadius: '8px' }}>
        <Inventario onNavigateToCompras={handleNavigateToCompras} />
      </div>
    </div>
  );
};

export default TestInventario;