# Tareas del Proyecto POS Finanzas

## 🧪 ESTRATEGIA COMPLETA: Alcanzar 70% de Cobertura de Pruebas (21 Ene 2026)

### Descripción del Objetivo

Implementar una estrategia integral de testing para alcanzar el objetivo de **70% de cobertura** documentado en el requerimiento no funcional RNF007. Este plan abarca pruebas unitarias, de integración y end-to-end para backend, frontend y servicio ML.

**ESTADO ACTUAL:**
- Backend: ~5% de cobertura (2 archivos de test)
- Frontend: 0% de cobertura (sin framework configurado)
- ML Service: 0% de pruebas unitarias (solo scripts manuales)

**OBJETIVO:**
- Backend: 70% de cobertura mínima
- Frontend: 70% de cobertura mínima
- ML Service: 70% de cobertura mínima

### Análisis de Situación Actual

#### ✅ Aspectos Positivos:
1. **Calidad de Datos ML**: Implementación excelente de ISO/IEC 25012
2. **Infraestructura Base**: H2 configurada, dependencias en `pom.xml`
3. **Ejemplo Funcional**: `DeudasProveedoresServiceTest.java` bien implementado
4. **Scripts ML**: Testing manual funcional para integración

#### ⚠️ Problemas Críticos:
1. **Tests deshabilitados en Backend**: `<skip>true</skip>` en `pom.xml`
2. **Frontend sin framework**: Ni Jest, ni Vitest configurados
3. **Cobertura insuficiente**: 65 puntos porcentuales por debajo del objetivo
4. **Sin pruebas E2E**: Flujos críticos sin verificación automatizada

---

## 📋 PLAN DE IMPLEMENTACIÓN

### FASE 1: Configuración de Infraestructura de Testing (Prioridad: CRÍTICA)

#### Backend: Habilitar y Configurar Testing

- [ ] **Paso 1.1: Habilitar compilación de tests**
  - [ ] Abrir `backend/pom.xml`
  - [ ] Remover o comentar el bloque `<skip>true</skip>` en la configuración de maven-surefire-plugin
  - [ ] Verificar que las dependencias de testing estén presentes:
    - `spring-boot-starter-test`
    - `spring-security-test`
    - `h2` (scope: test)
  - [ ] Ejecutar `./mvnw test` para verificar que los tests existentes corran

- [ ] **Paso 1.2: Configurar JaCoCo para medición de cobertura**
  - [ ] Añadir plugin JaCoCo en `pom.xml`:
    ```xml
    <plugin>
        <groupId>org.jacoco</groupId>
        <artifactId>jacoco-maven-plugin</artifactId>
        <version>0.8.11</version>
        <executions>
            <execution>
                <goals>
                    <goal>prepare-agent</goal>
                </goals>
            </execution>
            <execution>
                <id>report</id>
                <phase>test</phase>
                <goals>
                    <goal>report</goal>
                </goals>
            </execution>
        </executions>
    </plugin>
    ```
  - [ ] Ejecutar `./mvnw clean test jacoco:report`
  - [ ] Verificar que se genere reporte en `target/site/jacoco/index.html`
  - [ ] Documentar cobertura actual como baseline

- [ ] **Paso 1.3: Configurar perfiles de testing**
  - [ ] Verificar que `application-test.properties` esté correctamente configurado
  - [ ] Crear `data-test.sql` con datos de prueba mínimos si no existe
  - [ ] Asegurar que H2 esté configurada en modo PostgreSQL

#### Frontend: Instalar y Configurar Framework de Testing

- [ ] **Paso 1.4: Instalar Vitest y dependencias**
  - [ ] Navegar a `frontend/`
  - [ ] Ejecutar:
    ```bash
    npm install -D vitest @vitest/ui @vitest/coverage-v8
    npm install -D @testing-library/react @testing-library/jest-dom @testing-library/user-event
    npm install -D jsdom
    ```
  - [ ] Verificar que las dependencias se añadan a `package.json`

- [ ] **Paso 1.5: Crear configuración de Vitest**
  - [ ] Crear archivo `frontend/vitest.config.ts`:
    ```typescript
    import { defineConfig } from 'vitest/config'
    import react from '@vitejs/plugin-react'
    
    export default defineConfig({
      plugins: [react()],
      test: {
        globals: true,
        environment: 'jsdom',
        setupFiles: './src/test/setup.ts',
        coverage: {
          provider: 'v8',
          reporter: ['text', 'json', 'html'],
          exclude: [
            'node_modules/',
            'src/test/',
            '**/*.d.ts',
            '**/*.config.*',
            '**/mockData',
            'src/main.tsx',
          ],
          thresholds: {
            lines: 70,
            functions: 70,
            branches: 70,
            statements: 70,
          },
        },
      },
    })
    ```

