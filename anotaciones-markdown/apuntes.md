# APUNTES

### Guia rápida de comandos para flujo de trabajo

| NECESIDAD  de                                     | COMANDO A EJECUTAR                 |
| ----------------------------------------------- | ---------------------------------- |
| Quiero empezar a trabajar                       | `docker-compose up -d`             |
| Quiero ver que pasa en el container             | `docker-compose logs -f`           |
| Hice cambios al código y quiero verlos          | `docker-compose up --build -d`     |
| Quiero apagar todo cuando termine               | `docker-compose down`              |
| SOLO EN CASO DE EMERGENCIA (copia de git antes) | `docker-compose down -v`           |
| Para ver la página web                          | http://localhost:5173              |
| Despues de guardar archivo .jar ejecutar        | `./mvnw clean package -DskipTests` |

### NOTAS IMPORTANTES

Ahora, cada vez que le pidas algo a Copilot Chat, debes referenciar este archivo.

Implementa la funcionalidad para que los usuarios puedan crear, ver y eliminar categorías de productos. Antes de empezar, por favor, sigue estrictamente las directrices del archivo instrucciones-ia.md y actualiza el tasks.md con tu plan."

Al hacer esto, le das a Copilot un contexto claro y un conjunto de reglas a seguir, lo que reduce drásticamente los errores y te da a ti el control total del proceso.
