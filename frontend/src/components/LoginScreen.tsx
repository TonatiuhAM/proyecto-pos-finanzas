import React, { useState } from 'react';
import { authService } from '../services/apiService';
import type { UsuarioDTO } from '../types/index';
import './LoginScreen.css';

interface LoginScreenProps {
  onLoginSuccess: (usuario: UsuarioDTO) => void;
}

const LoginScreen: React.FC<LoginScreenProps> = ({ onLoginSuccess }) => {
  const [credentials, setCredentials] = useState({
    nombre: '',
    contrasena: ''
  });
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setCredentials(prev => ({
      ...prev,
      [name]: value
    }));
    setError(null);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!credentials.nombre || !credentials.contrasena) {
      setError('Por favor, ingresa usuario y contraseña');
      return;
    }

    setIsLoading(true);
    setError(null);

    try {
      const loginResponse = await authService.login(credentials);
      onLoginSuccess(loginResponse.usuario);
    } catch (error: any) {
      if (error.response?.data) {
        setError(typeof error.response.data === 'string' ? error.response.data : 'Error de autenticación');
      } else {
        setError('Error de conexión');
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="login-screen">
      <div className="login-screen__background">
        <div className="login-screen__background-pattern"></div>
      </div>
      
      <div className="login-screen__container">
        {/* Logo and Brand */}
        <div className="login-screen__header">
          <div className="login-screen__logo">
            <div className="login-screen__logo-icon">
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
              </svg>
            </div>
          </div>
          <h1 className="md-headline-medium login-screen__title">Sistema POS</h1>
          <p className="md-body-large login-screen__subtitle">Punto de Venta Moderno</p>
        </div>

        {/* Login Card */}
        <div className="login-screen__card">
          <div className="login-screen__card-header">
            <h2 className="md-title-large">Iniciar Sesión</h2>
            <p className="md-body-medium">Ingresa tus credenciales para continuar</p>
          </div>

          <form onSubmit={handleSubmit} className="login-screen__form">
            {/* Username Field */}
            <div className="login-screen__field">
              <label htmlFor="nombre" className="login-screen__label md-body-medium">
                Usuario
              </label>
              <div className="login-screen__input-container">
                <svg className="login-screen__input-icon" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
                </svg>
                <input
                  type="text"
                  id="nombre"
                  name="nombre"
                  value={credentials.nombre}
                  onChange={handleInputChange}
                  className="login-screen__input md-body-large"
                  placeholder="Ingresa tu usuario"
                  disabled={isLoading}
                  required
                />
              </div>
            </div>

            {/* Password Field */}
            <div className="login-screen__field">
              <label htmlFor="contrasena" className="login-screen__label md-body-medium">
                Contraseña
              </label>
              <div className="login-screen__input-container">
                <svg className="login-screen__input-icon" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M18,8h-1V6c0-2.76-2.24-5-5-5S7,3.24,7,6v2H6c-1.1,0-2,0.9-2,2v10c0,1.1,0.9,2,2,2h12c1.1,0,2-0.9,2-2V10C20,8.9,19.1,8,18,8z M12,17c-1.1,0-2-0.9-2-2s0.9-2,2-2s2,0.9,2,2S13.1,17,12,17z M15.1,8H8.9V6c0-1.71,1.39-3.1,3.1-3.1s3.1,1.39,3.1,3.1V8z"/>
                </svg>
                <input
                  type="password"
                  id="contrasena"
                  name="contrasena"
                  value={credentials.contrasena}
                  onChange={handleInputChange}
                  className="login-screen__input md-body-large"
                  placeholder="Ingresa tu contraseña"
                  disabled={isLoading}
                  required
                />
              </div>
            </div>

            {/* Error Message */}
            {error && (
              <div className="login-screen__error" role="alert">
                <svg className="login-screen__error-icon" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M12,2C17.53,2 22,6.47 22,12C22,17.53 17.53,22 12,22C6.47,22 2,17.53 2,12C2,6.47 6.47,2 12,2M15.59,7L12,10.59L8.41,7L7,8.41L10.59,12L7,15.59L8.41,17L12,13.41L15.59,17L17,15.59L13.41,12L17,8.41L15.59,7Z"/>
                </svg>
                <span className="md-body-medium">{error}</span>
              </div>
            )}

            {/* Submit Button */}
            <button
              type="submit"
              disabled={isLoading}
              className="md-button md-button--filled login-screen__submit-btn"
            >
              {isLoading && (
                <svg className="login-screen__loading-icon" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M12,4V2A10,10 0 0,0 2,12H4A8,8 0 0,1 12,4Z"/>
                </svg>
              )}
              <span>{isLoading ? 'Iniciando sesión...' : 'Iniciar Sesión'}</span>
            </button>
          </form>
        </div>

        {/* Footer */}
        <div className="login-screen__footer">
          <p className="md-body-small">© 2024 Sistema POS. Todos los derechos reservados.</p>
        </div>
      </div>
    </div>
  );
};

export default LoginScreen;
