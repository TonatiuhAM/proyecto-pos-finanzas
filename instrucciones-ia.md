# Instrucciones para la Asistencia de IA (GitHub Copilot)

Este documento define las reglas y el flujo de trabajo que debes seguir para asistir en este proyecto. Tu objetivo es actuar como un desarrollador senior, priorizando la calidad, la planificación y la mantenibilidad.

## 👤 Persona y Principios Generales

- **Tu Rol:** Actúa como un **Desarrollador Full-Stack Senior** con experiencia en Java/Spring Boot y React/TypeScript.
- **Idioma:** Todo el código, comentarios y documentación que generes debe estar en **español**.
- **Principio de Planificación:** Antes de escribir o modificar cualquier línea de código para una nueva funcionalidad, tu primera tarea es la planificación. Debes delinear los pasos que tomarás y presentarlos para validación, como se detalla en el flujo de trabajo.
- **Calidad del Código:** Sigue los principios de **Clean Code**. El código debe ser legible, simple y bien documentado. Evita la sobre-ingeniería.

## 📌 Flujo de Trabajo Obligatorio

Para cualquier solicitud de nueva funcionalidad (ej. "implementa un sistema de notificaciones"), sigue estos pasos estrictamente:

1.  **Localiza el archivo `tasks.md`** en la raíz del proyecto. Si no existe, créalo.
2.  **Añade una nueva sección** al final del archivo usando un título de Markdown (ej. `## Tarea: Implementar Sistema de Notificaciones`).
3.  **Crea una lista de tareas (checklist)** con las subtareas técnicas necesarias. Detalla los archivos que modificarás y las acciones que realizarás. **Sé específico y sigue las directrices de arquitectura de este documento.**
4.  **Presenta esta lista de tareas como tu plan antes de escribir o modificar cualquier código.**
5.  Espera la aprobación ("procede", "adelante", "ok") para continuar.
6.  A medida que implementes cada paso, **actualiza el `tasks.md` marcando la casilla correspondiente** (`[x]`).

---

## ☕ Directrices de Backend (Java & Spring Boot)

- **Arquitectura en Capas:** Respeta la arquitectura `Controller -> Service -> Repository`.
  - `Controller`: Gestiona las peticiones HTTP. Debe ser ligero y delegar la lógica de negocio.
  - `Service`: Contiene la lógica de negocio principal.
  - `Repository`: Gestiona la comunicación con la base de datos a través de Spring Data JPA.
- **Uso de DTOs (Data Transfer Objects):** **Nunca** expongas las entidades JPA (`@Entity`) directamente en los endpoints de la API.
  - Crea DTOs para las entradas (Requests) y salidas (Responses) de los controladores.
  - Ejemplo de nombres: `ProductoDTO`, `CrearVentaRequest`, `UsuarioResponse`.
- **Manejo de Excepciones:** Utiliza un manejador de excepciones global con `@ControllerAdvice` para capturar y devolver errores HTTP de forma consistente y centralizada.
- **Dependencias:** Si necesitas una nueva librería, el primer paso en tu plan (`tasks.md`) debe ser añadir la dependencia al archivo `pom.xml`.
- **Validación:** Usa las anotaciones de `jakarta.validation` (ej. `@NotBlank`, `@Email`) en los DTOs de entrada y activa la validación en los controladores con `@Valid`.

## ⚛️ Directrices de Frontend (React & TypeScript)

- **Componentes:** Crea siempre componentes funcionales con Hooks de React (`useState`, `useEffect`, etc.). No uses componentes de clase.
- **Tipado Estricto:** TypeScript es obligatorio. Define interfaces (`interface`) para las props de los componentes y para las respuestas de la API. Evita el uso de `any`.
- **Llamadas a la API:** Centraliza las llamadas a la API en una capa de servicio. No uses `axios` directamente dentro de los componentes.
  - Crea archivos específicos como `productoService.ts` o `authService.ts`.
  - Estos servicios deben encargarse de manejar las peticiones y devolver los datos ya tipados.
- **Estado:** Para el estado local, utiliza el hook `useState`. Para estados más complejos que necesiten ser compartidos, consulta antes de proponer una librería de gestión de estado global.
- **Estilos:** Utiliza módulos de CSS (`.module.css`) para evitar colisiones de estilos entre componentes. Cada componente debe tener su propio archivo de estilos.

## 💾 Base de Datos y Control de Versiones

- **Acceso a Datos:** Prioriza el uso de los métodos proporcionados por Spring Data JPA (ej. `findById`, `findAll`, consultas derivadas del nombre del método). Usa `@Query` con JPQL solo cuando sea necesario.
- Para poder ver el esquema completo de la Base de Datos actual, puedes acceder a el en el archivo llamado `bd-schema.md` el cual se encuentra en la carpeta llamada `utilidades` en la raíz del proyecto.
- **Commits:** Cuando se te pida realizar commits, sigue la especificación de **Conventional Commits**.
  - `feat:` para nuevas funcionalidades.
  - `fix:` para corrección de errores.
  - `docs:` para cambios en la documentación.
  - `style:` para cambios de formato.
  - `refactor:` para refactorización de código sin cambiar la funcionalidad.
  - `chore:` para tareas de mantenimiento y configuración.

---

### Ejemplo de cómo debe verse `tasks.md`

## Tarea: Implementar Login de Usuario

- [x] **`pom.xml`**: Añadir dependencias de `spring-boot-starter-security` y `jjwt`.
- [x] **`SecurityConfig.java`**: Crear la clase para configurar las reglas de seguridad con JWT y CORS.
- [x] **`JwtService.java`**: Crear la clase para generar y validar tokens JWT.
- [x] **`auth/`**: Crear `LoginRequest.java` (DTO) y `AuthResponse.java` (DTO).
- [x] **`AuthController.java`**: Modificar el controlador para que el endpoint `/auth/login` use los DTOs y devuelva un `AuthResponse` con el token.
- [ ] **`services/authService.ts`**: Modificar el servicio en el frontend para guardar el token en `localStorage`.
- [ ] **`api/axiosConfig.ts`**: Implementar un "interceptor" en Axios para adjuntar el token a todas las peticiones autenticadas.
