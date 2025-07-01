-- Script para crear datos de prueba
-- Asegúrate de que existan roles y estados primero

-- Insertar algunos roles si no existen
INSERT INTO roles (id, roles) VALUES 
('rol-empleado', 'Empleado'),
('rol-gerente', 'Gerente'),
('rol-admin', 'Administrador')
ON CONFLICT (id) DO NOTHING;

-- Insertar algunos estados si no existen
INSERT INTO estados (id, estado) VALUES 
('est-activo', 'Activo'),
('est-inactivo', 'Inactivo')
ON CONFLICT (id) DO NOTHING;

-- Crear usuarios de prueba
INSERT INTO usuarios (id, nombre, contrasena, telefono, roles_id, estados_id) VALUES 
('usr-empleado1', 'empleado1', 'password123', '1234567890', 'rol-empleado', 'est-activo'),
('usr-empleado2', 'empleado2', 'password123', '1234567891', 'rol-empleado', 'est-activo'),
('usr-gerente1', 'gerente1', 'password123', '1234567892', 'rol-gerente', 'est-activo'),
('usr-admin1', 'admin1', 'password123', '1234567893', 'rol-admin', 'est-activo')
ON CONFLICT (id) DO NOTHING;
