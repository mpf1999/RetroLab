package uoc.edu.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uoc.edu.dto.ChangeRoleRequestDTO;
import uoc.edu.dto.UserRequestDTO;
import uoc.edu.dto.UserResponseDTO;
import uoc.edu.exception.ForbiddenException;
import uoc.edu.exception.InvalidRequestException;
import uoc.edu.exception.ResourceAlreadyExistsException;
import uoc.edu.exception.ResourceInUseException;
import uoc.edu.exception.ResourceNotFoundException;
import uoc.edu.model.Console;
import uoc.edu.model.User;
import uoc.edu.repository.ConsoleRepository;
import uoc.edu.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class UserService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile(
                    "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
            );

    private final UserRepository userRepository;
    private final ConsoleRepository consoleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            ConsoleRepository consoleRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.consoleRepository = consoleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        return userRepository
                .findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(
            Long userId
    ) {
        User user =
                findUserEntityById(userId);

        return mapToResponseDTO(user);
    }

    @Transactional
    public UserResponseDTO addUser(
            UserRequestDTO request
    ) {
        String normalizedEmail =
                normalizeEmail(request.email());

        validateEmailAvailability(
                normalizedEmail,
                null
        );

        User user = new User();

        user.setName(request.name().trim());
        user.setEmail(normalizedEmail);

        user.setPasswordHash(
                passwordEncoder.encode(
                        request.password()
                )
        );

        user.setCreatedAt(LocalDateTime.now());
        user.setRole(request.role());

        User savedUser =
                userRepository.save(user);

        return mapToResponseDTO(savedUser);
    }

    @Transactional
    public UserResponseDTO updateUser(
            Long userId,
            UserRequestDTO request
    ) {
        User existingUser =
                findUserEntityById(userId);

        String normalizedEmail =
                normalizeEmail(request.email());

        validateEmailAvailability(
                normalizedEmail,
                userId
        );

        existingUser.setName(
                request.name().trim()
        );

        existingUser.setEmail(
                normalizedEmail
        );

        if (
                request.password() != null &&
                        !request.password().isBlank()
        ) {
            existingUser.setPasswordHash(
                    passwordEncoder.encode(
                            request.password()
                    )
            );
        }

        User updatedUser =
                userRepository.save(existingUser);

        return mapToResponseDTO(updatedUser);
    }

    @Transactional
    public UserResponseDTO updateProfileDetails(
            Long userId,
            String name,
            String email
    ) {
        User user =
                findUserEntityById(userId);

        if (!isAuthenticatedUser(user)) {
            throw new ForbiddenException(
                    "You can only update your own profile"
            );
        }

        if (name == null && email == null) {
            throw new InvalidRequestException(
                    "Provide a name or email to update"
            );
        }

        if (name != null) {
            String normalizedName =
                    name.trim();

            if (
                    normalizedName.length() < 2 ||
                            normalizedName.length() > 25
            ) {
                throw new InvalidRequestException(
                        "Name must contain between 2 and 25 characters"
                );
            }

            user.setName(normalizedName);
        }

        if (email != null) {
            String normalizedEmail =
                    normalizeEmail(email);

            validateEmailFormat(
                    normalizedEmail
            );

            validateEmailAvailability(
                    normalizedEmail,
                    userId
            );

            user.setEmail(normalizedEmail);
        }

        User updatedUser =
                userRepository.save(user);

        return mapToResponseDTO(updatedUser);
    }

    @Transactional
    public UserResponseDTO changeRole(
            Long userId,
            ChangeRoleRequestDTO request
    ) {
        User user =
                findUserEntityById(userId);

        // admin cannot stop being admin
        if (isAuthenticatedUser(user)) {
            throw new InvalidRequestException(
                    "You cannot change your own role"
            );
        }

        if (
                user.getRole() ==
                        request.role()
        ) {
            throw new InvalidRequestException(
                    "User already has this role"
            );
        }

        user.setRole(request.role());

        User updatedUser =
                userRepository.save(user);

        return mapToResponseDTO(updatedUser);
    }

    @Transactional
    public void deleteUser(Long userId) {

        User user = userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );

        List<Console> consoles =
                consoleRepository.findByOwnerId(userId);

        for (Console console : consoles) {

            if (
                    console.getExternalOwnerName() == null
                            || console.getExternalOwnerName().isBlank()
            ) {
                console.setExternalOwnerName(user.getName());
            }

            console.setOwner(null);
        }

        consoleRepository.saveAllAndFlush(consoles);
        userRepository.delete(user);
    }

    // check whether received user is the authenticated one
    private boolean isAuthenticatedUser(
            User user
    ) {
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (
                authentication == null ||
                        !authentication.isAuthenticated() ||
                        authentication.getName() == null
        ) {
            return false;
        }

        return user
                .getEmail()
                .equalsIgnoreCase(
                        authentication.getName()
                );
    }

    private void validateEmailAvailability(
            String email,
            Long currentUserId
    ) {
        userRepository
                .findByEmailIgnoreCase(email)
                .filter(existingUser ->
                        !existingUser
                                .getId()
                                .equals(currentUserId)
                )
                .ifPresent(existingUser -> {
                    throw new ResourceAlreadyExistsException(
                            "A user with this email already exists"
                    );
                });
    }

    private void validateEmailFormat(
            String email
    ) {
        if (
                email.isBlank() ||
                        !EMAIL_PATTERN
                                .matcher(email)
                                .matches()
        ) {
            throw new InvalidRequestException(
                    "Invalid email format"
            );
        }
    }

    private String normalizeEmail(
            String email
    ) {
        if (email == null) {
            throw new InvalidRequestException(
                    "Email cannot be null"
            );
        }

        return email
                .trim()
                .toLowerCase();
    }

    private User findUserEntityById(
            Long userId
    ) {
        return userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );
    }

    private UserResponseDTO mapToResponseDTO(
            User user
    ) {
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }
}