- [ ] **Paso 1.6: Crear archivo de setup de testing**
  - [ ] Crear carpeta `frontend/src/test/`
  - [ ] Crear archivo `frontend/src/test/setup.ts`:
    ```typescript
    import { expect, afterEach } from 'vitest'
    import { cleanup } from '@testing-library/react'
    import * as matchers from '@testing-library/jest-dom/matchers'
    
    expect.extend(matchers)
    
    afterEach(() => {
      cleanup()
    })
    ```

- [ ] **Paso 1.7: Añadir scripts de testing a package.json**
  - [ ] Añadir en la sección `"scripts"`:
    ```json
    "test": "vitest",
    "test:ui": "vitest --ui",
    "test:coverage": "vitest --coverage",
    "test:run": "vitest run"
    ```

- [ ] **Paso 1.8: Verificar instalación**
  - [ ] Ejecutar `npm test -- --version`
  - [ ] Verificar que Vitest se ejecute sin errores

#### ML Service: Configurar pytest

- [ ] **Paso 1.9: Instalar pytest y dependencias**
  - [ ] Navegar a `ml-prediction-service/`
  - [ ] Añadir a `requirements.txt`:
    ```
    pytest==7.4.3
    pytest-cov==4.1.0
    pytest-asyncio==0.21.1
    httpx==0.25.2
    ```
  - [ ] Ejecutar `pip install -r requirements.txt`

- [ ] **Paso 1.10: Crear configuración de pytest**
  - [ ] Crear archivo `ml-prediction-service/pytest.ini`:
    ```ini
    [pytest]
    testpaths = tests
    python_files = test_*.py
    python_classes = Test*
    python_functions = test_*
    addopts = 
        --verbose
        --cov=app
        --cov-report=html
        --cov-report=term
        --cov-fail-under=70
    ```

- [ ] **Paso 1.11: Crear estructura de carpetas de tests**
  - [ ] Crear carpeta `ml-prediction-service/tests/`
  - [ ] Crear archivo `ml-prediction-service/tests/__init__.py` vacío
  - [ ] Crear carpeta `ml-prediction-service/tests/unit/`
  - [ ] Crear carpeta `ml-prediction-service/tests/integration/`

---

### FASE 2: Pruebas Unitarias Backend (Prioridad: ALTA)

**Objetivo**: Alcanzar 40% de cobertura implementando tests de servicios críticos

#### Services: Capa de Lógica de Negocio

- [ ] **Paso 2.1: Test de InventarioService**
  - [ ] Crear `backend/src/test/java/.../service/InventarioServiceTest.java`
  - [ ] Implementar tests para:
    - `getAllProductos()` - Lista de productos
    - `getProductoById(id)` - Obtener producto por ID
    - `createProducto(dto)` - Crear producto
    - `updateProducto(id, dto)` - Actualizar producto
    - `deleteProducto(id)` - Eliminar (desactivar) producto
    - `getProductosByCategoria(categoria)` - Filtrar por categoría
    - `getProductosConStockBajo()` - Alertas de stock
  - [ ] Usar Mockito para mockear repositories
  - [ ] Verificar manejo de excepciones

- [ ] **Paso 2.2: Test de VentasService / OrdenesDeVentasService**
  - [ ] Crear `OrdenesDeVentasServiceTest.java`
  - [ ] Implementar tests para:
    - `crearOrdenDeVenta(dto)` - Crear orden
    - `agregarProductoAOrden(ordenId, productoId, cantidad)` - Añadir producto
    - `calcularTotalOrden(ordenId)` - Calcular total
    - `finalizarOrden(ordenId)` - Finalizar orden
    - `getOrdenesByUsuario(usuarioId)` - Órdenes por usuario
    - `getOrdenesRecientes(limit)` - Órdenes recientes
  - [ ] Verificar actualización de inventario
  - [ ] Verificar cálculos de totales

- [ ] **Paso 2.3: Test de ComprasService / OrdenesWorkspaceService**
  - [ ] Crear `OrdenesWorkspaceServiceTest.java`
  - [ ] Implementar tests para:
    - `crearOrdenCompra(dto)` - Crear orden de compra
    - `agregarProductoAOrdenWorkspace(workspaceId, productoId, cantidad)` - Añadir producto
    - `incrementarInventario(productoId, cantidad)` - Incrementar stock
    - `finalizarOrdenWorkspace(workspaceId)` - Finalizar orden
  - [ ] Verificar incremento de inventario
  - [ ] Verificar estados de workspace

- [ ] **Paso 2.4: Test de AuthService / UsuariosService**
  - [ ] Crear `UsuariosServiceTest.java`
  - [ ] Implementar tests para:
    - `createUsuario(dto)` - Crear usuario
    - `updateUsuario(id, dto)` - Actualizar usuario
    - `findByNombre(nombre)` - Buscar por nombre
    - `validatePassword(usuario, password)` - Validar contraseña
    - `changePassword(usuarioId, oldPassword, newPassword)` - Cambiar contraseña
  - [ ] Verificar encriptación de contraseñas
  - [ ] Verificar validaciones

