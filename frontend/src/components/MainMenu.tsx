import React from 'react';
import type { UsuarioDTO } from '../types';
import './MainMenu.css';

interface MainMenuProps {
  usuario: UsuarioDTO;
  onPuntoDeVentaClick: () => void;
  onInventarioClick: () => void;
  onFinanzasClick: () => void;
  onEmpleadosClick: () => void;
  onLogout: () => void;
}

const MainMenu: React.FC<MainMenuProps> = ({ 
  usuario, 
  onPuntoDeVentaClick, 
  onInventarioClick,
  onFinanzasClick,
  onEmpleadosClick,
  onLogout 
}) => {
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
              <span className="md-body-medium main-menu__user-name">Hola, {usuario.nombre}</span>
              <span className="md-body-small main-menu__user-role">{usuario.rolesRoles || 'Usuario'}</span>
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

      {/* Main Content */}
      <main className="main-menu__content">
        <div className="main-menu__welcome">
          <h2 className="md-headline-medium">¡Bienvenido de vuelta!</h2>
          <p className="md-body-large">¿Qué deseas hacer hoy?</p>
        </div>

        {/* Action Cards Grid */}
        <div className="main-menu__grid">
          
          {/* Punto de Venta Card */}
          <button
            onClick={onPuntoDeVentaClick}
            className="main-menu__card main-menu__card--pos"
            aria-label="Abrir Punto de Venta"
          >
            <div className="main-menu__card-icon">
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path d="M7 18c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zM1 2v2h2l3.6 7.59-1.35 2.45c-.16.28-.25.61-.25.96 0 1.1.9 2 2 2h12v-2H7.42c-.14 0-.25-.11-.25-.25l.03-.12L8.1 13h7.45c.75 0 1.41-.41 1.75-1.03L21.7 4H5.21l-.94-2H1zm16 16c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z"/>
              </svg>
            </div>
            <div className="main-menu__card-content">
              <h3 className="md-title-large">Punto de Venta</h3>
              <p className="md-body-medium">Procesa ventas y gestiona transacciones con clientes</p>
            </div>
            <div className="main-menu__card-arrow">
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path d="M8.59 16.59L13.17 12 8.59 7.41 10 6l6 6-6 6-1.41-1.41z"/>
              </svg>
            </div>
          </button>

          {/* Inventario Card */}
          <button
            onClick={onInventarioClick}
            className="main-menu__card main-menu__card--inventory"
            aria-label="Abrir Inventario"
          >
            <div className="main-menu__card-icon">
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path d="M20 8l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 8m8 4v10M4 8v10l8 4"/>
              </svg>
            </div>
            <div className="main-menu__card-content">
              <h3 className="md-title-large">Inventario</h3>
              <p className="md-body-medium">Controla productos, stock y ubicaciones de almacén</p>
            </div>
            <div className="main-menu__card-arrow">
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path d="M8.59 16.59L13.17 12 8.59 7.41 10 6l6 6-6 6-1.41-1.41z"/>
              </svg>
            </div>
          </button>

          {/* Finanzas Card */}
          <button
            onClick={onFinanzasClick}
            className="main-menu__card main-menu__card--finance"
            aria-label="Abrir Finanzas"
          >
            <div className="main-menu__card-icon">
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path d="M9 17H7v-7h2v7zm4 0h-2V7h2v10zm4 0h-2v-4h2v4zm2 2H5V5h14v14zm0-16H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2z"/>
              </svg>
            </div>
            <div className="main-menu__card-content">
              <h3 className="md-title-large">Finanzas</h3>
              <p className="md-body-medium">Visualiza reportes, estadísticas y análisis financiero</p>
            </div>
            <div className="main-menu__card-arrow">
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path d="M8.59 16.59L13.17 12 8.59 7.41 10 6l6 6-6 6-1.41-1.41z"/>
              </svg>
            </div>
          </button>

          {/* Empleados Card */}
          <button
            onClick={onEmpleadosClick}
            className="main-menu__card main-menu__card--employees"
            aria-label="Abrir Gestión de Empleados"
          >
            <div className="main-menu__card-icon">
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path d="M16 4c0-1.11.89-2 2-2s2 .89 2 2-.89 2-2 2-2-.89-2-2zm4 18v-6h2.5l-2.54-7.63A3.001 3.001 0 0 0 17 6c-1.06 0-2.05.55-2.6 1.46L9.8 14h2.2v8H4v-8h2.5L8 10.5V9H6V7h8v2H12v1.5l1.5 4.5H16v8h4zM12.5 11.5c.83 0 1.5-.67 1.5-1.5s-.67-1.5-1.5-1.5S11 9.17 11 10s.67 1.5 1.5 1.5zm-6 0C7.33 11.5 8 10.83 8 10s-.67-1.5-1.5-1.5S5 9.17 5 10s.67 1.5 1.5 1.5z"/>
              </svg>
            </div>
            <div className="main-menu__card-content">
              <h3 className="md-title-large">Empleados</h3>
              <p className="md-body-medium">Gestiona usuarios, roles y permisos del sistema</p>
            </div>
            <div className="main-menu__card-arrow">
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path d="M8.59 16.59L13.17 12 8.59 7.41 10 6l6 6-6 6-1.41-1.41z"/>
              </svg>
            </div>
          </button>

        </div>

        {/* User Profile Card */}
        <div className="main-menu__profile-card">
          <div className="main-menu__profile-avatar">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
            </svg>
          </div>
          <div className="main-menu__profile-info">
            <h4 className="md-title-medium">{usuario.nombre}</h4>
            <p className="md-body-medium main-menu__profile-role">{usuario.rolesRoles || 'Usuario'}</p>
            {usuario.telefono && (
              <p className="md-body-small main-menu__profile-phone">📞 {usuario.telefono}</p>
            )}
          </div>
        </div>
      </main>
    </div>
  );
};

export default MainMenu;
