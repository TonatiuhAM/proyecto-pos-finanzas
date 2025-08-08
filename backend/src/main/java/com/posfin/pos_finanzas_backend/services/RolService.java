package com.posfin.pos_finanzas_backend.services;

import com.posfin.pos_finanzas_backend.dtos.RolResponseDTO;
import com.posfin.pos_finanzas_backend.models.Roles;
import com.posfin.pos_finanzas_backend.repositories.RolesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class RolService {

    @Autowired
    private RolesRepository rolesRepository;

    /**
     * Obtener todos los roles disponibles para el dropdown
     */
    public List<RolResponseDTO> obtenerTodosLosRoles() {
        List<Roles> roles = rolesRepository.findAll();

        return roles.stream()
                .map(this::convertirARolResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener rol por ID
     */
    public RolResponseDTO obtenerRolPorId(Long rolId) {
        Optional<Roles> rolOpt = rolesRepository.findById(rolId.toString());
        if (rolOpt.isEmpty()) {
            throw new IllegalArgumentException("Rol no encontrado");
        }

        return convertirARolResponseDTO(rolOpt.get());
    }

    /**
     * Método helper para convertir entidad Rol a DTO de respuesta
     */
    private RolResponseDTO convertirARolResponseDTO(Roles rol) {
        return new RolResponseDTO(
                rol.getId(),
                rol.getRoles());
    }
}