- [ ] **Paso 2.5: Test de PersonaService / EmpleadoService**
  - [ ] Crear `PersonaServiceTest.java`
  - [ ] Crear `EmpleadoServiceTest.java`
  - [ ] Implementar tests básicos CRUD

#### Verificación de Cobertura Fase 2

- [ ] **Paso 2.6: Medir cobertura después de tests de servicios**
  - [ ] Ejecutar `./mvnw clean test jacoco:report`
  - [ ] Abrir `target/site/jacoco/index.html`
  - [ ] Verificar que cobertura esté ≥ 40%
  - [ ] Documentar resultados

---

### FASE 3: Pruebas de Integración Backend (Prioridad: ALTA)

**Objetivo**: Alcanzar 55% de cobertura implementando tests de controllers

#### Controllers: Capa de API REST

- [ ] **Paso 3.1: Test de AuthController**
  - [ ] Crear `backend/src/test/java/.../controller/AuthControllerTest.java`
  - [ ] Usar `@SpringBootTest` y `MockMvc`
  - [ ] Implementar tests para:
    - `POST /api/auth/login` - Login exitoso
    - `POST /api/auth/login` - Login con credenciales incorrectas
    - `POST /api/auth/login` - Login con usuario inexistente
    - `POST /api/auth/register` - Registro exitoso
    - `POST /api/auth/register` - Registro con datos inválidos
  - [ ] Verificar respuesta HTTP 200/400/401
  - [ ] Verificar generación de JWT token

- [ ] **Paso 3.2: Test de InventarioController**
  - [ ] Crear `InventarioControllerTest.java`
  - [ ] Implementar tests para:
    - `GET /api/productos` - Obtener todos los productos
    - `GET /api/productos/{id}` - Obtener producto por ID
    - `POST /api/productos` - Crear producto (requiere auth)
    - `PUT /api/productos/{id}` - Actualizar producto (requiere auth)
    - `DELETE /api/productos/{id}` - Eliminar producto (requiere auth)
  - [ ] Verificar validación de DTOs
  - [ ] Verificar seguridad (endpoints protegidos)

- [ ] **Paso 3.3: Test de OrdenesDeVentasController**
  - [ ] Crear `OrdenesDeVentasControllerTest.java`
  - [ ] Implementar tests para endpoints críticos
  - [ ] Verificar flujo completo de venta

- [ ] **Paso 3.4: Test de OrdenesWorkspaceController**
  - [ ] Crear `OrdenesWorkspaceControllerTest.java`
  - [ ] Implementar tests para gestión de workspaces
  - [ ] Verificar estados de ocupación

#### Configuración de Seguridad en Tests

- [ ] **Paso 3.5: Configurar Spring Security Test**
  - [ ] Crear clase de utilidad `TestSecurityConfig.java`
  - [ ] Implementar método para generar tokens JWT de prueba
  - [ ] Crear anotación personalizada `@WithMockJWT` si es necesario

#### Verificación de Cobertura Fase 3

- [ ] **Paso 3.6: Medir cobertura después de tests de controllers**
  - [ ] Ejecutar `./mvnw clean test jacoco:report`
  - [ ] Verificar que cobertura esté ≥ 55%
  - [ ] Identificar áreas con baja cobertura

---

### FASE 4: Pruebas de Repositories Backend (Prioridad: MEDIA)

**Objetivo**: Alcanzar 65% de cobertura implementando tests de consultas JPA

#### Repositories: Capa de Acceso a Datos

- [ ] **Paso 4.1: Test de ProductosRepository**
  - [ ] Crear `ProductosRepositoryTest.java`
  - [ ] Usar `@DataJpaTest` para tests de repository
  - [ ] Implementar tests para:
    - `findByNombre(nombre)` - Buscar por nombre
    - `findByCategoriaId(categoriaId)` - Filtrar por categoría
    - `findByProveedorId(proveedorId)` - Filtrar por proveedor
    - Consultas personalizadas con `@Query`
  - [ ] Verificar integridad de datos

- [ ] **Paso 4.2: Test de UsuariosRepository**
  - [ ] Crear `UsuariosRepositoryTest.java`
  - [ ] Implementar tests para:
    - `findByNombre(nombre)` - Buscar usuario por nombre
    - `findByNombreIgnoreCase(nombre)` - Buscar case-insensitive
    - `existsByNombre(nombre)` - Verificar existencia
  - [ ] Verificar consultas custom

- [ ] **Paso 4.3: Test de OrdenesDeVentasRepository**
  - [ ] Crear `OrdenesDeVentasRepositoryTest.java`
  - [ ] Implementar tests para consultas complejas
  - [ ] Verificar joins con otras tablas

#### Verificación de Cobertura Fase 4

- [ ] **Paso 4.4: Medir cobertura después de tests de repositories**
  - [ ] Ejecutar `./mvnw clean test jacoco:report`
  - [ ] Verificar que cobertura esté ≥ 65%

---

### FASE 5: Pruebas Unitarias Frontend (Prioridad: ALTA)

**Objetivo**: Alcanzar 40% de cobertura implementando tests de componentes críticos

