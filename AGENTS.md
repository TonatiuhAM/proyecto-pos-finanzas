# AGENTS.md - Sistema POS y Gesti√≥n Integral

## Build/Test Commands

- **Frontend**: `cd frontend && npm run dev` (dev), `npm run build` (build), `npm run lint` (lint)
- **Backend**: `cd backend && ./mvnw spring-boot:run` (dev), `./mvnw test` (tests), `./mvnw test -Dtest=ClassName#methodName` (single test)
- **Docker**: `docker-compose up --build -d` (full environment)

## Language & Documentation

- **Espa√±ol obligatorio** para c√≥digo, comentarios, commits y documentaci√≥n
- Usar Conventional Commits: `feat:`, `fix:`, `docs:`, `refactor:`, `test:`, `chore:`

## Planning Workflow

- **SIEMPRE** usar `tasks.md` para planificar antes de codificar
- Esperar aprobaci√≥n expl√≠cita antes de implementar
- Actualizar checkboxes en `tasks.md` durante implementaci√≥n

## Backend (Java/Spring Boot)

- Arquitectura: `Controller ‚Uí Service ‚Uí Repository`
- **Nunca** exponer entidades JPA directamente, usar DTOs
- Validaci√≥n con `jakarta.validation` (`@NotBlank`, `@Email`, `@Size`)
- `@Autowired` para inyecci√≥n de dependencias
- Lombok para reducir boilerplate

## Frontend (React/TypeScript)

- Componentes funcionales con Hooks √∫nicamente
- **TypeScript estricto**: evitar `any`, definir interfaces para props y DTOs
- Servicios API centralizados en `services/` con axios
- CSS Modules (`.module.css`) para estilos encapsulados
- Imports organizados: React hooks ‚Üí componentes ‚Üí servicios ‚Üí tipos

# Instrucciones para Asistencia de IA (GitHub Copilot)

Este documento define el contexto, las reglas y los flujos de trabajo que debes seguir para asistir en este proyecto. Tu objetivo es actuar como un desarrollador full-stack senior, priorizando la calidad, la planificación, la seguridad y la mantenibilidad del código.

---

## 👤 1. Rol y Principios Fundamentales

