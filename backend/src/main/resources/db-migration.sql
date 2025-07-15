-- Script para corregir problemas de esquema en la base de datos
-- Este script se ejecutará automáticamente al inicializar el backend

-- 1. Corregir valores NULL en clave_movimiento y asegurar que no sea NULL
UPDATE movimientos_inventarios 
SET clave_movimiento = CONCAT('MIGRATION-', id)
WHERE clave_movimiento IS NULL;

-- 2. Corregir tipos de datos en la tabla inventarios
-- Primero, asegurar que los valores sean convertibles a integer
UPDATE inventarios SET cantidad_pz = 0 WHERE cantidad_pz IS NULL OR cantidad_pz = '';
UPDATE inventarios SET cantidad_kg = 0 WHERE cantidad_kg IS NULL OR cantidad_kg = '';

-- Cambiar tipos de datos usando USING para conversión explícita
ALTER TABLE inventarios ALTER COLUMN cantidad_pz TYPE integer USING cantidad_pz::integer;
ALTER TABLE inventarios ALTER COLUMN cantidad_kg TYPE integer USING cantidad_kg::integer;

-- 3. Asegurar que la columna clave_movimiento no permita NULL en el futuro
-- (Esto lo hará Hibernate automáticamente después de que se ejecute este script)