#### Componentes de Autenticación

- [ ] **Paso 5.1: Test de LoginScreen**
  - [ ] Crear `frontend/src/components/LoginScreen.test.tsx`
  - [ ] Implementar tests para:
    - Renderizado inicial del formulario
    - Validación de campos vacíos
    - Submit con credenciales válidas
    - Manejo de errores de login
    - Navegación después de login exitoso
  - [ ] Mockear `AuthContext` y `apiService`

- [ ] **Paso 5.2: Test de AuthContext**
  - [ ] Crear `frontend/src/contexts/AuthContext.test.tsx`
  - [ ] Implementar tests para:
    - Inicialización del contexto
    - Función `login()` exitosa
    - Función `logout()` limpia estado
    - Persistencia de token en localStorage
    - Verificación de token expirado

#### Servicios API

- [ ] **Paso 5.3: Test de apiService**
  - [ ] Crear `frontend/src/services/apiService.test.ts`
  - [ ] Mockear axios con `vi.mock('axios')`
  - [ ] Implementar tests para:
    - Configuración de headers con token
    - Manejo de errores 401 (unauthorized)
    - Manejo de errores 500 (server error)
    - Manejo de errores de red

- [ ] **Paso 5.4: Test de inventarioService**
  - [ ] Crear `frontend/src/services/inventarioService.test.ts`
  - [ ] Implementar tests para:
    - `getProductos()` - Obtener productos
    - `createProducto(dto)` - Crear producto
    - `updateProducto(id, dto)` - Actualizar producto
    - `deleteProducto(id)` - Eliminar producto
  - [ ] Verificar transformación de DTOs

- [ ] **Paso 5.5: Test de mlService**
  - [ ] Crear `frontend/src/services/mlService.test.ts`
  - [ ] Implementar tests para:
    - `getPredictions(data)` - Obtener predicciones
    - Manejo de timeout
    - Fallback a datos dummy

#### Componentes de Negocio Críticos

- [ ] **Paso 5.6: Test de PuntoDeVenta**
  - [ ] Crear `frontend/src/components/PuntoDeVenta.test.tsx`
  - [ ] Implementar tests para:
    - Renderizado inicial de productos
    - Añadir producto al carrito
    - Incrementar/decrementar cantidad
    - Calcular total correctamente
    - Guardar orden exitosamente
    - Manejo de productos sin stock
  - [ ] Mockear servicios de ventas

- [ ] **Paso 5.7: Test de Inventario**
  - [ ] Crear `frontend/src/components/Inventario.test.tsx`
  - [ ] Implementar tests para:
    - Renderizado de tabla de productos
    - Búsqueda y filtrado
    - Abrir modal de crear producto
    - Abrir modal de editar producto
    - Eliminar producto con confirmación
  - [ ] Mockear inventarioService

#### Hooks Personalizados

- [ ] **Paso 5.8: Test de useAuth**
  - [ ] Crear `frontend/src/hooks/useAuth.test.ts`
  - [ ] Implementar tests para lógica del hook

- [ ] **Paso 5.9: Test de useToast**
  - [ ] Crear `frontend/src/hooks/useToast.test.ts`
  - [ ] Implementar tests para notificaciones

#### Verificación de Cobertura Fase 5

- [ ] **Paso 5.10: Medir cobertura del frontend**
  - [ ] Ejecutar `npm run test:coverage`
  - [ ] Abrir `coverage/index.html`
  - [ ] Verificar que cobertura esté ≥ 40%

---

### FASE 6: Pruebas de Componentes Frontend Adicionales (Prioridad: MEDIA)

**Objetivo**: Alcanzar 60% de cobertura implementando tests de componentes secundarios

#### Componentes de Gestión

- [ ] **Paso 6.1: Test de GestionEmpleados**
  - [ ] Crear `GestionEmpleados.test.tsx`
  - [ ] Implementar tests básicos CRUD

- [ ] **Paso 6.2: Test de GestionPersonas**
  - [ ] Crear `GestionPersonas.test.tsx`
  - [ ] Implementar tests básicos CRUD

- [ ] **Paso 6.3: Test de DeudasProveedores**
  - [ ] Crear `DeudasProveedores.test.tsx`
  - [ ] Implementar tests de cálculos

- [ ] **Paso 6.4: Test de PuntoDeCompras**
  - [ ] Crear `PuntoDeCompras.test.tsx`
  - [ ] Implementar tests de flujo de compras

#### Componentes de UI Compartidos

- [ ] **Paso 6.5: Test de SidebarNavigation**
  - [ ] Crear `SidebarNavigation.test.tsx`
  - [ ] Implementar tests para:
    - Renderizado de enlaces
    - Estado activo correcto
    - Navegación al hacer click
    - Botón de logout

- [ ] **Paso 6.6: Test de MainMenu**
  - [ ] Crear `MainMenu.test.tsx`
  - [ ] Implementar tests de dashboard

