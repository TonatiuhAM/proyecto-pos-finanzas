package com.posfin.pos_finanzas_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.posfin.pos_finanzas_backend")
public class PosFinanzasBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(PosFinanzasBackendApplication.class, args);
	}
}