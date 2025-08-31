package com.posfin.pos_finanzas_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;

@SpringBootApplication(exclude = { UserDetailsServiceAutoConfiguration.class })
public class PosFinanzasBackendApplication {

	public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(PosFinanzasBackendApplication.class, args);
		
		// Log de diagnóstico: mostrar todos los controllers registrados
		System.out.println("🔍 [STARTUP-DEBUG] === CONTROLLERS REGISTRADOS ===");
		String[] beanNames = context.getBeanDefinitionNames();
		for (String beanName : beanNames) {
			try {
				Object bean = context.getBean(beanName);
				if (bean.getClass().isAnnotationPresent(RestController.class)) {
					System.out.println("🔍 [STARTUP-DEBUG] Controller encontrado: " + beanName + " (" + bean.getClass().getSimpleName() + ")");
					
					// Mostrar mapping de la clase
					RequestMapping classMapping = bean.getClass().getAnnotation(RequestMapping.class);
					if (classMapping != null && classMapping.value().length > 0) {
						System.out.println("🔍 [STARTUP-DEBUG]   - Base path: " + String.join(", ", classMapping.value()));
					}
					
					// Mostrar métodos con mappings
					Method[] methods = bean.getClass().getDeclaredMethods();
					for (Method method : methods) {
						if (method.isAnnotationPresent(org.springframework.web.bind.annotation.GetMapping.class)) {
							org.springframework.web.bind.annotation.GetMapping getMapping = 
								method.getAnnotation(org.springframework.web.bind.annotation.GetMapping.class);
							String[] paths = getMapping.value();
							if (paths.length > 0) {
								System.out.println("🔍 [STARTUP-DEBUG]     - GET " + String.join(", ", paths) + " -> " + method.getName() + "()");
							} else {
								System.out.println("🔍 [STARTUP-DEBUG]     - GET (root) -> " + method.getName() + "()");
							}
						}
					}
				}
			} catch (Exception e) {
				// Ignorar errores al inspeccionar beans
			}
		}
		System.out.println("🔍 [STARTUP-DEBUG] === FIN DE CONTROLLERS REGISTRADOS ===");
	}
}
}