- [ ] **Paso 6.7: Test de Modales**
  - [ ] Crear `ModalCrearProducto.test.tsx`
  - [ ] Crear `ModalEditarProducto.test.tsx`
  - [ ] Crear `ModalPredicciones.test.tsx`
  - [ ] Implementar tests de renderizado y validación

#### Verificación de Cobertura Fase 6

- [ ] **Paso 6.8: Medir cobertura después de componentes adicionales**
  - [ ] Ejecutar `npm run test:coverage`
  - [ ] Verificar que cobertura esté ≥ 60%

---

### FASE 7: Pruebas Unitarias ML Service (Prioridad: ALTA)

**Objetivo**: Alcanzar 50% de cobertura implementando tests de lógica de predicción

#### Tests de API

- [ ] **Paso 7.1: Test de endpoints FastAPI**
  - [ ] Crear `ml-prediction-service/tests/integration/test_api.py`
  - [ ] Usar `TestClient` de FastAPI
  - [ ] Implementar tests para:
    - `GET /` - Health check
    - `GET /health` - Health status
    - `POST /predict` - Predicción exitosa
    - `POST /predict` - Predicción con datos inválidos
  - [ ] Verificar estructura de respuesta JSON

#### Tests de Pipeline de Datos

- [ ] **Paso 7.2: Test de feature engineering**
  - [ ] Crear `ml-prediction-service/tests/unit/test_pipeline.py`
  - [ ] Implementar tests para:
    - Transformación de datos de entrada
    - Generación de características temporales
    - Manejo de valores faltantes
    - Validación de tipos de datos
  - [ ] Verificar que las features generadas sean correctas

- [ ] **Paso 7.3: Test de predictor**
  - [ ] Crear `ml-prediction-service/tests/unit/test_predictor.py`
  - [ ] Implementar tests para:
    - Carga correcta de modelos
    - Predicción con datos válidos
    - Manejo de errores de predicción
    - Formato de salida correcto

#### Tests de Calidad de Datos

- [ ] **Paso 7.4: Test de data_quality_analyzer**
  - [ ] Crear `ml-prediction-service/tests/unit/test_data_quality_analyzer.py`
  - [ ] Implementar tests para:
    - Detección de valores faltantes
    - Detección de outliers
    - Análisis de distribución
    - Generación de reporte

#### Verificación de Cobertura Fase 7

- [ ] **Paso 7.5: Medir cobertura de ML Service**
  - [ ] Ejecutar `pytest --cov=app --cov-report=html`
  - [ ] Abrir `htmlcov/index.html`
  - [ ] Verificar que cobertura esté ≥ 50%

---

### FASE 8: Pruebas End-to-End (E2E) (Prioridad: MEDIA)

**Objetivo**: Verificar flujos críticos de usuario completos

#### Configuración de Cypress/Playwright

- [ ] **Paso 8.1: Instalar Cypress**
  - [ ] Navegar a `frontend/`
  - [ ] Ejecutar `npm install -D cypress`
  - [ ] Ejecutar `npx cypress open` para inicializar
  - [ ] Configurar `cypress.config.ts`

- [ ] **Paso 8.2: Configurar base URL y comandos personalizados**
  - [ ] Configurar `baseUrl` en `cypress.config.ts`
  - [ ] Crear comandos personalizados en `cypress/support/commands.ts`:
    - `cy.login(nombre, password)` - Login automatizado
    - `cy.logout()` - Logout automatizado

#### Flujos Críticos de Negocio

- [ ] **Paso 8.3: E2E de flujo de login**
  - [ ] Crear `cypress/e2e/auth.cy.ts`
  - [ ] Implementar test:
    - Visitar página de login
    - Ingresar credenciales
    - Verificar redirección a dashboard
    - Verificar que token se guarde

- [ ] **Paso 8.4: E2E de flujo de venta completo**
  - [ ] Crear `cypress/e2e/ventas.cy.ts`
  - [ ] Implementar test:
    - Login como usuario
    - Navegar a POS
    - Seleccionar workspace
    - Añadir productos al carrito
    - Verificar cálculo de total
    - Guardar orden
    - Verificar actualización de stock

- [ ] **Paso 8.5: E2E de gestión de inventario**
  - [ ] Crear `cypress/e2e/inventario.cy.ts`
  - [ ] Implementar test:
    - Login como admin
    - Navegar a inventario
    - Crear nuevo producto
    - Editar producto
    - Verificar cambios en tabla
    - Eliminar producto

- [ ] **Paso 8.6: E2E de predicciones ML**
  - [ ] Crear `cypress/e2e/predicciones.cy.ts`
  - [ ] Implementar test:
    - Login como usuario
    - Navegar a inventario
    - Abrir modal de predicciones
    - Verificar carga de predicciones
    - Crear orden de compra desde predicciones

#### Verificación E2E

- [ ] **Paso 8.7: Ejecutar suite completa E2E**
  - [ ] Ejecutar `npx cypress run`
  - [ ] Verificar que todos los tests pasen
  - [ ] Capturar screenshots de fallos

---

