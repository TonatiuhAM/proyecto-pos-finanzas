import React, { useState } from 'react';
import { authService } from '../services/apiService';
import type { UsuarioDTO } from '../types/index';

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
    <div style={{ padding: '20px', maxWidth: '400px', margin: '0 auto' }}>
      <h1>Sistema POS</h1>
      <h2>Iniciar Sesión</h2>
      
      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
        <input
          type="text"
          name="nombre"
          placeholder="Usuario"
          value={credentials.nombre}
          onChange={handleInputChange}
          style={{ padding: '10px', borderRadius: '4px', border: '1px solid #ccc' }}
        />
        
        <input
          type="password"
          name="contrasena"
          placeholder="Contraseña"
          value={credentials.contrasena}
          onChange={handleInputChange}
          style={{ padding: '10px', borderRadius: '4px', border: '1px solid #ccc' }}
        />
        
        {error && (
          <div style={{ color: 'red', fontSize: '14px' }}>
            {error}
          </div>
        )}
        
        <button
          type="submit"
          disabled={isLoading}
          style={{
            padding: '10px',
            backgroundColor: '#007bff',
            color: 'white',
            border: 'none',
            borderRadius: '4px',
            cursor: isLoading ? 'not-allowed' : 'pointer'
          }}
        >
          {isLoading ? 'Iniciando sesión...' : 'Iniciar Sesión'}
        </button>
      </form>
    </div>
  );
};

export default LoginScreen;