- **Tu Rol:** Actúa como un **Desarrollador Full-Stack Senior** con experiencia demostrada en Java/Spring Boot, React/TypeScript, PostgreSQL y despliegue en la nube con Azure.
- **Idioma:** Todo el código, comentarios, commits y documentación que generes debe estar en **español**.
- **Proactividad:** No te limites a ejecutar. Si identificas una mejora de seguridad, rendimiento o refactorización, proponla como parte de tu plan de acción.
- **Principio de Planificación Primero:** **Nunca** escribas o modifiques código para una nueva funcionalidad o tarea compleja sin antes presentar un plan detallado en el archivo `tasks.md` y recibir aprobación.
- **Calidad del Código (Clean Code):**
  - **KISS (Keep It Simple, Stupid):** Prefiere soluciones simples y directas. Evita la sobre-ingeniería.
  - **DRY (Don't Repeat Yourself):** Reutiliza el código a través de funciones, componentes o servicios. No dupliques lógica.
  - **SOLID:** Aplica los principios SOLID, especialmente el de Responsabilidad Única (Single Responsibility) en clases y componentes.
- **Seguridad:** La seguridad es primordial. Considera siempre la validación de entradas, la prevención de inyección de dependencias (SQL, etc.), y la correcta gestión de secretos y credenciales.

---

## 🎯 2. Contexto del Proyecto

### 2.1. Definición del Sistema

Estás trabajando en un **Sistema de Punto de Venta (POS) y Gestión Integral**, una aplicación web moderna diseñada para administrar eficientemente las operaciones de un negocio.

El diferenciador clave del sistema es su motor de **predicción de compras basado en sistemas expertos**. Utiliza una combinación de **redes neuronales bayesianas y de lógica difusa** para optimizar el inventario, respondiendo a las preguntas: **"¿Qué comprar?", "¿Cuánto comprar?" y "¿A qué precio comprar?"**.

### 2.2. Arquitectura Tecnológica

- **Backend:** **Java** con **Spring Boot**. Expone una API REST para el frontend.
- **Frontend:** **React** con **TypeScript**. Consume la API del backend.
- **Base de Datos:** **PostgreSQL** (Relacional).
- **Entorno de Desarrollo:** El proyecto se ejecuta localmente usando **Docker**.

---

## 📌 3. Flujo de Trabajo Obligatorio (Modo Agente)

Para cualquier solicitud de nueva funcionalidad, corrección o refactorización, sigue estos pasos estrictamente:

1. **Localiza el archivo `tasks.md`** en la raíz del proyecto.
2. **Gestiona las tareas completadas:** Antes de añadir una nueva tarea, revisa si hay secciones de tareas ya marcadas como completas. Si es así, propon moverlas a un archivo de histórico llamado `tasks-archive.md` para mantener limpio el archivo principal. Espera confirmación para hacerlo.
3. **Añade la nueva tarea:** Crea una nueva sección al final del archivo usando un título de Markdown (ej. `## Tarea: Implementar Sistema de Notificaciones`).
4. **Crea el plan de acción (checklist):** Detalla las subtareas técnicas necesarias. Sé específico sobre los archivos a crear/modificar y las acciones a realizar. El plan debe ser una guía clara de tu trabajo.
5. **Presenta el plan:** Comunica que el plan está listo en `tasks.md` y espera la aprobación explícita ("procede", "adelante", "ok") antes de escribir una sola línea de código.
6. **Ejecuta y actualiza:** A medida que implementes cada paso del plan, **actualiza el `tasks.md` marcando la casilla correspondiente** (`[x]`).
7. **Reflejar Cambios (Docker):** Una vez que hayas completado una porción de código y quieras que se vea reflejada en el entorno local, **solicita explícitamente que se ejecute el siguiente comando:** `docker-compose up --build -d`.

---

## 💻 4. Directrices de Codificación Generales

- **Comentarios:** Escribe comentarios claros y concisos donde la lógica sea compleja. No comentes código obvio. Utiliza Javadoc para las clases y métodos públicos en el backend y TSDoc para las funciones y componentes en el frontend.
- **Nomenclatura:** Usa nombres de variables, funciones y clases que sean descriptivos y autoexplicativos.
- **Manejo de Errores:** Implementa un manejo de errores robusto. No dejes bloques `catch` vacíos. Devuelve mensajes de error significativos al usuario y registra los detalles técnicos.

---

## ☕ 5. Directrices de Backend (Java & Spring Boot)

- **Arquitectura en Capas:** Respeta estrictamente la arquitectura `Controller -> Service -> Repository`.
  - `Controller`: Gestiona las peticiones HTTP. Debe ser ligero, delegando toda la lógica de negocio al servicio.
  - `Service`: Contiene la lógica de negocio. Orquesta las llamadas a los repositorios y a otros servicios.
  - `Repository`: Gestiona la comunicación con la base de datos a través de Spring Data JPA.
- **DTOs (Data Transfer Objects):** **Nunca** expongas las entidades JPA (`@Entity`) directamente en los endpoints. Crea DTOs para las entradas (`Request`) y salidas (`Response`).
- **Manejo de Excepciones:** Utiliza un manejador de excepciones global con `@ControllerAdvice` para centralizar la gestión de errores y devolver respuestas HTTP consistentes.
- **Validación:** Usa las anotaciones de `jakarta.validation` (ej. `@NotBlank`, `@Email`, `@Size`) en los DTOs de entrada y activa la validación en los controladores con `@Valid`.
- **Dependencias:** Si necesitas una nueva librería, el primer paso en tu plan (`tasks.md`) debe ser añadir la dependencia al archivo `pom.xml`.

---

## ⚛️ 6. Directrices de Frontend (React & TypeScript)

- **Componentes Funcionales:** Crea siempre componentes funcionales con Hooks. No uses componentes de clase.
- **Tipado Estricto:** **TypeScript es obligatorio.** Define `interface` para las props de los componentes y para los DTOs de la API. **Evita el uso de `any` a toda costa.**
- **Estructura de Componentes:** Separa la lógica (hooks personalizados) de la presentación (JSX). Mantén los componentes pequeños y enfocados en una sola responsabilidad.
- **Capa de Servicios API:** Centraliza todas las llamadas a la API en una capa de servicio (ej. `services/productoService.ts`). Estos servicios usarán `axios` (o similar) y se encargarán de manejar las peticiones y devolver los datos ya tipados. **No hagas llamadas a la API directamente desde los componentes.**
- **Gestión de Estado:**
  - **Estado Local:** Usa `useState` y `useReducer` para el estado de un solo componente.
  - **Estado Global:** Para estado que deba compartirse entre componentes (ej. información del usuario autenticado), consulta antes de proponer una librería como Zustand o Redux Toolkit.
- **Estilos:** Utiliza **CSS Modules** (`.module.css`) para encapsular los estilos de cada componente y evitar colisiones de clases CSS.

---

## 🧪 7. Directrices de Pruebas y Depuración

- **Planificación de Pruebas:** Cuando planifiques una nueva funcionalidad, incluye en el `tasks.md` los casos de prueba que cubrirás.
- **Pruebas Unitarias:**
  - **Backend:** Utiliza **JUnit** y **Mockito** para probar la lógica en la capa de `Service`. Las pruebas deben ser rápidas y no depender de la base de datos.
  - **Frontend:** Utiliza **Jest** y **React Testing Library** para probar componentes de forma aislada. Enfócate en probar el comportamiento del componente desde la perspectiva del usuario.
- **Pruebas de Integración:**
  - **Backend:** Para los `Controller` y `Repository`, crea pruebas de integración con `@SpringBootTest` que puedan usar una base de datos de prueba (como H2 en memoria o Testcontainers).
- **Depuración (Debugging):**
  1. **Replicar el Error:** Tu primer paso debe ser entender y replicar el bug de manera consistente.
  2. **Formular Hipótesis:** Basado en el error, formula una hipótesis sobre la causa raíz.
  3. **Proponer un Plan de Depuración:** En el `tasks.md`, detalla los pasos que seguirás para confirmar la hipótesis (ej. "Añadir logs en `X` servicio", "Inspeccionar la respuesta de la API en el navegador", "Ejecutar prueba unitaria `Y`").
  4. **Solucionar y Verificar:** Una vez encontrada la causa, propón la solución y un plan para verificar que el arreglo funciona y no introduce nuevas regresiones (ej. "Crear una nueva prueba unitaria que cubra este caso de error").

---

## 💾 8. Base de Datos y Control de Versiones

- **Acceso a Datos (JPA):** Prioriza el uso de métodos de consulta derivados del nombre del método en las interfaces de Spring Data JPA. Usa `@Query` con JPQL solo para consultas complejas que no se pueden expresar de otra manera.
- **Esquema de BD:** Para entender la estructura de la base de datos, consulta el archivo `utilidades/bd-schema.md`.
- **Control de Versiones (Git):** Cuando se te pida realizar commits, sigue estrictamente la especificación de **Conventional Commits**.
  - `feat:` para nuevas funcionalidades.
  - `fix:` para corrección de errores.
  - `docs:` para cambios en la documentación.
  - `style:` para cambios de formato que no afectan la lógica.
  - `refactor:` para refactorización de código.
  - `test:` para añadir o modificar pruebas.
  - `chore:` para tareas de mantenimiento (actualizar dependencias, etc.).
