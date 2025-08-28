package com.posfin.pos_finanzas_backend.controllers;

import com.posfin.pos_finanzas_backend.models.Usuarios;
import com.posfin.pos_finanzas_backend.models.Estados;
import com.posfin.pos_finanzas_backend.models.Roles;
import com.posfin.pos_finanzas_backend.dtos.UsuariosDTO;
import com.posfin.pos_finanzas_backend.dtos.LoginResponseDTO;
import com.posfin.pos_finanzas_backend.repositories.UsuariosRepository;
import com.posfin.pos_finanzas_backend.repositories.EstadosRepository;
import com.posfin.pos_finanzas_backend.repositories.RolesRepository;
import com.posfin.pos_finanzas_backend.services.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private EstadosRepository estadosRepository;

    @Autowired
    private RolesRepository rolesRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/create-admin")
    public ResponseEntity<?> createAdmin() {
        System.out.println("!!!!!! DEBUG: Creando usuario admin de prueba !!!!!!");
        try {
            // Verificar si ya existe el usuario
            Optional<Usuarios> existingUser = usuariosRepository.findByNombre("admin");
            if (existingUser.isPresent()) {
                return ResponseEntity.ok("Usuario admin ya existe");
            }

            // Crear estados si no existen
            Estados estadoActivo = estadosRepository.findById("activo-id").orElseGet(() -> {
                Estados estado = new Estados();
                estado.setId("activo-id");
                estado.setEstado("Activo");
                return estadosRepository.save(estado);
            });

            // Crear rol admin si no existe
            Roles rolAdmin = rolesRepository.findById("admin-role-id").orElseGet(() -> {
                Roles rol = new Roles();
                rol.setId("admin-role-id");
                rol.setRoles("Administrador");
                return rolesRepository.save(rol);
            });

            // Crear usuario admin básico
            Usuarios adminUser = new Usuarios();
            adminUser.setId("admin-user-id");
            adminUser.setNombre("admin");
            adminUser.setTelefono("555-0000");
            adminUser.setContrasena(passwordEncoder.encode("admin"));
            adminUser.setEstados(estadoActivo);
            adminUser.setRoles(rolAdmin);

            usuariosRepository.save(adminUser);

            return ResponseEntity.ok("Usuario admin creado exitosamente");
        } catch (Exception e) {
            System.err.println("Error creando usuario admin: " + e.getMessage());
            return ResponseEntity.badRequest().body("Error al crear usuario admin: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        System.out.println("!!!!!! DEBUG: Solicitud recibida en AuthController.login !!!!!!");
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

            // ✅ VALIDACIÓN CRÍTICA: Verificar que el usuario esté activo ANTES de verificar
            // la contraseña
            if (usuario.getEstados() == null || !"Activo".equals(usuario.getEstados().getEstado())) {
                return ResponseEntity.badRequest().body("Usuario inactivo");
            }

            // Verificar contraseña usando BCrypt para usuarios nuevos, fallback a texto
            // plano para usuarios existentes
            boolean contrasenaValida = false;
            if (usuario.getContrasena().startsWith("$2a$") || usuario.getContrasena().startsWith("$2b$")
                    || usuario.getContrasena().startsWith("$2y$")) {
                // Contraseña hasheada con BCrypt
                contrasenaValida = passwordEncoder.matches(contrasena, usuario.getContrasena());
            } else {
                // Contraseña en texto plano (para compatibilidad con usuarios existentes)
                contrasenaValida = contrasena.equals(usuario.getContrasena());
            }

            if (!contrasenaValida) {
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

            // Crear respuesta con LoginResponseDTO que incluye rol del usuario
            String rolNombre = (usuario.getRoles() != null) ? usuario.getRoles().getRoles() : "Sin Rol";
            String rolId = (usuario.getRoles() != null) ? usuario.getRoles().getId() : null;

            // Token expira en 24 horas (86400000 ms)
            long expiresIn = 24 * 60 * 60 * 1000;

            LoginResponseDTO response = new LoginResponseDTO(
                    token,
                    usuario.getNombre(),
                    rolNombre,
                    rolId,
                    expiresIn);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error en el login: " + e.getMessage());
        }
    }
}
