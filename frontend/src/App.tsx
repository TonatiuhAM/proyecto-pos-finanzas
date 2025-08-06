import { useState } from 'react';
import { ToastContainer } from 'react-toastify';
import LoginScreen from './components/LoginScreen';
import MainMenu from './components/MainMenu';
import WorkspaceScreen from './components/WorkspaceScreen';
import Inventario from './components/Inventario';
import PuntoDeVenta from './components/PuntoDeVenta';
import type { UsuarioDTO } from './types/index';
import './App.css';
import 'react-toastify/dist/ReactToastify.css';

type AppState = 'login' | 'main-menu' | 'workspaces' | 'inventario' | 'finanzas' | 'punto-de-venta';

function App() {
  const [appState, setAppState] = useState<AppState>('login');
  const [currentUser, setCurrentUser] = useState<UsuarioDTO | null>(null);
  const [selectedWorkspaceId, setSelectedWorkspaceId] = useState<string>('');

  const handleLoginSuccess = (usuario: UsuarioDTO) => {
    setCurrentUser(usuario);
    // Todos los usuarios van al menú principal
    setAppState('main-menu');
  };

  const handleLogout = () => {
    setCurrentUser(null);
    setAppState('login');
  };

  const handlePuntoDeVentaClick = () => {
    setAppState('workspaces');
  };

  const handleInventarioClick = () => {
    setAppState('inventario');
  };

  const handleFinanzasClick = () => {
    setAppState('finanzas');
  };

  const handleBackToMainMenu = () => {
    setAppState('main-menu');
  };

  const handleWorkspaceSelect = (workspaceId: string) => {
    // Guardar el workspace seleccionado y navegar al punto de venta
    setSelectedWorkspaceId(workspaceId);
    setAppState('punto-de-venta');
  };

  const handleBackToWorkspaces = () => {
    setAppState('workspaces');
  };

  switch (appState) {
    case 'login':
      return <LoginScreen onLoginSuccess={handleLoginSuccess} />;
    
    case 'main-menu':
      if (!currentUser) return <LoginScreen onLoginSuccess={handleLoginSuccess} />;
      return (
        <MainMenu 
          usuario={currentUser}
          onPuntoDeVentaClick={handlePuntoDeVentaClick}
          onInventarioClick={handleInventarioClick}
          onFinanzasClick={handleFinanzasClick}
          onLogout={handleLogout}
        />
      );
    
    case 'workspaces':
      if (!currentUser) return <LoginScreen onLoginSuccess={handleLoginSuccess} />;
      return (
        <WorkspaceScreen 
          onWorkspaceSelect={handleWorkspaceSelect}
          onBackToMainMenu={handleBackToMainMenu}
        />
      );
    
    case 'punto-de-venta':
      if (!currentUser) return <LoginScreen onLoginSuccess={handleLoginSuccess} />;
      return (
        <PuntoDeVenta
          workspaceId={selectedWorkspaceId}
          onBackToWorkspaces={handleBackToWorkspaces}
        />
      );
    
    case 'inventario':
      return (
        <div className="inventory-screen">
          {/* Header */}
          <header className="inventory-screen__header">
            <div className="inventory-screen__header-content">
              <div className="inventory-screen__nav">
                <button
                  onClick={handleBackToMainMenu}
                  className="md-button md-button--outlined inventory-screen__back-btn"
                  aria-label="Volver al menú principal"
                >
                  <svg className="inventory-screen__back-icon" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z"/>
                  </svg>
                  <span>Volver</span>
                </button>
                <div className="inventory-screen__title-section">
                  <h1 className="md-headline-medium">Inventario</h1>
                  <p className="md-body-medium">Gestión de productos y stock</p>
                </div>
              </div>
            </div>
          </header>

          {/* Main Content */}
          <main className="inventory-screen__main">
            <Inventario />
          </main>
        </div>
      );
    
    case 'finanzas':
      return (
        <div className="min-h-screen bg-gray-50">
          {/* Header */}
          <header className="bg-white shadow-sm border-b border-gray-200">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
              <div className="flex justify-between items-center h-16">
                <div className="flex items-center">
                  <button
                    onClick={handleBackToMainMenu}
                    className="mr-4 inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                  >
                    ← Volver al Menú
                  </button>
                  <h1 className="text-xl font-semibold text-gray-900">Finanzas</h1>
                </div>
                <button
                  onClick={handleLogout}
                  className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-red-600 hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500"
                >
                  Cerrar Sesión
                </button>
              </div>
            </div>
          </header>

          {/* Main Content */}
          <main className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
            <div className="px-4 py-6 sm:px-0">
              <div className="text-center">
                <div className="mx-auto h-12 w-12 text-gray-400">
                  <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" className="h-12 w-12">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                  </svg>
                </div>
                <h3 className="mt-2 text-sm font-medium text-gray-900">Módulo de Finanzas</h3>
                <p className="mt-1 text-sm text-gray-500">
                  Esta funcionalidad estará disponible próximamente.
                </p>
              </div>
            </div>
          </main>
        </div>
      );
    
    default:
      return <LoginScreen onLoginSuccess={handleLoginSuccess} />;
  }
}

function AppWithToast() {
  return (
    <>
      <App />
      <ToastContainer 
        position="top-right"
        autoClose={8000}
        hideProgressBar={false}
        newestOnTop={false}
        closeOnClick
        rtl={false}
        pauseOnFocusLoss
        draggable
        pauseOnHover
        style={{ zIndex: 99999 }}
        toastStyle={{
          fontSize: '16px',
          fontWeight: '500',
          padding: '16px',
          borderRadius: '8px',
          boxShadow: '0 8px 32px rgba(0, 0, 0, 0.3)',
          minHeight: '80px',
        }}
      />
    </>
  );
}

export default AppWithToast;