### FASE 9: Optimización y Alcance de Objetivo 70% (Prioridad: ALTA)

**Objetivo**: Análisis de cobertura y implementación de tests faltantes

#### Análisis de Cobertura Global

- [ ] **Paso 9.1: Generar reportes de cobertura de todos los servicios**
  - [ ] Backend: `./mvnw clean test jacoco:report`
  - [ ] Frontend: `npm run test:coverage`
  - [ ] ML Service: `pytest --cov=app --cov-report=html`
  - [ ] Consolidar métricas en un documento

- [ ] **Paso 9.2: Identificar áreas con baja cobertura**
  - [ ] Revisar reporte JaCoCo del backend
  - [ ] Revisar reporte de Vitest del frontend
  - [ ] Revisar reporte de pytest del ML Service
  - [ ] Listar clases/funciones con <50% de cobertura

- [ ] **Paso 9.3: Priorizar implementación de tests faltantes**
  - [ ] Crear lista de archivos críticos sin tests
  - [ ] Implementar tests adicionales enfocados en:
    - Ramas no cubiertas (if/else)
    - Manejo de excepciones
    - Casos edge (valores límite, nulos, vacíos)

#### Tests de Casos Edge

- [ ] **Paso 9.4: Backend - Tests de validación**
  - [ ] Tests para DTOs con datos inválidos
  - [ ] Tests para operaciones con IDs inexistentes
  - [ ] Tests para operaciones sin autenticación
  - [ ] Tests para operaciones sin permisos

- [ ] **Paso 9.5: Frontend - Tests de estados de error**
  - [ ] Tests para componentes en estado de carga
  - [ ] Tests para componentes con error de API
  - [ ] Tests para componentes sin datos
  - [ ] Tests para componentes con datos inválidos

- [ ] **Paso 9.6: ML Service - Tests de robustez**
  - [ ] Tests con datos de entrada malformados
  - [ ] Tests con modelos no cargados
  - [ ] Tests con predicciones extremas
  - [ ] Tests de timeout

#### Verificación Final de Cobertura

- [ ] **Paso 9.7: Medición final de cobertura**
  - [ ] Backend: Verificar ≥ 70%
  - [ ] Frontend: Verificar ≥ 70%
  - [ ] ML Service: Verificar ≥ 70%
  - [ ] Documentar métricas finales

---

### FASE 10: Integración con CI/CD y Documentación (Prioridad: MEDIA)

**Objetivo**: Automatizar ejecución de tests y mantener calidad

#### Configuración de GitHub Actions

- [ ] **Paso 10.1: Crear workflow de CI para Backend**
  - [ ] Crear `.github/workflows/backend-tests.yml`
  - [ ] Configurar para ejecutar en cada push y PR
  - [ ] Incluir steps:
    - Checkout código
    - Setup Java 17
    - Run tests con Maven
    - Upload reporte de cobertura
    - Fail si cobertura < 70%

- [ ] **Paso 10.2: Crear workflow de CI para Frontend**
  - [ ] Crear `.github/workflows/frontend-tests.yml`
  - [ ] Configurar para ejecutar en cada push y PR
  - [ ] Incluir steps:
    - Checkout código
    - Setup Node.js
    - Install dependencies
    - Run tests con Vitest
    - Upload reporte de cobertura
    - Fail si cobertura < 70%

- [ ] **Paso 10.3: Crear workflow de CI para ML Service**
  - [ ] Crear `.github/workflows/ml-tests.yml`
  - [ ] Configurar para ejecutar en cada push y PR
  - [ ] Incluir steps:
    - Checkout código
    - Setup Python 3.11
    - Install dependencies
    - Run tests con pytest
    - Upload reporte de cobertura
    - Fail si cobertura < 70%

#### Documentación de Testing

- [ ] **Paso 10.4: Crear guía de testing**
  - [ ] Crear `docs/testing/GUIA_TESTING.md`
  - [ ] Documentar:
    - Cómo ejecutar tests localmente
    - Cómo escribir nuevos tests
    - Estándares y convenciones
    - Troubleshooting común

- [ ] **Paso 10.5: Actualizar README principal**
  - [ ] Añadir sección de testing
  - [ ] Incluir badges de cobertura
  - [ ] Documentar comandos de testing

- [ ] **Paso 10.6: Crear matriz de trazabilidad**
  - [ ] Crear `docs/testing/MATRIZ_TRAZABILIDAD.md`
  - [ ] Mapear requerimientos → tests
  - [ ] Documentar qué tests cubren qué funcionalidades

#### Configuración de Pre-commit Hooks

- [ ] **Paso 10.7: Instalar Husky (opcional)**
  - [ ] Instalar Husky en frontend
  - [ ] Configurar pre-commit hook para ejecutar tests
  - [ ] Configurar pre-push hook para verificar cobertura

---

### FASE 11: Validación y Entrega (Prioridad: CRÍTICA)

**Objetivo**: Verificación completa del cumplimiento del objetivo 70%

#### Validación Integral

