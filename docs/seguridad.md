# Arquitectura de Seguridad del Sistema POS

## Introducción

El sistema implementa una arquitectura de seguridad robusta basada en **Spring Security** para el backend y **React Context** para el frontend, utilizando **JSON Web Tokens (JWT)** como mecanismo de autenticación y autorización. La seguridad se estructura en múltiples capas que van desde la configuración de CORS hasta la protección granular de rutas basada en roles.

## Flujo de Autenticación Completo

### 1. Proceso de Login

El proceso se inicia cuando el usuario ingresa sus credenciales en `LoginScreen.tsx`. Este componente captura el evento `onSubmit` del formulario y invoca `handleSubmit()`, que ejecuta `authService.login(credentials)` del archivo `apiService.ts`. Este servicio realiza una petición POST al endpoint `/api/auth/login` del backend.

En el backend, `AuthController.login()` recibe las credenciales y ejecuta una serie de validaciones críticas. Primero, `usuariosRepository.findByNombre(nombre)` busca al usuario en la base de datos. Si el usuario existe, se valida que su estado sea "Activo" verificando `usuario.getEstados().getEstado() === "Activo"`. Posteriormente, se realiza la validación de contraseña utilizando `passwordEncoder.matches()` para contraseñas hasheadas con BCrypt o comparación directa para compatibilidad con contraseñas legacy.

Una vez validadas las credenciales, `jwtService.generateToken(usuario.getNombre(), usuario.getId())` genera un JWT que incluye el username como subject y el userId como claim personalizado. El token se firma con una clave secreta de 256 bits usando el algoritmo HS256 y tiene una expiración de 24 horas. El controlador construye un `LoginResponseDTO` que contiene el token, información del usuario y su rol, y lo retorna al frontend.

### 2. Gestión de Tokens en el Frontend

Cuando `authService.login()` recibe la respuesta exitosa, extrae los datos del `LoginResponse` y los almacena temporalmente en localStorage como `authToken` para compatibilidad legacy. Sin embargo, el manejo principal se delega al `AuthContext.tsx` mediante `authLogin(loginResponse)`.

El `AuthContext` ejecuta la función `login()` que crea un objeto `UsuarioAutenticado` con todos los datos del usuario y lo persiste en localStorage bajo la clave `pos_auth_data`. También actualiza el estado local con `setUsuario(userData)` y `setIsAuthenticated(true)`, lo que hace que toda la aplicación reaccione al cambio de estado de autenticación.

### 3. Interceptor de Peticiones API

Para todas las peticiones HTTP subsecuentes, el interceptor `api.interceptors.request.use()` en `apiService.ts` se ejecuta automáticamente. Este interceptor busca primero en `localStorage.getItem('pos_auth_data')` el token del nuevo sistema de autenticación. Si lo encuentra, extrae el token y lo añade al header `Authorization: Bearer ${token}` de la petición.

Como mecanismo de fallback, si no encuentra datos en el nuevo sistema, busca en `localStorage.getItem('authToken')` para mantener compatibilidad con tokens legacy. El interceptor también incluye validaciones básicas para detectar tokens corruptos y limpiarlos automáticamente.

## Configuración de Seguridad del Backend

### SecurityConfig.java

La configuración de seguridad se centraliza en `SecurityConfig.java`, que define la cadena de filtros de Spring Security. `securityFilterChain()` configura las reglas de autorización mediante `authorizeHttpRequests()`, permitiendo acceso libre a los endpoints `/api/auth/**` y `/auth/**` con `permitAll()`, mientras que cualquier otra petición requiere autenticación con `anyRequest().authenticated()`.

La configuración establece `SessionCreationPolicy.STATELESS` para asegurar que no se mantengan sesiones en el servidor, forzando la validación del JWT en cada petición. Se desactiva CSRF con `csrf.disable()` ya que la aplicación usa JWT en lugar de cookies de sesión.

Los manejadores de excepciones personalizados `customAccessDeniedHandler()` y `customAuthenticationEntryPoint()` capturan errores 403 (acceso denegado) y 401 (no autenticado) respectivamente, devolviendo respuestas JSON estructuradas con información detallada del error para debugging.

### CORS Configuration

`corsConfigurationSource()` configura CORS para permitir peticiones desde el frontend en desarrollo (`http://localhost:5173`) y producción (`https://pos-finanzas-q2ddz.ondigitalocean.app`). Se permiten todos los métodos HTTP estándar y headers, con `setAllowCredentials(true)` para habilitar el envío de cookies y headers de autorización.

### JwtRequestFilter.java

`JwtRequestFilter` extiende `OncePerRequestFilter` y se ejecuta antes que `UsernamePasswordAuthenticationFilter` en la cadena de filtros. En `doFilterInternal()`, el filtro primero verifica si la petición es a un endpoint de autenticación (`/api/auth/` o `/auth/`) y si es así, la pasa sin procesamiento JWT.

