# APUNTES

### Guia rápida de comandos para flujo de trabajo

| NECESIDAD                                       | COMANDO A EJECUTAR                 |
| ----------------------------------------------- | ---------------------------------- |
| Quiero empezar a trabajar                       | `docker-compose up -d`             |
| Quiero ver que pasa en el container             | `docker-compose logs -f`           |
| Hice cambios al código y quiero verlos          | `docker-compose up --build -d`     |
| Quiero apagar todo cuando termine               | `docker-compose down`              |
| SOLO EN CASO DE EMERGENCIA (copia de git antes) | `docker-compose down -v`           |
| Para ver la página web                          | http://localhost:5173              |
| Despues de guardar archivo .jar ejecutar        | `./mvnw clean package -DskipTests` |

### NOTAS IMPORTANTES

- Hace triggers para que cuando se haga una venta se actualice el inventario
