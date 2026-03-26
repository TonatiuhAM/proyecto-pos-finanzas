# Sistema de Punto de Venta (POS) y Gestión Integral

> **Proyecto de Titulación** - Ingeniería en Computación  
> Sistema POS con predicción inteligente de compras mediante Machine Learning (XGBoost)

---

## Tabla de Contenidos

- [Descripción](#descripción)
- [Stack Tecnológico](#stack-tecnológico)
- [Arquitectura del Sistema](#arquitectura-del-sistema)
- [Requisitos Previos](#requisitos-previos)
- [Instalación y Configuración](#instalación-y-configuración)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Características Principales](#características-principales)
- [Flujo de Desarrollo](#flujo-de-desarrollo)
- [Estándares de Código](#estándares-de-código)
- [Testing](#testing)
- [Contribución](#contribución)
- [Licencia](#licencia)

---

## Descripción

Sistema integral de Punto de Venta (POS) diseñado para la gestión completa de operaciones comerciales. El diferenciador principal es la integración de un **modelo de Machine Learning (XGBoost)** que analiza patrones históricos de compras para generar predicciones inteligentes de demanda.

### Objetivos del Sistema

- **Gestión de Ventas**: Procesamiento ágil de transacciones con múltiples métodos de pago
- **Control de Inventario**: Seguimiento en tiempo real de stock con alertas de reabastecimiento
- **Predicción de Demanda**: Modelo ML que predice necesidades de compra basándose en historial
- **Gestión Financiera**: Control de cuentas por cobrar/pagar, flujo de caja y reportes
- **Administración de Usuarios**: Sistema de roles y permisos con autenticación JWT

---

## Stack Tecnológico

### Backend
| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| Java | 17 | Lenguaje principal |
| Spring Boot | 3.5.0 | Framework de aplicación |
| Spring Data JPA | - | Persistencia de datos |
| Spring Security | - | Autenticación y autorización |
| PostgreSQL | 15+ | Base de datos relacional |
| Maven | - | Gestión de dependencias |

### Frontend
| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| React | 19 | Biblioteca UI |
| TypeScript | 5.8 | Tipado estático |
| Vite | 6.2 | Build tool y dev server |
| Tailwind CSS | 4.0 | Framework de estilos |
| Material UI (MUI) | 7.0 | Componentes UI |
| TanStack Query | 5.x | Gestión de estado servidor |
| React Router | 7.x | Enrutamiento |
| Zustand | 5.x | Gestión de estado global |

### Servicio de Predicción (ML)
| Tecnología | Propósito |
|------------|-----------|
| Python | Lenguaje del servicio |
| XGBoost | Modelo de predicción |
| FastAPI/Flask | API REST del servicio |
| Pandas/NumPy | Procesamiento de datos |

### Infraestructura
| Tecnología | Propósito |
|------------|-----------|
| Docker | Contenedorización |
| Docker Compose | Orquestación local |

---

## Arquitectura del Sistema

```
┌─────────────────────────────────────────────────────────────────┐
│                         FRONTEND                                │
│                    (React + TypeScript)                         │
│                      Puerto: 5173                               │
└─────────────────────────┬───────────────────────────────────────┘
                          │ HTTP/REST
                          ▼
┌─────────────────────────────────────────────────────────────────┐
│                         BACKEND                                 │
│                 (Spring Boot + Java 17)                         │
│                      Puerto: 8080                               │
│                                                                 │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────────┐ │
│  │ Controllers │──│  Services   │──│     Repositories        │ │
│  │   (REST)    │  │  (Lógica)   │  │  (Spring Data JPA)      │ │
│  └─────────────┘  └─────────────┘  └───────────┬─────────────┘ │
└────────────────────────────────────────────────┼────────────────┘
                          │                      │
                          ▼                      ▼
┌─────────────────────────────────┐  ┌───────────────────────────┐
│      ML PREDICTION SERVICE      │  │        POSTGRESQL         │
│           (Python)              │  │       Puerto: 5432        │
│         Puerto: 5000            │  │                           │
└─────────────────────────────────┘  └───────────────────────────┘
```

### Patrón de Arquitectura Backend

El backend sigue estrictamente el patrón **Controller → Service → Repository**:

- **Controllers**: Manejan solicitudes HTTP y delegan a servicios. NO contienen lógica de negocio.
- **Services**: Contienen TODA la lógica de negocio. Coordinan operaciones entre repositorios.
- **Repositories**: Acceso a datos mediante Spring Data JPA. Métodos personalizados van aquí.

```java
// Ejemplo de flujo
@RestController → @Service → @Repository → Database
```

---

## Requisitos Previos

- **Docker** y **Docker Compose** (recomendado para desarrollo)
- O alternativamente:
  - Java 17+
  - Node.js 18+
  - PostgreSQL 15+
  - Python 3.9+

---

## Instalación y Configuración

### Opción 1: Docker Compose (Recomendado)

```bash
# Clonar el repositorio
git clone <url-repositorio>
cd proyecto-pos-finanzas

# Copiar archivo de variables de entorno
cp .env.example .env

# Levantar todos los servicios
docker-compose up -d

# Ver logs
docker-compose logs -f
```

### Opción 2: Desarrollo Local

#### Backend
```bash
cd backend

# Configurar base de datos en application.properties
# Ejecutar con Maven
./mvnw spring-boot:run
```

#### Frontend
```bash
cd frontend

# Instalar dependencias
npm install

# Iniciar servidor de desarrollo
npm run dev
```

#### Servicio ML
```bash
cd ml-prediction-service

# Crear entorno virtual
python -m venv venv
source venv/bin/activate  # Linux/Mac
# o: venv\Scripts\activate  # Windows

# Instalar dependencias
pip install -r requirements.txt

# Ejecutar servicio
python app.py
```

### Variables de Entorno

Crear archivo `.env` en la raíz del proyecto:

```env
# Base de datos
POSTGRES_DB=pos_finanzas
POSTGRES_USER=usuario
POSTGRES_PASSWORD=contraseña

# Backend
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/pos_finanzas
JWT_SECRET=tu-secreto-jwt

# Frontend
VITE_API_URL=http://localhost:8080
```

---

## Estructura del Proyecto

```
proyecto-pos-finanzas/
│
├── backend/                    # API REST (Spring Boot)
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/tona/posfinanzas/
│   │   │   │   ├── controller/     # Endpoints REST
│   │   │   │   ├── service/        # Lógica de negocio
│   │   │   │   ├── repository/     # Acceso a datos
│   │   │   │   ├── model/          # Entidades JPA
│   │   │   │   ├── dto/            # Objetos de transferencia
│   │   │   │   ├── security/       # Configuración JWT
│   │   │   │   └── config/         # Configuraciones
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── test/                   # Tests unitarios
│   └── pom.xml
│
├── frontend/                   # Aplicación Web (React)
│   ├── src/
│   │   ├── components/         # Componentes reutilizables
│   │   ├── pages/              # Vistas/páginas
│   │   ├── hooks/              # Custom hooks
│   │   ├── services/           # Llamadas API
│   │   ├── stores/             # Estado global (Zustand)
│   │   ├── types/              # Tipos TypeScript
│   │   └── utils/              # Utilidades
│   ├── package.json
│   └── vite.config.ts
│
├── ml-prediction-service/      # Servicio de Predicción
│   ├── models/                 # Modelos entrenados
│   ├── data/                   # Datos de entrenamiento
│   └── app.py                  # API del servicio
│
├── database/                   # Datos PostgreSQL (volumen)
│
├── docs/                       # Documentación
│   ├── requerimientos/         # Especificaciones del sistema
│   ├── testing/                # Documentación de pruebas
│   ├── seguridad.md            # Arquitectura de seguridad
│   └── bd-schema.md            # Esquema de base de datos
│
├── scripts/                    # Scripts de utilidad
├── pruebas/                    # Planes de prueba
├── pruebas-rendimiento/        # Tests de rendimiento
├── reportes-predicciones/      # Reportes del modelo ML
├── analisis-tesis-xgboost/     # Análisis para tesis
│
├── docker-compose.yml          # Orquestación de contenedores
├── docker-compose.override.yml # Configuración local
├── .env                        # Variables de entorno
└── .gitignore
```

---

## Características Principales

### Módulo de Ventas
- Procesamiento de ventas con múltiples productos
- Soporte para múltiples métodos de pago (efectivo, tarjeta, transferencia)
- Generación de comprobantes
- Historial de transacciones

### Módulo de Inventario
- Control de stock en tiempo real
- Alertas de stock mínimo
- Gestión de proveedores
- Historial de movimientos

### Módulo de Predicción (ML)
- Análisis de patrones de compra históricos
- Predicción de demanda por producto
- Recomendaciones de reabastecimiento
- Reportes de precisión del modelo

### Módulo Financiero
- Cuentas por cobrar y pagar
- Flujo de caja
- Reportes financieros
- Cortes de caja

### Módulo de Usuarios
- Autenticación JWT
- Gestión de roles (Admin, Vendedor, Almacenista)
- Permisos granulares
- Auditoría de acciones

---

## Flujo de Desarrollo

### Ramas Git

```
main            ← Producción estable
└── develop     ← Integración de features
    └── feature/nombre-feature  ← Desarrollo de características
    └── fix/nombre-fix          ← Corrección de bugs
```

### Workflow

1. Crear rama desde `develop`
2. Desarrollar y hacer commits atómicos
3. Ejecutar tests localmente
4. Crear Pull Request hacia `develop`
5. Code review y merge

---

## Estándares de Código

### Idioma
> **Español obligatorio** para código, comentarios, commits y documentación.

### Backend (Java/Spring)

```java
// Nomenclatura de clases
public class ProductoService { }      // PascalCase
public class ProductoRepository { }

// Nomenclatura de métodos y variables
public Producto obtenerPorId(Long id) { }  // camelCase
private String nombreProducto;

// DTOs siempre terminan en DTO
public class ProductoDTO { }
public class CrearProductoDTO { }
```

### Frontend (TypeScript/React)

```typescript
// Componentes en PascalCase
export const ProductoCard: React.FC<Props> = () => { }

// Hooks con prefijo 'use'
export const useProductos = () => { }

// Tipos e interfaces
interface ProductoProps { }
type EstadoVenta = 'pendiente' | 'completada';
```

### Commits

```
feat(modulo): descripción breve

- Detalle 1
- Detalle 2
```

Tipos: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`

---

## Testing

### Backend

```bash
cd backend

# Ejecutar todos los tests
./mvnw test

# Test con cobertura
./mvnw test jacoco:report
```

**Framework**: JUnit 5 + Mockito  
**Cobertura actual**: ~41% global, ~89% en servicios

### Frontend

```bash
cd frontend

# Ejecutar tests
npm run test

# Tests con cobertura
npm run test:coverage

# Tests en modo watch
npm run test:watch
```

**Framework**: Vitest + React Testing Library

### Servicio ML

```bash
cd ml-prediction-service

# Ejecutar tests
pytest

# Con cobertura
pytest --cov=.
```

---

## Contribución

1. Fork del repositorio
2. Crear rama de feature (`git checkout -b feature/nueva-caracteristica`)
3. Commits atómicos con mensajes descriptivos
4. Push a tu fork (`git push origin feature/nueva-caracteristica`)
5. Crear Pull Request

### Checklist antes de PR

- [ ] Código sigue los estándares establecidos
- [ ] Tests pasan localmente
- [ ] Sin errores de linting
- [ ] Documentación actualizada si aplica

---

## Licencia

Este proyecto es parte de un trabajo de titulación académico.

---

## Contacto

Para dudas o contribuciones, contactar al equipo de desarrollo.
