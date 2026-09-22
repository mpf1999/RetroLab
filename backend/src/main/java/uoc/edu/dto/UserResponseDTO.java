package uoc.edu.dto;

import uoc.edu.model.Role;

public record UserResponseDTO(Long userId, String name, String email, Role role) {
}
