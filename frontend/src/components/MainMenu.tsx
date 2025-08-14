import React from 'react';
import { useAuth } from '../hooks/useAuth';
import RoleBasedNavigation from './RoleBasedNavigation';
import './MainMenu.css';

interface MainMenuProps {
  onPuntoDeVentaClick: () => void;
  onInventarioClick: () => void;
  onEmpleadosClick: () => void;
  onLogout: () => void;
}

const MainMenu: React.FC<MainMenuProps> = ({ 
  onPuntoDeVentaClick, 
  onInventarioClick,
  onEmpleadosClick,
  onLogout 
}) => {
  const { getUserName, getUserRole } = useAuth();

  // Función para manejar navegación desde RoleBasedNavigation
  const handleNavigation = (view: string) => {
    switch (view) {
      case 'pdv':
        onPuntoDeVentaClick();
        break;
      case 'inventario':
        onInventarioClick();
        break;
      case 'empleados':
        onEmpleadosClick();
        break;
      default:
        console.warn('Vista no reconocida:', view);
    }
  };

  return (
    <div className="main-menu">
      {/* Top App Bar */}
      <header className="main-menu__header">
        <div className="main-menu__header-content">
          <div className="main-menu__brand">
            <div className="main-menu__logo">
              <svg viewBox="0 0 24 24" fill="currentColor" className="main-menu__logo-icon">
                <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
              </svg>
            </div>
            <div className="main-menu__brand-text">
              <h1 className="md-title-large">Sistema POS</h1>
              <span className="md-body-small main-menu__subtitle">Punto de Venta Moderno</span>
            </div>
          </div>
          
          <div className="main-menu__user-section">
            <div className="main-menu__user-info">
              <span className="md-body-medium main-menu__user-name">
                Hola, {getUserName() || 'Usuario'}
              </span>
              <span className="md-body-small main-menu__user-role">
                {getUserRole() || 'Usuario'}
              </span>
            </div>
            <button
              onClick={onLogout}
              className="md-button md-button--outlined main-menu__logout-btn"
              aria-label="Cerrar sesión"
            >
              <svg viewBox="0 0 24 24" fill="currentColor" className="main-menu__logout-icon">
                <path d="M17 7l-1.41 1.41L18.17 11H8v2h10.17l-2.58 2.59L17 17l5-5zM4 5h8V3H4c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h8v-2H4V5z"/>
              </svg>
              <span className="main-menu__logout-text">Salir</span>
            </button>
          </div>
        </div>
      </header>

      {/* Main Content con navegación basada en roles */}
      <main className="main-menu__content">
        <RoleBasedNavigation onNavigation={handleNavigation} />
      </main>
    </div>
  );
};

export default MainMenu;
