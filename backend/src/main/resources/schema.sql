-- Script de migración simplificado - SQL estándar solamente
-- Ejecuta operaciones de manera segura

-- 1. Crear la columna clave_movimiento solo si no existe (usando CREATE COLUMN IF NOT EXISTS)
ALTER TABLE movimientos_inventarios 
ADD COLUMN IF NOT EXISTS clave_movimiento VARCHAR(255);

-- 2. Actualizar valores NULL en clave_movimiento
UPDATE movimientos_inventarios 
SET clave_movimiento = 'MIGRATION-' || id
WHERE clave_movimiento IS NULL;

-- 3. Actualizar valores NULL en inventarios
UPDATE inventarios 
SET cantidad_pz = 0 
WHERE cantidad_pz IS NULL;

UPDATE inventarios 
SET cantidad_kg = 0 
WHERE cantidad_kg IS NULL;
