package com.posfin.pos_finanzas_backend.controllers;

import com.posfin.pos_finanzas_backend.models.Usuarios;
import com.posfin.pos_finanzas_backend.dtos.UsuariosDTO;
import com.posfin.pos_finanzas_backend.repositories.UsuariosRepository;
import com.posfin.pos_finanzas_backend.services.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UsuariosRepository usuariosRepository;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        try {
            String nombre = credentials.get("nombre");
            String contrasena = credentials.get("contrasena");

            if (nombre == null || contrasena == null) {
                return ResponseEntity.badRequest().body("Nombre de usuario y contraseña son requeridos");
            }

            // Buscar usuario por nombre
            Optional<Usuarios> usuarioOpt = usuariosRepository.findByNombre(nombre);

            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Usuario no encontrado");
            }

            Usuarios usuario = usuarioOpt.get();

            // Verificar contraseña (en un entorno real deberías usar hashing)
            if (!contrasena.equals(usuario.getContrasena())) {
                return ResponseEntity.badRequest().body("Contraseña incorrecta");
            }

            // Crear DTO para respuesta (sin incluir la contraseña)
            UsuariosDTO dto = new UsuariosDTO();
            dto.setId(usuario.getId());
            dto.setNombre(usuario.getNombre());
            dto.setTelefono(usuario.getTelefono());

            if (usuario.getRoles() != null) {
                dto.setRolesId(usuario.getRoles().getId());
                dto.setRolesRoles(usuario.getRoles().getRoles());
            }

            if (usuario.getEstados() != null) {
                dto.setEstadosId(usuario.getEstados().getId());
                dto.setEstadosEstado(usuario.getEstados().getEstado());
            }

            // Generar el token JWT
            String token = jwtService.generateToken(usuario.getNombre(), usuario.getId());

            // Crear respuesta con el usuario y el token
            Map<String, Object> response = new HashMap<>();
            response.put("usuario", dto);
            response.put("token", token);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error en el login: " + e.getMessage());
        }
    }
}
