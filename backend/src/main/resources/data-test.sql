-- Datos de prueba para sistema de compras
-- Se ejecuta después de la creación de tablas por Hibernate

-- Insertar estados
INSERT INTO estados (id, estado) VALUES 
('activo-id', 'Activo'),
('inactivo-id', 'Inactivo');

-- Insertar categorías de personas  
INSERT INTO categoria_personas (id, categoria) VALUES 
('proveedor-cat-id', 'Proveedor'),
('cliente-cat-id', 'Cliente');

-- Insertar los 4 proveedores reales del sistema
INSERT INTO personas (id, nombre, apellido_paterno, apellido_materno, telefono, email, estados_id, categoria_personas_id) VALUES 
('prov-jaime', 'Jaime', 'Aguilar', '', '555-1001', 'jaime.aguilar@email.com', 'activo-id', 'proveedor-cat-id'),
('prov-elias', 'Elias', 'Bless', '', '555-1002', 'elias.bless@email.com', 'activo-id', 'proveedor-cat-id'),
('prov-1', 'Distribuidora', 'Mexicana', 'SA', '555-0001', 'ventas@dismex.com', 'activo-id', 'proveedor-cat-id'),
('prov-2', 'Productos', 'del Norte', 'LTDA', '555-0002', 'contacto@prodnorte.com', 'activo-id', 'proveedor-cat-id');

-- Insertar métodos de pago
INSERT INTO metodos_pago (id, metodo_pago) VALUES 
('efectivo-id', 'Efectivo'),
('transferencia-id', 'Transferencia'),
('cheque-id', 'Cheque');