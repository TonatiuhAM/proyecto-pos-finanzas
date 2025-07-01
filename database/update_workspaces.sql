-- Agregar la columna 'permanente' a la tabla workspaces
-- Ejecutar este script si la tabla ya existe y no tiene la columna permanente

ALTER TABLE workspaces 
ADD COLUMN permanente BOOLEAN NOT NULL DEFAULT FALSE;

-- Crear algunos workspaces de ejemplo para pruebas
INSERT INTO workspaces (id, nombre, permanente) VALUES 
('ws-1', 'Mesa 1', true),
('ws-2', 'Mesa 2', true),
('ws-3', 'Mesa 3', true),
('ws-4', 'Mesa 4', true),
('ws-5', 'Mesa 5', true),
('ws-6', 'Mesa VIP', false),
('ws-7', 'Mesa Terraza', false),
('ws-8', 'Mesa Bar', true)
ON CONFLICT (id) DO NOTHING;
