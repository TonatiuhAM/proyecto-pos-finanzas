import React, { useState, useEffect } from 'react';
import { workspaceService } from '../services/apiService';
import type { WorkspaceStatus } from '../types/index';
import './WorkspaceScreen.css';

interface WorkspaceScreenProps {
  onWorkspaceSelect: (workspaceId: string) => void;
  onBackToMainMenu?: () => void;
}

const WorkspaceScreen: React.FC<WorkspaceScreenProps> = ({ 
  onWorkspaceSelect, 
  onBackToMainMenu 
}) => {
  const [workspaces, setWorkspaces] = useState<WorkspaceStatus[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [newWorkspaceName, setNewWorkspaceName] = useState('');
  const [isPermanent, setIsPermanent] = useState(false);
  const [isCreating, setIsCreating] = useState(false);

  const loadWorkspaces = async () => {
    try {
      setIsLoading(true);
      const data = await workspaceService.getAllWithStatus();
      setWorkspaces(data);
      setError(null);
    } catch (error) {
      setError('Error al cargar los workspaces');
      console.error('Error loading workspaces:', error);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadWorkspaces();
  }, []);

  const handleCreateWorkspace = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!newWorkspaceName.trim()) {
      return;
    }

    setIsCreating(true);
    
    try {
      await workspaceService.create({
        nombre: newWorkspaceName.trim(),
        permanente: isPermanent
      });
      
      // Recargar la lista de workspaces
      await loadWorkspaces();
      
      // Resetear el modal
      setShowCreateModal(false);
      setNewWorkspaceName('');
      setIsPermanent(false);
    } catch (error) {
      setError('Error al crear el workspace');
      console.error('Error creating workspace:', error);
    } finally {
      setIsCreating(false);
    }
  };

  const getStatusInfo = (estado: string) => {
    switch (estado) {
      case 'disponible':
        return {
          color: 'success',
          text: 'Disponible',
          icon: (
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"/>
            </svg>
          )
        };
      case 'ocupado':
        return {
          color: 'error',
          text: 'Ocupada',
          icon: (
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
            </svg>
          )
        };
      case 'cuenta':
        return {
          color: 'warning',
          text: 'Cuenta',
          icon: (
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M11,9H13V7H11M12,20C7.59,20 4,16.41 4,12C4,7.59 7.59,4 12,4C16.41,4 20,7.59 20,12C20,16.41 16.41,20 12,20M12,2A10,10 0 0,0 2,12A10,10 0 0,0 12,22A10,10 0 0,0 22,12A10,10 0 0,0 12,2M11,17H13V11H11V17Z"/>
            </svg>
          )
        };
      default:
        return {
          color: 'success',
          text: 'Disponible',
          icon: (
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"/>
            </svg>
          )
        };
    }
  };

  const handleBackToMainMenu = () => {
    if (onBackToMainMenu) {
      onBackToMainMenu();
    } else {
      window.history.back();
    }
  };

  const handleClearAccounts = async () => {
    if (window.confirm('¿Estás seguro de que deseas eliminar todos los workspaces temporales? Esta acción no se puede deshacer.')) {
      try {
        // Filtrar workspaces temporales (no permanentes)
        const temporaryWorkspaces = workspaces.filter(ws => !ws.permanente);
        
        // Eliminar cada workspace temporal
        for (const workspace of temporaryWorkspaces) {
          await workspaceService.delete(workspace.id);
        }
        
        // Recargar la lista
        await loadWorkspaces();
      } catch (error) {
        setError('Error al limpiar cuentas temporales');
        console.error('Error clearing temporary workspaces:', error);
      }
    }
  };

  if (isLoading) {
    return (
      <div className="workspace-screen workspace-screen--loading">
        <div className="workspace-screen__loading">
          <div className="workspace-screen__loading-spinner">
            <svg className="workspace-screen__loading-icon" viewBox="0 0 24 24" fill="currentColor">
              <path d="M12,4V2A10,10 0 0,0 2,12H4A8,8 0 0,1 12,4Z"/>
            </svg>
          </div>
          <h2 className="md-title-large">Cargando workspaces...</h2>
        </div>
      </div>
    );
  }

  return (
    <div className="workspace-screen">
      {/* Header */}
      <header className="workspace-screen__header">
        <div className="workspace-screen__header-content">
          <div className="workspace-screen__nav">
            <button
              onClick={handleBackToMainMenu}
              className="md-button md-button--outlined workspace-screen__back-btn"
              aria-label="Volver al menú principal"
            >
              <svg className="workspace-screen__back-icon" viewBox="0 0 24 24" fill="currentColor">
                <path d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z"/>
              </svg>
              <span>Volver</span>
            </button>
            <div className="workspace-screen__title-section">
              <h1 className="md-headline-medium">Seleccionar Workspace</h1>
              <p className="md-body-medium">Elige un workspace para comenzar a trabajar</p>
            </div>
          </div>
          
          <div className="workspace-screen__actions">
            <button
              onClick={() => setShowCreateModal(true)}
              className="md-button md-button--filled workspace-screen__create-btn"
            >
              <svg className="workspace-screen__create-icon" viewBox="0 0 24 24" fill="currentColor">
                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/>
              </svg>
              <span>Nuevo Workspace</span>
            </button>
            <button
              onClick={handleClearAccounts}
              className="md-button md-button--text workspace-screen__clear-btn"
              aria-label="Limpiar cuentas temporales"
            >
              <svg className="workspace-screen__clear-icon" viewBox="0 0 24 24" fill="currentColor">
                <path d="M19,4H15.5L14.5,3H9.5L8.5,4H5V6H19M6,19A2,2 0 0,0 8,21H16A2,2 0 0,0 18,19V7H6V19Z"/>
              </svg>
              <span className="workspace-screen__clear-text">Limpiar Cuentas</span>
            </button>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="workspace-screen__content">
        {error && (
          <div className="workspace-screen__error" role="alert">
            <svg className="workspace-screen__error-icon" viewBox="0 0 24 24" fill="currentColor">
              <path d="M12,2C17.53,2 22,6.47 22,12C22,17.53 17.53,22 12,22C6.47,22 2,17.53 2,12C2,6.47 6.47,2 12,2M15.59,7L12,10.59L8.41,7L7,8.41L10.59,12L7,15.59L8.41,17L12,13.41L15.59,17L17,15.59L13.41,12L17,8.41L15.59,7Z"/>
            </svg>
            <span className="md-body-medium">{error}</span>
            <button
              onClick={loadWorkspaces}
              className="md-button md-button--text workspace-screen__retry-btn"
            >
              Reintentar
            </button>
          </div>
        )}

        {workspaces.length === 0 && !error && (
          <div className="workspace-screen__empty">
            <div className="workspace-screen__empty-icon">
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z"/>
              </svg>
            </div>
            <h2 className="md-headline-small">No hay workspaces disponibles</h2>
            <p className="md-body-large">Crea tu primer workspace para comenzar</p>
            <button
              onClick={() => setShowCreateModal(true)}
              className="md-button md-button--filled"
            >
              Crear Workspace
            </button>
          </div>
        )}

        {workspaces.length > 0 && (
          <div className="workspace-screen__grid">
            {workspaces.map((workspace) => {
              const statusInfo = getStatusInfo(workspace.estado);
              const isAvailable = workspace.estado === 'disponible';
              
              return (
                <button
                  key={workspace.id}
                  onClick={() => isAvailable && onWorkspaceSelect(workspace.id)}
                  disabled={!isAvailable}
                  className={`workspace-screen__card workspace-screen__card--${statusInfo.color} ${!isAvailable ? 'workspace-screen__card--disabled' : ''}`}
                  aria-label={`Workspace ${workspace.nombre} - ${statusInfo.text}`}
                >
                  <div className="workspace-screen__card-header">
                    <div className={`workspace-screen__card-status workspace-screen__card-status--${statusInfo.color}`}>
                      {statusInfo.icon}
                      <span className="md-label-medium">{statusInfo.text}</span>
                    </div>
                  </div>
                  
                  <div className="workspace-screen__card-content">
                    <h3 className="md-title-large workspace-screen__card-title">
                      {workspace.nombre}
                    </h3>
                    <div className="workspace-screen__card-meta">
                      <span className="md-body-small">
                        {workspace.permanente ? 'Permanente' : 'Temporal'}
                      </span>
                      {workspace.ultimaActividad && (
                        <span className="md-body-small">
                          Última actividad: {new Date(workspace.ultimaActividad).toLocaleDateString()}
                        </span>
                      )}
                    </div>
                  </div>
                  
                  {isAvailable && (
                    <div className="workspace-screen__card-arrow">
                      <svg viewBox="0 0 24 24" fill="currentColor">
                        <path d="M8.59 16.59L13.17 12 8.59 7.41 10 6l6 6-6 6-1.41-1.41z"/>
                      </svg>
                    </div>
                  )}
                </button>
              );
            })}
          </div>
        )}
      </main>

      {/* Create Modal */}
      {showCreateModal && (
        <div className="workspace-screen__modal-overlay" onClick={() => setShowCreateModal(false)}>
          <div className="workspace-screen__modal" onClick={(e) => e.stopPropagation()}>
            <div className="workspace-screen__modal-header">
              <h2 className="md-title-large">Crear Nuevo Workspace</h2>
              <button
                onClick={() => setShowCreateModal(false)}
                className="workspace-screen__modal-close"
                aria-label="Cerrar modal"
              >
                <svg viewBox="0 0 24 24" fill="currentColor">
                  <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
                </svg>
              </button>
            </div>
            
            <form onSubmit={handleCreateWorkspace} className="workspace-screen__modal-form">
              <div className="workspace-screen__modal-field">
                <label htmlFor="workspaceName" className="md-body-medium">
                  Nombre del Workspace *
                </label>
                <input
                  type="text"
                  id="workspaceName"
                  value={newWorkspaceName}
                  onChange={(e) => setNewWorkspaceName(e.target.value)}
                  className="workspace-screen__modal-input md-body-large"
                  placeholder="Ej: Mesa 1, Punto de Venta Principal"
                  required
                  disabled={isCreating}
                />
              </div>
              
              <div className="workspace-screen__modal-checkbox">
                <label className="workspace-screen__checkbox-label">
                  <input
                    type="checkbox"
                    checked={isPermanent}
                    onChange={(e) => setIsPermanent(e.target.checked)}
                    className="workspace-screen__checkbox"
                    disabled={isCreating}
                  />
                  <span className="workspace-screen__checkbox-indicator"></span>
                  <div className="workspace-screen__checkbox-text">
                    <span className="md-body-medium">Workspace Permanente</span>
                    <span className="md-body-small">Este workspace no se eliminará automáticamente</span>
                  </div>
                </label>
              </div>
              
              <div className="workspace-screen__modal-actions">
                <button
                  type="button"
                  onClick={() => setShowCreateModal(false)}
                  className="md-button md-button--outlined"
                  disabled={isCreating}
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  className="md-button md-button--filled"
                  disabled={isCreating || !newWorkspaceName.trim()}
                >
                  {isCreating && (
                    <svg className="workspace-screen__loading-icon" viewBox="0 0 24 24" fill="currentColor">
                      <path d="M12,4V2A10,10 0 0,0 2,12H4A8,8 0 0,1 12,4Z"/>
                    </svg>
                  )}
                  <span>{isCreating ? 'Creando...' : 'Crear Workspace'}</span>
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default WorkspaceScreen;
