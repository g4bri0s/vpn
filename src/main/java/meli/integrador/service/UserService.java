package meli.integrador.service;

import meli.integrador.common.UserRole;
import meli.integrador.dto.UserRequest;
import meli.integrador.dto.UserResponse;
import meli.integrador.exception.*;
import meli.integrador.model.User;
import meli.integrador.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
                    "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");
    private static final Pattern UUID_PATTERN = Pattern
            .compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    private void validateUuid(String uuidString, String fieldName) {
        if (!StringUtils.hasText(uuidString)) {
            throw new IllegalArgumentException(fieldName + " cannot be blank.");
        }
        if (!UUID_PATTERN.matcher(uuidString).matches()) {
            throw new IllegalArgumentException("Invalid " + fieldName + " format (must be UUID): " + uuidString);
        }
    }

    private void validateEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("Email cannot be blank.");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }
    }

    private void validateStringNotBlank(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(fieldName + " cannot be blank.");
        }
    }

    private void validateUserRequestForCreation(UserRequest userRequest) {
        Objects.requireNonNull(userRequest, "UserRequest cannot be null.");
        validateStringNotBlank(userRequest.getUsername(), "Username");
        validateEmail(userRequest.getEmail());
        validateStringNotBlank(userRequest.getPassword(), "Password");
        validateStringNotBlank(userRequest.getFullName(), "Full name");
        // Role validation: if role is provided in UserRequest as UserRole enum,
        // Jackson will handle invalid enum values during deserialization.
        // We just need to ensure it's not null if required by business logic,
        // or handle default assignment elsewhere if it can be optional.
        // For now, if it's not null, it's assumed to be a valid UserRole enum instance.
        if (userRequest.getRole() == null && isRoleRequiredForCreation()) {
            throw new IllegalArgumentException("Role is required for user creation.");
        }
    }

    private void validateUserRequestForUpdate(UserRequest userRequest) {
        Objects.requireNonNull(userRequest, "UserRequest cannot be null.");
        if (StringUtils.hasText(userRequest.getUsername())) {
            validateStringNotBlank(userRequest.getUsername(), "Username");
        }
        if (StringUtils.hasText(userRequest.getEmail())) {
            validateEmail(userRequest.getEmail());
        }
        if (StringUtils.hasText(userRequest.getFullName())) {
            validateStringNotBlank(userRequest.getFullName(), "Full name");
        }
        // Similar to creation, if role is present, it's already a UserRole enum.
        // Validation for nullity or specific roles can be done here if needed.
        // if (userRequest.getRole() != null) { /* further logic if needed */ }
    }

    private boolean isRoleRequiredForCreation() {
        return false;
    }

    private void validatePageable(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page index must not be less than zero.");
        }
        if (size < 1) {
            throw new IllegalArgumentException("Page size must not be less than one.");
        }
    }

    public UserResponse createUser(UserRequest userRequest) throws UserAlreadyExistException {
        validateUserRequestForCreation(userRequest);

        if (userRepository.existsByUsername(userRequest.getUsername())) {
            throw new UserAlreadyExistException("username", userRequest.getUsername());
        }
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new UserAlreadyExistException("email", userRequest.getEmail());
        }

        User user = userRequest.toUser();
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        if (user.getRole() == null) {
            user.setRole(UserRole.USER);
        }

        User savedUser = userRepository.save(user);
        return new UserResponse(savedUser);
    }

    public UserResponse getUserById(String id) throws UserNotFoundException {
        validateUuid(id, "User ID");
        User user = userRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new UserNotFoundException("id", id));
        return new UserResponse(user);
    }

    public Optional<User> findById(UUID userId) {
        Objects.requireNonNull(userId, "User ID (UUID) cannot be null.");
        return userRepository.findById(userId);
    }

    public UserResponse updateUser(String id, UserRequest userRequest) throws UserNotFoundException {
        validateUuid(id, "User ID");
        validateUserRequestForUpdate(userRequest);

        User existingUser = userRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new UserNotFoundException("id", id));

        userRequest.updateUser(existingUser, passwordEncoder);
        User updatedUser = userRepository.save(existingUser);
        return new UserResponse(updatedUser);
    }

    public void deleteUser(String id) throws UserNotFoundException {
        validateUuid(id, "User ID");
        UUID userId = UUID.fromString(id);
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("id", id);
        }
        userRepository.deleteById(userId);
    }

    public Page<UserResponse> listUsers(int page, int size) {
        validatePageable(page, size);
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAll(pageable).map(UserResponse::new);
    }

    public void updateUserStatus(String id, boolean isActive) throws UserNotFoundException {
        validateUuid(id, "User ID");
        UUID userId = UUID.fromString(id);
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("id", id);
        }
        userRepository.updateUserStatus(userId, isActive);
    }

    public void updateUserRole(String id, UserRole newRole) {
        User user = userRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new UserNotFoundException("id", id));
        user.setRole(newRole);
        userRepository.save(user);
    }
}
