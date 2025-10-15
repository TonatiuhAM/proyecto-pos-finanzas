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
        Optional<Estados> estadoActivoOpt = estadosRepository.findFirstByEstado("Activo");
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
        System.out.println("🔧 [DEBUG] Iniciando cambio de estado - empleadoId: " + empleadoId + ", nuevoEstado: " + nuevoEstado);
        
        // Verificar que el empleado existe
        Optional<Usuarios> usuarioOpt = usuariosRepository.findById(empleadoId);
        if (usuarioOpt.isEmpty()) {
            System.err.println("❌ [ERROR] Empleado no encontrado con ID: " + empleadoId);
            throw new RuntimeException("Empleado no encontrado");
        }
        
        System.out.println("✅ [DEBUG] Empleado encontrado: " + usuarioOpt.get().getNombre());

        // Verificar que el estado existe (usar findFirstByEstado para evitar error de duplicados)
        Optional<Estados> estadoOpt = estadosRepository.findFirstByEstado(nuevoEstado);
        if (estadoOpt.isEmpty()) {
            System.err.println("❌ [ERROR] Estado no encontrado: " + nuevoEstado);
            throw new RuntimeException("El estado especificado no existe");
        }
        
        System.out.println("✅ [DEBUG] Estado encontrado: " + estadoOpt.get().getEstado());

        // Actualizar el estado del usuario
        Usuarios usuario = usuarioOpt.get();
        Estados estadoAnterior = usuario.getEstados();
        System.out.println("🔄 [DEBUG] Cambiando estado de '" + 
            (estadoAnterior != null ? estadoAnterior.getEstado() : "null") + "' a '" + nuevoEstado + "'");
            
        usuario.setEstados(estadoOpt.get());

        try {
            Usuarios usuarioActualizado = usuariosRepository.save(usuario);
            System.out.println("✅ [DEBUG] Estado actualizado exitosamente");
            return convertirAEmpleadoResponseDTO(usuarioActualizado);
        } catch (Exception e) {
            System.err.println("❌ [ERROR] Error al guardar usuario: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al actualizar el estado del empleado: " + e.getMessage());
        }
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
