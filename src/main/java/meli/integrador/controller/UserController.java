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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para gerenciamento de usuários do Painel VPN
 */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Usuários", description = "API para gerenciamento de usuários do Painel VPN")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Cria um novo usuário
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria um novo usuário")
    public UserResponse createUser(@Valid @RequestBody UserRequest userRequest)
            throws UserAlreadyExistException {
        return userService.createUser(userRequest);
    }

    /**
     * Obtém um usuário pelo ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and #id == authentication.principal.id)")
    @Operation(summary = "Obtém um usuário pelo ID", security = @SecurityRequirement(name = "bearerAuth"))
    public UserResponse getUserById(@PathVariable String id) throws UserNotFoundException {
        return userService.getUserById(id);
    }

    /**
     * Obtém um usuário pelo nome de usuário
     */
    @GetMapping("/username/{username}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and #username == authentication.principal.username)")
    @Operation(summary = "Obtém um usuário pelo nome de usuário", security = @SecurityRequirement(name = "bearerAuth"))
    public UserResponse getUserByUsername(@PathVariable String username) throws UserNotFoundException {
        return userService.getUserByUsername(username);
    }

    /**
     * Atualiza um usuário existente
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and #id == authentication.principal.id)")
    @Operation(summary = "Atualiza um usuário existente", security = @SecurityRequirement(name = "bearerAuth"))
    public UserResponse updateUser(
            @PathVariable String id,
            @Valid @RequestBody UserRequest userRequest) throws UserNotFoundException {
        return userService.updateUser(id, userRequest);
    }

    /**
     * Remove um usuário
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove um usuário", security = @SecurityRequirement(name = "bearerAuth"))
    public void deleteUser(@PathVariable String id) throws UserNotFoundException {
        userService.deleteUser(id);
    }

    /**
     * Lista todos os usuários com paginação
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lista todos os usuários com paginação", security = @SecurityRequirement(name = "bearerAuth"))
    public Page<UserResponse> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return userService.listUsers(page, size);
    }

    /**
     * Lista usuários por papel (role)
     */
    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lista usuários por papel (role)", security = @SecurityRequirement(name = "bearerAuth"))
    public Page<UserResponse> listUsersByRole(
            @PathVariable UserRole role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return userService.listUsersByRole(role, page, size);
    }

    /**
     * Lista usuários ativos com VPN ativada
     */
    @GetMapping("/active-vpn")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lista usuários ativos com VPN ativada", security = @SecurityRequirement(name = "bearerAuth"))
    public Page<UserResponse> listActiveUsersWithVpn(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return userService.listActiveUsersWithVpn(page, size);
    }

    /**
     * Atualiza o status de ativação de um usuário
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualiza o status de ativação de um usuário", security = @SecurityRequirement(name = "bearerAuth"))
    public void updateUserStatus(
            @PathVariable String id,
            @RequestParam boolean active) throws UserNotFoundException {
        userService.updateUserStatus(id, active);
    }

    /**
     * Atualiza o status da VPN de um usuário
     */
    @PatchMapping("/{id}/vpn-status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualiza o status da VPN de um usuário", security = @SecurityRequirement(name = "bearerAuth"))
    public void updateVpnStatus(
            @PathVariable String id,
            @RequestParam boolean enabled) throws UserNotFoundException {
        userService.updateVpnStatus(id, enabled);
    }

    /**
     * Desativa certificados expirados
     */
    @PostMapping("/certificates/expired/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Desativa certificados expirados", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<String> deactivateExpiredCertificates() {
        int count = userService.deactivateExpiredCertificates();
        return ResponseEntity.ok("Certificados expirados desativados: " + count);
    }

    /**
     * Encontra usuários com certificados que irão expirar em breve
     */
    @GetMapping("/certificates/expiring-soon")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Encontra usuários com certificados que irão expirar em breve", security = @SecurityRequirement(name = "bearerAuth"))
    public List<UserResponse> findUsersWithCertificatesExpiringSoon(
            @RequestParam(defaultValue = "7") int days) {
        return userService.findUsersWithCertificatesExpiringSoon(days);
    }
}
