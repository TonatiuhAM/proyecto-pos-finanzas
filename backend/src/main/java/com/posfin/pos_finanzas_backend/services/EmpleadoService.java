package com.posfin.pos_finanzas_backend.services;

import com.posfin.pos_finanzas_backend.dtos.EmpleadoCreateRequestDTO;
import com.posfin.pos_finanzas_backend.dtos.EmpleadoResponseDTO;
import com.posfin.pos_finanzas_backend.models.Estados;
import com.posfin.pos_finanzas_backend.models.Roles;
import com.posfin.pos_finanzas_backend.models.Usuarios;
import com.posfin.pos_finanzas_backend.repositories.EstadosRepository;
import com.posfin.pos_finanzas_backend.repositories.RolesRepository;
import com.posfin.pos_finanzas_backend.repositories.UsuariosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class EmpleadoService {

    @Autowired
    private UsuariosRepository usuariosRepository;

    @Autowired
    private RolesRepository rolesRepository;

    @Autowired
    private EstadosRepository estadosRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Obtener todos los empleados con información de rol y estado
     */
    public List<EmpleadoResponseDTO> obtenerTodosLosEmpleados() {
        List<Usuarios> usuarios = usuariosRepository.findAll();

        return usuarios.stream()
                .map(this::convertirAEmpleadoResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Crear un nuevo empleado
     */
    public EmpleadoResponseDTO crearEmpleado(EmpleadoCreateRequestDTO empleadoRequest) {
        // Validar que el rol existe
        Optional<Roles> rolOpt = rolesRepository.findById(empleadoRequest.getRolId());
        if (rolOpt.isEmpty()) {
            throw new RuntimeException("El rol especificado no existe");
        }

        // Obtener el estado "Activo" (por defecto para nuevos empleados)
        Optional<Estados> estadoActivoOpt = estadosRepository.findByEstado("Activo");
        if (estadoActivoOpt.isEmpty()) {
            throw new RuntimeException("No se encontró el estado 'Activo' en la base de datos");
        }

        // Verificar que no existe otro usuario con el mismo nombre
        Optional<Usuarios> usuarioExistente = usuariosRepository.findByNombre(empleadoRequest.getNombre());
        if (usuarioExistente.isPresent()) {
            throw new RuntimeException("Ya existe un usuario con ese nombre");
        }

        // Crear nuevo usuario
        Usuarios nuevoUsuario = new Usuarios();
        nuevoUsuario.setNombre(empleadoRequest.getNombre());
        nuevoUsuario.setTelefono(empleadoRequest.getTelefono());

        // Hashear la contraseña antes de guardarla
        String contrasenaHasheada = passwordEncoder.encode(empleadoRequest.getContrasena());
        nuevoUsuario.setContrasena(contrasenaHasheada);

        nuevoUsuario.setRoles(rolOpt.get());
        nuevoUsuario.setEstados(estadoActivoOpt.get());

        // Guardar el usuario
        Usuarios usuarioGuardado = usuariosRepository.save(nuevoUsuario);

        // Retornar DTO de respuesta
        return convertirAEmpleadoResponseDTO(usuarioGuardado);
    }

    /**
     * Cambiar el estado de un empleado
     */
    public EmpleadoResponseDTO cambiarEstadoEmpleado(String empleadoId, String nuevoEstado) {
        // Verificar que el empleado existe
        Optional<Usuarios> usuarioOpt = usuariosRepository.findById(empleadoId);
        if (usuarioOpt.isEmpty()) {
            throw new RuntimeException("Empleado no encontrado");
        }

        // Verificar que el estado existe
        Optional<Estados> estadoOpt = estadosRepository.findByEstado(nuevoEstado);
        if (estadoOpt.isEmpty()) {
            throw new RuntimeException("El estado especificado no existe");
        }

        // Actualizar el estado del usuario
        Usuarios usuario = usuarioOpt.get();
        usuario.setEstados(estadoOpt.get());

        Usuarios usuarioActualizado = usuariosRepository.save(usuario);

        return convertirAEmpleadoResponseDTO(usuarioActualizado);
    }

    /**
     * Obtener empleado por ID
     */
    public EmpleadoResponseDTO obtenerEmpleadoPorId(String empleadoId) {
        Optional<Usuarios> usuarioOpt = usuariosRepository.findById(empleadoId);
        if (usuarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Empleado no encontrado");
        }

        return convertirAEmpleadoResponseDTO(usuarioOpt.get());
    }

    /**
     * Método helper para convertir entidad Usuario a DTO de respuesta
     */
    private EmpleadoResponseDTO convertirAEmpleadoResponseDTO(Usuarios usuario) {
        return new EmpleadoResponseDTO(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getTelefono(),
                usuario.getRoles().getId(),
                usuario.getRoles().getRoles(),
                usuario.getEstados().getId(),
                usuario.getEstados().getEstado());
    }
}
