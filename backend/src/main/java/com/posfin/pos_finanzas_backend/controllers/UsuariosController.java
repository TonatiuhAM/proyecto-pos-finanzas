package com.posfin.pos_finanzas_backend.controllers;

import com.posfin.pos_finanzas_backend.models.Usuarios;
import com.posfin.pos_finanzas_backend.models.Roles;
import com.posfin.pos_finanzas_backend.models.Estados;
import com.posfin.pos_finanzas_backend.dtos.UsuariosDTO;
import com.posfin.pos_finanzas_backend.repositories.UsuariosRepository;
import com.posfin.pos_finanzas_backend.repositories.RolesRepository;
import com.posfin.pos_finanzas_backend.repositories.EstadosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
public class UsuariosController {
    @Autowired
    private UsuariosRepository usuariosRepository;

    @Autowired
    private RolesRepository rolesRepository;

    @Autowired
    private EstadosRepository estadosRepository;

    // Obtener todos los usuarios
    @GetMapping
    public List<UsuariosDTO> getAllUsuarios() {
        return usuariosRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Obtener un usuario por ID
    @GetMapping("/{id}")
    public ResponseEntity<UsuariosDTO> getUsuarioById(@PathVariable String id) {
        Optional<Usuarios> usuario = usuariosRepository.findById(id);
        return usuario.map(u -> ResponseEntity.ok(convertToDTO(u)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Crear un nuevo usuario
    @PostMapping
    public Usuarios createUsuario(@RequestBody Usuarios usuario) {
        // Validar que los roles y estados existan
        if (usuario.getRoles() != null && usuario.getRoles().getId() != null) {
            Roles roles = rolesRepository.findById(usuario.getRoles().getId())
                    .orElseThrow(() -> new RuntimeException("Roles no encontrado"));
            usuario.setRoles(roles);
        }

        if (usuario.getEstados() != null && usuario.getEstados().getId() != null) {
            Estados estados = estadosRepository.findById(usuario.getEstados().getId())
                    .orElseThrow(() -> new RuntimeException("Estados no encontrado"));
            usuario.setEstados(estados);
        }

        return usuariosRepository.save(usuario);
    }

    // Actualizar un usuario existente
    @PutMapping("/{id}")
    public ResponseEntity<Usuarios> updateUsuario(@PathVariable String id, @RequestBody Usuarios usuario) {
        if (!usuariosRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        // Validar que los roles y estados existan
        if (usuario.getRoles() != null && usuario.getRoles().getId() != null) {
            Roles roles = rolesRepository.findById(usuario.getRoles().getId())
                    .orElseThrow(() -> new RuntimeException("Roles no encontrado"));
            usuario.setRoles(roles);
        }

        if (usuario.getEstados() != null && usuario.getEstados().getId() != null) {
            Estados estados = estadosRepository.findById(usuario.getEstados().getId())
                    .orElseThrow(() -> new RuntimeException("Estados no encontrado"));
            usuario.setEstados(estados);
        }

        usuario.setId(id);
        Usuarios updatedUsuario = usuariosRepository.save(usuario);
        return ResponseEntity.ok(updatedUsuario);
    }

    // Eliminar un usuario
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUsuario(@PathVariable String id) {
        if (!usuariosRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        usuariosRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Método auxiliar para convertir Usuarios a UsuariosDTO
    private UsuariosDTO convertToDTO(Usuarios usuario) {
        UsuariosDTO dto = new UsuariosDTO();
        dto.setId(usuario.getId());
        dto.setNombre(usuario.getNombre());
        dto.setContrasena(usuario.getContrasena());
        dto.setTelefono(usuario.getTelefono());

        // Mapear relación con Roles
        if (usuario.getRoles() != null) {
            dto.setRolesId(usuario.getRoles().getId());
            dto.setRolesRoles(usuario.getRoles().getRoles());
        }

        // Mapear relación con Estados
        if (usuario.getEstados() != null) {
            dto.setEstadosId(usuario.getEstados().getId());
            dto.setEstadosEstado(usuario.getEstados().getEstado());
        }

        return dto;
    }
}
