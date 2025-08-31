package com.posfin.pos_finanzas_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
@ComponentScan(basePackages = "com.posfin.pos_finanzas_backend")
public class PosFinanzasBackendApplication {

	public static void main(String[] args) {
		System.out.println("=== INICIANDO APLICACIÓN CON USERDETAILSSERVICE PERSONALIZADO ===");
		System.out.println("=== EXCLUDES: UserDetailsServiceAutoConfiguration ===");
		SpringApplication.run(PosFinanzasBackendApplication.class, args);
	}
}