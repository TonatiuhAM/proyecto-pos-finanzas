| table_schema | table_name                   | column_name             | data_type                | is_nullable | column_default |
| ------------ | ---------------------------- | ----------------------- | ------------------------ | ----------- | -------------- |
| public       | categoria_personas           | id                      | character varying        | NO          |                |
| public       | categoria_personas           | categoria               | character varying        | NO          |                |
| public       | categorias_productos         | id                      | character varying        | NO          |                |
| public       | categorias_productos         | categoria               | character varying        | NO          |                |
| public       | detalles_ordenes_de_compras  | id                      | character varying        | NO          |                |
| public       | detalles_ordenes_de_compras  | ordenes_de_compras_id   | character varying        | NO          |                |
| public       | detalles_ordenes_de_compras  | producto_id             | character varying        | NO          |                |
| public       | detalles_ordenes_de_compras  | historial_costos_id     | character varying        | NO          |                |
| public       | detalles_ordenes_de_compras  | cantidad_kg             | numeric                  | NO          |                |
| public       | detalles_ordenes_de_compras  | cantidad_pz             | numeric                  | NO          |                |
| public       | detalles_ordenes_de_ventas   | id                      | character varying        | NO          |                |
| public       | detalles_ordenes_de_ventas   | ordenes_de_ventas_id    | character varying        | NO          |                |
| public       | detalles_ordenes_de_ventas   | productos_id            | character varying        | NO          |                |
| public       | detalles_ordenes_de_ventas   | historial_precios_id    | character varying        | NO          |                |
| public       | detalles_ordenes_de_ventas   | cantidad_kg             | numeric                  | NO          |                |
| public       | detalles_ordenes_de_ventas   | cantidad_pz             | numeric                  | NO          |                |
| public       | estados                      | id                      | character varying        | NO          |                |
| public       | estados                      | estado                  | character varying        | NO          |                |
| public       | historial_cargos_proveedores | id                      | character varying        | NO          |                |
| public       | historial_cargos_proveedores | personas_id             | character varying        | NO          |                |
| public       | historial_cargos_proveedores | monto_pagado            | numeric                  | NO          |                |
| public       | historial_cargos_proveedores | fecha                   | timestamp with time zone | NO          |                |
| public       | historial_cargos_proveedores | metodos_pago_id         | character varying        | NO          |                |
| public       | historial_cargos_proveedores | ordenes_de_compras_id   | character varying        | NO          |                |
| public       | historial_costos             | id                      | character varying        | NO          |                |
| public       | historial_costos             | productos_id            | character varying        | NO          |                |
| public       | historial_costos             | costo                   | numeric                  | NO          |                |
| public       | historial_costos             | fecha_de_registro       | timestamp with time zone | NO          |                |
| public       | historial_pagos_clientes     | id                      | character varying        | NO          |                |
| public       | historial_pagos_clientes     | personas_id             | character varying        | NO          |                |
| public       | historial_pagos_clientes     | monto_pagado            | numeric                  | NO          |                |
| public       | historial_pagos_clientes     | fecha                   | timestamp with time zone | NO          |                |
| public       | historial_pagos_clientes     | metodos_pago_id         | character varying        | NO          |                |
| public       | historial_pagos_clientes     | orden_de_venta_id       | character varying        | YES         |                |
| public       | historial_precios            | id                      | character varying        | NO          |                |
| public       | historial_precios            | productos_id            | character varying        | NO          |                |
| public       | historial_precios            | precio                  | numeric                  | NO          |                |
| public       | historial_precios            | fecha_de_registro       | timestamp with time zone | NO          |                |
| public       | inventarios                  | id                      | character varying        | NO          |                |
| public       | inventarios                  | productos_id            | character varying        | NO          |                |
| public       | inventarios                  | ubicaciones_id          | character varying        | NO          |                |
| public       | inventarios                  | cantidad_minima         | integer                  | NO          |                |
| public       | inventarios                  | cantidad_maxima         | integer                  | NO          |                |
| public       | inventarios                  | cantidad_kg             | integer                  | YES         |                |
| public       | inventarios                  | cantidad_pz             | integer                  | YES         |                |
| public       | metodos_pago                 | id                      | character varying        | NO          |                |
| public       | metodos_pago                 | metodo_pago             | character varying        | NO          |                |
| public       | movimientos_inventarios      | id                      | character varying        | NO          |                |
| public       | movimientos_inventarios      | productos_id            | character varying        | NO          |                |
| public       | movimientos_inventarios      | ubicaciones_id          | character varying        | NO          |                |
| public       | movimientos_inventarios      | tipo_movimientos_id     | character varying        | NO          |                |
| public       | movimientos_inventarios      | cantidad                | numeric                  | NO          |                |
| public       | movimientos_inventarios      | fecha_movimiento        | timestamp with time zone | NO          |                |
| public       | movimientos_inventarios      | usuarios_id             | character varying        | NO          |                |
| public       | movimientos_inventarios      | clave_movimiento        | character varying        | YES         |                |
| public       | ordenes_de_compras           | id                      | character varying        | NO          |                |
| public       | ordenes_de_compras           | persona_id              | character varying        | NO          |                |
| public       | ordenes_de_compras           | fecha_orden             | timestamp with time zone | NO          |                |
| public       | ordenes_de_compras           | estados_id              | character varying        | NO          |                |
| public       | ordenes_de_compras           | total_compra            | numeric                  | NO          |                |
| public       | ordenes_de_compras           | metodos_pago_id         | character varying        | YES         |                |
| public       | ordenes_de_ventas            | id                      | character varying        | NO          |                |
| public       | ordenes_de_ventas            | personas_id             | character varying        | NO          |                |
| public       | ordenes_de_ventas            | fecha_orden             | timestamp with time zone | NO          |                |
| public       | ordenes_de_ventas            | usuarios_id             | character varying        | NO          |                |
| public       | ordenes_de_ventas            | total_venta             | numeric                  | NO          |                |
| public       | ordenes_de_ventas            | metodos_pago_id         | character varying        | NO          |                |
| public       | ordenes_workspace            | id                      | character varying        | NO          |                |
| public       | ordenes_workspace            | workspace_id            | character varying        | NO          |                |
| public       | ordenes_workspace            | productos_id            | character varying        | NO          |                |
| public       | ordenes_workspace            | cantidad_pz             | numeric                  | NO          |                |
| public       | ordenes_workspace            | cantidad_kg             | numeric                  | NO          |                |
| public       | ordenes_workspace            | historial_precios_id    | character varying        | NO          |                |
| public       | personas                     | id                      | character varying        | NO          |                |
| public       | personas                     | nombre                  | character varying        | NO          |                |
| public       | personas                     | apellido_paterno        | character varying        | YES         |                |
| public       | personas                     | apellido_materno        | character varying        | YES         |                |
| public       | personas                     | rfc                     | character varying        | YES         |                |
| public       | personas                     | telefono                | character varying        | NO          |                |
| public       | personas                     | email                   | character varying        | YES         |                |
| public       | personas                     | estados_id              | character varying        | NO          |                |
| public       | personas                     | categoria_personas_id   | character varying        | NO          |                |
| public       | productos                    | id                      | character varying        | NO          |                |
| public       | productos                    | nombre                  | character varying        | NO          |                |
| public       | productos                    | categorias_productos_id | character varying        | NO          |                |
| public       | productos                    | proveedor_id            | character varying        | NO          |                |
| public       | productos                    | estados_id              | character varying        | NO          |                |
| public       | roles                        | id                      | character varying        | NO          |                |
| public       | roles                        | roles                   | character varying        | NO          |                |
| public       | tipo_movimientos             | id                      | character varying        | NO          |                |
| public       | tipo_movimientos             | movimiento              | character varying        | NO          |                |
| public       | ubicaciones                  | id                      | character varying        | NO          |                |
| public       | ubicaciones                  | nombre                  | character varying        | NO          |                |
| public       | ubicaciones                  | ubicacion               | character varying        | NO          |                |
| public       | usuarios                     | id                      | character varying        | NO          |                |
| public       | usuarios                     | nombre                  | character varying        | NO          |                |
| public       | usuarios                     | contrasena              | character varying        | NO          |                |
| public       | usuarios                     | telefono                | character varying        | NO          |                |
| public       | usuarios                     | roles_id                | character varying        | NO          |                |
| public       | usuarios                     | estados_id              | character varying        | NO          |                |
| public       | workspaces                   | id                      | character varying        | NO          |                |
| public       | workspaces                   | nombre                  | character varying        | NO          |                |
| public       | workspaces                   | permanente              | boolean                  | NO          |                |
