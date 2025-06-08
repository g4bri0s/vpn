package meli.integrador.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import meli.integrador.common.UserRole;
import meli.integrador.dto.UserRequest;
import meli.integrador.dto.UserResponse;
import meli.integrador.exception.UserAlreadyExistException;
import meli.integrador.exception.UserNotFoundException;
import meli.integrador.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Usuários", description = "API para gerenciamento de usuários do Painel VPN")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria um novo usuário")
    public UserResponse createUser(@Valid @RequestBody UserRequest userRequest)
            throws UserAlreadyExistException {
        return userService.createUser(userRequest);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and #id == authentication.principal.id)")
    @Operation(summary = "Atualiza um usuário existente", security = @SecurityRequirement(name = "bearerAuth"))
    public UserResponse updateUser(
            @PathVariable String id,
            @Valid @RequestBody UserRequest userRequest) throws UserNotFoundException {
        return userService.updateUser(id, userRequest);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove um usuário", security = @SecurityRequirement(name = "bearerAuth"))
    public void deleteUser(@PathVariable String id) throws UserNotFoundException {
        userService.deleteUser(id);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lista todos os usuários com paginação", security = @SecurityRequirement(name = "bearerAuth"))
    public Page<UserResponse> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return userService.listUsers(page, size);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualiza o status de ativação de um usuário", security = @SecurityRequirement(name = "bearerAuth"))
    public void updateUserStatus(
            @PathVariable String id,
            @RequestParam boolean active) throws UserNotFoundException {
        userService.updateUserStatus(id, active);
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualiza o papel (role) de um usuário", security = @SecurityRequirement(name = "bearerAuth"))
    public void updateUserRole(
            @PathVariable String id,
            @RequestBody String roleString) throws UserNotFoundException {

        if (roleString == null || roleString.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role não pode ser vazio");
        }

        UserRole newRole;
        try {
            newRole = UserRole.valueOf(roleString.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role inválido: " + roleString);
        }

        userService.updateUserRole(id, newRole);
    }
}