Para otras peticiones, extrae el header `Authorization` y verifica que comience con "Bearer ". Si encuentra un token válido, invoca `jwtService.extractUsername(token)` para obtener el username del JWT. Luego, `jwtService.validateToken(token, username)` verifica que el token no haya expirado y que el username coincida.

Si la validación es exitosa, el filtro crea un `UsernamePasswordAuthenticationToken` con autoridades básicas (`ROLE_USER`) y lo establece en el `SecurityContextHolder`. Esto permite que el resto de la aplicación reconozca al usuario como autenticado para esa petición específica.

## Servicios JWT

### JwtService.java

`JwtService` encapsula toda la lógica de manejo de JWT utilizando la librería `io.jsonwebtoken`. `generateToken(username, userId)` crea un JWT con el username como subject, el userId como claim personalizado, y establece timestamps de emisión y expiración.

`extractUsername(token)` y `extractUserId(token)` utilizan `extractClaim()` genérico para obtener información específica del token. `validateToken(token, username)` combina la validación de expiración mediante `isTokenExpired()` y la verificación de que el username extraído coincida con el esperado.

La implementación utiliza una clave secreta fija de 256 bits almacenada como constante (en producción debería venir de variables de entorno) y el algoritmo de firma HS256 para garantizar la integridad del token.

## Sistema de Roles y Autorización

### AuthContext.tsx

El `AuthContext` implementa un sistema de gestión de estado centralizado para la autenticación. Define interfaces TypeScript estrictas como `UsuarioAutenticado` y `LoginResponse` para garantizar type safety. Las funciones auxiliares `isAdmin()`, `isEmployee()`, `getUserRole()` y `getUserName()` proporcionan métodos convenientes para verificar permisos en toda la aplicación.

El hook `useEffect()` verifica automáticamente si existe una sesión persistente en localStorage al cargar la aplicación, permitiendo que los usuarios mantengan su sesión entre recargas del navegador. La función `logout()` limpia tanto el estado local como el localStorage.

### ProtectedRoute.tsx

`ProtectedRoute` implementa protección de rutas basada en roles a nivel de componente. Recibe props como `requiredRole`, `adminOnly`, y `employeeAccess` para definir granularmente los permisos requeridos. La lógica de autorización en la función anónima dentro de `hasAccess` evalúa las diferentes combinaciones de permisos.

Si el usuario no cumple los requisitos, el componente renderiza un mensaje de acceso denegado con información detallada sobre el rol actual del usuario y los permisos requeridos, mejorando la experiencia de usuario al explicar por qué se denegó el acceso.

## Manejo de Errores y Seguridad

### Interceptor de Respuestas

El interceptor `api.interceptors.response.use()` maneja automáticamente errores de autenticación y autorización. Cuando recibe un error 401 (token expirado o inválido), limpia automáticamente el localStorage y dispara un evento personalizado `auth:logout` que la aplicación puede capturar para redirigir al login.

Para errores 403 (acceso denegado), el interceptor registra el error pero no limpia el token, ya que indica que el usuario está autenticado pero no autorizado para esa acción específica.

### Validaciones de Seguridad

`AuthController.login()` implementa múltiples capas de validación: verificación de existencia del usuario, validación de estado activo, y verificación de contraseña. El controlador utiliza `passwordEncoder.matches()` para verificar contraseñas hasheadas con BCrypt, pero mantiene compatibilidad con contraseñas en texto plano para usuarios legacy.

La respuesta nunca incluye información sensible como contraseñas; en su lugar, se construye un DTO limpio con solo la información necesaria para el frontend.

## Persistencia y Recuperación de Sesión

### Almacenamiento Local

El sistema implementa un mecanismo dual de almacenamiento: el nuevo sistema guarda toda la información de autenticación en `pos_auth_data` como un objeto JSON estructurado, mientras que mantiene compatibilidad con `authToken` legacy. Esta estrategia permite migración gradual y debugging sencillo.

### Recuperación Automática

Al inicializar la aplicación, `AuthContext` ejecuta `checkPersistedAuth()` que intenta restaurar la sesión desde localStorage. Si encuentra datos válidos, reconstruye el estado de autenticación sin requerir un nuevo login. Esta funcionalidad mejora significativamente la experiencia de usuario al mantener sesiones entre navegaciones.

## Consideraciones de Seguridad en Producción

El sistema incluye logs detallados para debugging que deben removerse en producción para evitar exposición de información sensible. La clave secreta JWT está hardcodeada para desarrollo pero debe migrarse a variables de entorno en producción.

La configuración CORS está específicamente configurada para los dominios de desarrollo y producción, evitando vulnerabilidades de origen cruzado. Los tokens tienen una expiración de 24 horas, balanceando seguridad con usabilidad.

El sistema implementa validación tanto en frontend como backend, pero nunca confía únicamente en validaciones del cliente, asegurando que toda validación crítica ocurra en el servidor donde no puede ser manipulada.