- [ ] **Paso 11.1: Ejecutar suite completa de tests**
  - [ ] Backend: `./mvnw clean verify`
  - [ ] Frontend: `npm run test:run && npm run test:coverage`
  - [ ] ML Service: `pytest --cov=app --cov-report=term`
  - [ ] Verificar que no haya tests fallando

- [ ] **Paso 11.2: Generar reportes finales**
  - [ ] Consolidar reportes de cobertura
  - [ ] Crear dashboard de métricas
  - [ ] Documentar resultados

- [ ] **Paso 11.3: Verificación de cumplimiento RNF007**
  - [ ] Confirmar Backend ≥ 70%
  - [ ] Confirmar Frontend ≥ 70%
  - [ ] Confirmar ML Service ≥ 70%
  - [ ] Actualizar documentación de requerimientos

#### Presentación de Resultados

- [ ] **Paso 11.4: Crear reporte ejecutivo**
  - [ ] Crear `docs/testing/REPORTE_COBERTURA_FINAL.md`
  - [ ] Incluir:
    - Métricas iniciales vs finales
    - Gráficas de progreso
    - Resumen de tests implementados
    - Lecciones aprendidas
    - Recomendaciones futuras

- [ ] **Paso 11.5: Demo de tests en ejecución**
  - [ ] Preparar demostración de:
    - Tests unitarios
    - Tests de integración
    - Tests E2E
    - Reportes de cobertura

---

## 📊 MÉTRICAS Y OBJETIVOS

### Distribución de Tests por Fase

| Fase | Backend | Frontend | ML Service | Total Estimado |
|------|---------|----------|------------|----------------|
| Fase 1 | 0 tests | 0 tests | 0 tests | Infraestructura |
| Fase 2 | ~30 tests | 0 tests | 0 tests | 30 tests |
| Fase 3 | ~25 tests | 0 tests | 0 tests | 25 tests |
| Fase 4 | ~15 tests | 0 tests | 0 tests | 15 tests |
| Fase 5 | 0 tests | ~40 tests | 0 tests | 40 tests |
| Fase 6 | 0 tests | ~30 tests | 0 tests | 30 tests |
| Fase 7 | 0 tests | 0 tests | ~25 tests | 25 tests |
| Fase 8 | 0 tests | 4 E2E | 0 tests | 4 tests |
| Fase 9 | ~20 tests | ~20 tests | ~15 tests | 55 tests |
| **TOTAL** | **~90 tests** | **~94 tests** | **~40 tests** | **~224 tests** |

### Objetivos de Cobertura por Fase

| Fase | Backend Target | Frontend Target | ML Service Target |
|------|----------------|-----------------|-------------------|
| Fase 2 | 40% | 0% | 0% |
| Fase 3 | 55% | 0% | 0% |
| Fase 4 | 65% | 0% | 0% |
| Fase 5 | 65% | 40% | 0% |
| Fase 6 | 65% | 60% | 0% |
| Fase 7 | 65% | 60% | 50% |
| Fase 9 | **≥70%** | **≥70%** | **≥70%** |

---

## 🎯 CRITERIOS DE ÉXITO

### ✅ Infraestructura Configurada:
- [ ] Maven tests habilitados y ejecutándose
- [ ] JaCoCo generando reportes de cobertura
- [ ] Vitest configurado y funcional
- [ ] pytest configurado y funcional
- [ ] Scripts de testing documentados

### ✅ Cobertura de 70% Alcanzada:
- [ ] Backend: ≥ 70% líneas, branches, métodos
- [ ] Frontend: ≥ 70% statements, branches, functions, lines
- [ ] ML Service: ≥ 70% lines, branches

### ✅ Calidad de Tests:
- [ ] Tests unitarios aislados con mocks apropiados
- [ ] Tests de integración usando base de datos de test
- [ ] Tests E2E cubriendo flujos críticos
- [ ] Sin tests flaky (intermitentes)
- [ ] Tiempo de ejecución razonable (< 5 min por suite)

### ✅ Automatización:
- [ ] CI/CD ejecutando tests automáticamente
- [ ] Reportes de cobertura generados en cada build
- [ ] Builds fallando si cobertura < 70%

### ✅ Documentación:
- [ ] Guía de testing completa
- [ ] Matriz de trazabilidad actualizada
- [ ] README actualizado con comandos de testing
- [ ] Reporte final de cumplimiento RNF007

---

## 📝 ARCHIVOS A CREAR/MODIFICAR

### Backend:
- `backend/pom.xml` - Habilitar tests, añadir JaCoCo
- `backend/src/test/java/.../service/*ServiceTest.java` - Tests de servicios (5 archivos)
- `backend/src/test/java/.../controller/*ControllerTest.java` - Tests de controllers (4 archivos)
- `backend/src/test/java/.../repository/*RepositoryTest.java` - Tests de repositories (3 archivos)
- `backend/src/test/java/.../util/TestSecurityConfig.java` - Configuración de seguridad para tests

