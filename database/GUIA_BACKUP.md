# Guía de Backup y Restauración de Base de Datos PostgreSQL

Esta guía te ayudará a restaurar el backup de tu base de datos PostgreSQL desde DigitalOcean a tu homelab.

## Prerrequisitos

1. **Tener el archivo de backup**: Tu archivo `.pgsql` obtenido de DigitalOcean
2. **Docker y Docker Compose instalados** en tu homelab
3. **El proyecto configurado** con los nuevos servicios

**NOTA**: Esta configuración usa PostgreSQL 18 con la estructura de directorios recomendada (/var/lib/postgresql) que permite upgrades más sencillos.

## Pasos para Restaurar la Base de Datos

### 1. Preparar el entorno

Primero, asegúrate de que el proyecto esté configurado correctamente:

```bash
# Navegar al directorio del proyecto
cd /ruta/a/tu/proyecto-pos-finanzas

# Verificar que exista el directorio de backups
ls -la database/
```

### 2. Copiar el archivo de backup

Copia tu archivo de backup al directorio correspondiente:

```bash
# Copiar el backup al directorio del proyecto
cp /ruta/a/tu/backup.pgsql database/backups/

# Verificar que se copió correctamente
ls -la database/backups/
```

### 3. Iniciar el contenedor de PostgreSQL

```bash
# Iniciar solo el servicio de base de datos
docker-compose up -d database

# Verificar que el contenedor esté ejecutándose
docker-compose ps database

# Verificar los logs del contenedor
docker-compose logs database
```

## Script Automatizado (Recomendado)

Para facilitar la restauración, puedes usar el script automatizado:

```bash
# Copiar tu backup al directorio correcto
cp /ruta/a/tu/backup.pgsql database/backups/

# Ejecutar el script de restauración
./scripts/restaurar-backup.sh backup.pgsql
```

El script se encarga automáticamente de:
- Verificar que el contenedor esté corriendo
- Iniciar PostgreSQL si no está activo
- Ejecutar la restauración con los parámetros correctos
- Verificar que el proceso se completó exitosamente

#### Opción A: Usando docker exec (Recomendado)

```bash
# Conectar al contenedor de PostgreSQL y restaurar
docker exec -i pos_database pg_restore -U postgres -d pos_finanzas -v /backups/backup.pgsql

# O si necesitas más control sobre la restauración:
docker exec -i pos_database pg_restore \\
  --host=localhost \\
  --port=5432 \\
  --username=postgres \\
  --dbname=pos_finanzas \\
  --verbose \\
  --clean \\
  --if-exists \\
  /backups/backup.pgsql
```

#### Opción B: Desde tu host (si tienes pg_restore instalado)

```bash
# Restaurar desde el host usando el puerto expuesto (NOTA: Puerto cambiado a 5433)
pg_restore \
  --host=localhost \
  --port=5433 \
  --username=postgres \
  --dbname=pos_finanzas \
  --verbose \
  --clean \
  --if-exists \
  database/backups/backup.pgsql
```

### 5. Verificar la restauración

```bash
# Conectar a la base de datos para verificar las tablas
docker exec -it pos_database psql -U postgres -d pos_finanzas

# Dentro de PostgreSQL, ejecutar:
\\dt  -- Listar todas las tablas
\\q   -- Salir
```

### 6. Iniciar todos los servicios

```bash
# Iniciar todos los servicios del proyecto
docker-compose up -d

# Verificar que todos los contenedores estén ejecutándose
docker-compose ps

# Verificar los logs de cada servicio
docker-compose logs backend
docker-compose logs frontend
docker-compose logs ml-prediction
```

## Comandos de Troubleshooting

### Si hay problemas con la restauración:

```bash
# Ver logs detallados del contenedor de base de datos
docker-compose logs -f database

# Conectar directamente al contenedor
docker exec -it pos_database bash

# Dentro del contenedor, verificar pg_restore
which pg_restore
pg_restore --version

# Verificar permisos del archivo de backup
ls -la /backups/

# Si hay problemas de permisos:
chmod 644 database/backups/backup.pgsql
```

### Si necesitas recrear la base de datos:

```bash
# Conectar a PostgreSQL como superusuario
docker exec -it pos_database psql -U postgres

# Dentro de PostgreSQL:
DROP DATABASE IF EXISTS pos_finanzas;
CREATE DATABASE pos_finanzas;
\\q

# Luego repetir el proceso de restauración
```

### Verificar conectividad desde el backend:

```bash
# Ver logs del backend para errores de conexión
docker-compose logs backend

# Verificar variables de entorno del backend
docker exec pos_backend env | grep DB_
```

## Configuraciones Adicionales para Homelab

### Actualizar variables de entorno

Asegúrate de que tu archivo `.env` contenga:

```env
DB_NAME=pos_finanzas
DB_USER=postgres
DB_PASS=tu_password_seguro
DB_URL=jdbc:postgresql://database:5432/pos_finanzas
```

### Configurar acceso externo

Si quieres acceder desde fuera de tu red local, actualiza la configuración CORS en `SecurityConfig.java` con tu dominio:

```java
"https://tu-dominio.com:5173",
"http://tu-dominio.com:5173"
```

## Backup Periódico (Recomendado)

Para crear backups regulares de tu base de datos:

```bash
# Crear un backup
docker exec pos_database pg_dump -U postgres pos_finanzas > "database/backups/backup_$(date +%Y%m%d_%H%M%S).sql"

# Crear un backup comprimido
docker exec pos_database pg_dump -U postgres pos_finanzas | gzip > "database/backups/backup_$(date +%Y%m%d_%H%M%S).sql.gz"
```

## URLs de Acceso

Una vez que todo esté funcionando:

- **Frontend**: http://tu-ip-homelab:5173 o https://tu-dominio.com:5173
- **Backend API**: http://tu-ip-homelab:8084 o https://tu-dominio.com:8084
- **ML Service**: http://tu-ip-homelab:8004 o https://tu-dominio.com:8004
- **Base de Datos**: puerto 5433 (desde el host), puerto 5432 (interno entre contenedores)

## Notas Importantes

1. **Seguridad**: Cambia las contraseñas por defecto en el archivo `.env`
2. **Firewall**: Asegúrate de que los puertos necesarios estén abiertos en tu homelab
3. **SSL**: Para acceso público, considera configurar SSL/TLS con Let's Encrypt
4. **Monitoreo**: Configura logs y monitoreo para tu homelab

¡Tu migración debería estar completa siguiendo estos pasos!