### Frontend:
- `frontend/vitest.config.ts` - Configuración de Vitest
- `frontend/src/test/setup.ts` - Setup de testing
- `frontend/package.json` - Scripts de testing
- `frontend/src/components/*.test.tsx` - Tests de componentes (10+ archivos)
- `frontend/src/services/*.test.ts` - Tests de servicios (5 archivos)
- `frontend/src/hooks/*.test.ts` - Tests de hooks (2 archivos)
- `frontend/src/contexts/*.test.tsx` - Tests de contextos (1 archivo)
- `frontend/cypress/e2e/*.cy.ts` - Tests E2E (4 archivos)
- `frontend/cypress.config.ts` - Configuración de Cypress

### ML Service:
- `ml-prediction-service/pytest.ini` - Configuración de pytest
- `ml-prediction-service/requirements.txt` - Añadir pytest
- `ml-prediction-service/tests/unit/*.py` - Tests unitarios (5+ archivos)
- `ml-prediction-service/tests/integration/*.py` - Tests de integración (2 archivos)

### Documentación:
- `docs/testing/GUIA_TESTING.md` - Guía completa de testing
- `docs/testing/MATRIZ_TRAZABILIDAD.md` - Matriz de requerimientos
- `docs/testing/REPORTE_COBERTURA_FINAL.md` - Reporte final
- `README.md` - Actualización con sección de testing

### CI/CD:
- `.github/workflows/backend-tests.yml` - Workflow de backend
- `.github/workflows/frontend-tests.yml` - Workflow de frontend
- `.github/workflows/ml-tests.yml` - Workflow de ML

---

## ⚠️ RIESGOS Y MITIGACIONES

### Riesgo 1: Tiempo de Implementación
**Descripción**: 224 tests estimados pueden tomar varias semanas  
**Mitigación**: 
- Priorizar fases 1-5 (críticas)
- Implementar en paralelo (backend y frontend simultáneamente)
- Usar generadores de tests cuando sea posible

### Riesgo 2: Tests Frágiles
**Descripción**: Tests pueden romperse frecuentemente con cambios en código  
**Mitigación**:
- Escribir tests mantenibles y desacoplados
- Usar patrones de Page Object para E2E
- Revisar y refactorizar tests regularmente

### Riesgo 3: Cobertura Superficial
**Descripción**: Alcanzar 70% sin tests de calidad  
**Mitigación**:
- Revisar código de tests en PRs
- Enfocarse en casos edge y manejo de errores
- No escribir tests solo para aumentar cobertura

### Riesgo 4: Performance de CI/CD
**Descripción**: Suite de tests muy lenta puede bloquear desarrollo  
**Mitigación**:
- Ejecutar tests unitarios en paralelo
- Separar tests E2E en workflow diferente
- Optimizar setup/teardown de tests

---

## 📅 CRONOGRAMA ESTIMADO

| Fase | Duración Estimada | Dependencias |
|------|-------------------|--------------|
| Fase 1 | 1-2 días | Ninguna |
| Fase 2 | 3-4 días | Fase 1 |
| Fase 3 | 3-4 días | Fase 1 |
| Fase 4 | 2-3 días | Fase 1 |
| Fase 5 | 4-5 días | Fase 1 |
| Fase 6 | 3-4 días | Fase 5 |
| Fase 7 | 3-4 días | Fase 1 |
| Fase 8 | 2-3 días | Fase 5 |
| Fase 9 | 3-5 días | Fases 2-8 |
| Fase 10 | 2-3 días | Fase 9 |
| Fase 11 | 1-2 días | Todas |
| **TOTAL** | **27-39 días** | (~5-8 semanas) |

**Nota**: Con 2-3 desarrolladores trabajando en paralelo, el tiempo puede reducirse a 3-4 semanas.

---

## 🚀 PRÓXIMOS PASOS INMEDIATOS

1. **Aprobar este plan**: Revisar y confirmar que el enfoque es correcto
2. **Iniciar Fase 1**: Configurar infraestructura de testing (crítico)
3. **Ejecutar en paralelo**:
   - Un desarrollador en Backend (Fases 2-4)
   - Un desarrollador en Frontend (Fases 5-6)
   - Un desarrollador en ML Service (Fase 7)
4. **Reuniones de sincronización**: Cada 2-3 días para reportar progreso

---

## 📌 ESTADO: 🔄 ESPERANDO APROBACIÓN

### Notas de Implementación
- **Prioridad**: CRÍTICA - Cumplimiento de RNF007
- **Complejidad**: ALTA - Requiere implementación de 224+ tests
- **Tiempo Estimado**: 5-8 semanas con equipo completo
- **Riesgo**: MEDIO - Requiere disciplina y constancia
- **Impacto**: ALTO - Mejora significativa en calidad y mantenibilidad del código

---

**Fecha de creación del plan**: 21 Enero 2026  
**Responsable**: Equipo de Desarrollo POS Finanzas  
**Objetivo**: Alcanzar 70% de cobertura de pruebas según RNF007
