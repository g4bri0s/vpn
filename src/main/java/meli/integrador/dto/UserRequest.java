package meli.integrador.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import meli.integrador.common.UserRole;
import meli.integrador.model.User;
import meli.integrador.validation.ValidationGroups;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

/**
 * DTO para requisições de criação/atualização de usuários do Painel VPN.
 * Inclui validações para garantir a integridade dos dados fornecidos.
 */
@Data
@Schema(description = "DTO para requisições de criação/atualização de usuários")
public class UserRequest {

    @NotBlank(
        groups = {ValidationGroups.CreateUser.class, ValidationGroups.UpdateUser.class}, 
        message = "{user.username.required}"
    )
    @Size(min = 3, max = 50, message = "{user.username.size}")
    @Pattern(regexp = UserResponse.USERNAME_PATTERN, message = "{user.username.invalid}")
    @Schema(
        description = "Nome de usuário único. Deve conter apenas letras, números, pontos, sublinhados e hífens.",
        example = "joaosilva",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String username;

    @NotBlank(
        groups = {ValidationGroups.CreateUser.class, ValidationGroups.UpdateUser.class},
        message = "{user.fullname.required}"
    )
    @Size(min = 3, max = 100, message = "{user.fullname.size}")
    @Pattern(regexp = UserResponse.NAME_PATTERN, message = "{user.fullname.invalid}")
    @Schema(
        description = "Nome completo do usuário. Deve conter apenas letras, espaços, apóstrofos e hífens.",
        example = "João da Silva",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String fullName;

    @NotBlank(
        groups = {ValidationGroups.CreateUser.class, ValidationGroups.UpdateUser.class},
        message = "{user.email.required}"
    )
    @Email(message = "{user.email.invalid}")
    @Pattern(regexp = UserResponse.EMAIL_PATTERN, message = "{user.email.invalid}")
    @Schema(
        description = "Endereço de e-mail do usuário",
        example = "usuario@exemplo.com",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String email;

    @NotBlank(groups = ValidationGroups.CreateUser.class, message = "{user.password.required}")
    @Size(min = 8, max = 100, message = "{user.password.size}")
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{8,}$",
        message = "{user.password.weak}",
        groups = {ValidationGroups.CreateUser.class, ValidationGroups.UpdateUser.class}
    )
    @Schema(
        description = "Senha do usuário. Deve conter pelo menos 8 caracteres, incluindo letras maiúsculas, minúsculas, números e caracteres especiais.",
        example = "Senha@123",
        requiredMode = Schema.RequiredMode.REQUIRED,
        accessMode = Schema.AccessMode.WRITE_ONLY
    )
    @JsonProperty(access = Access.WRITE_ONLY)
    private String password;

    @NotNull(groups = ValidationGroups.AdminOperation.class, message = "{user.role.required}")
    @Schema(
        description = "Papel do usuário no sistema. Define as permissões de acesso.",
        requiredMode = Schema.RequiredMode.REQUIRED,
        defaultValue = "USER"
    )
    private UserRole role = UserRole.USER;
    
    @Schema(
        description = "Indica se o usuário tem permissão para usar a VPN",
        example = "false",
        defaultValue = "false"
    )
    private boolean vpnEnabled = false;

    /**
     * Converte este DTO para uma entidade User.
     * A senha não é codificada aqui, pois isso deve ser feito no serviço.
     *
     * @return uma nova instância de User com os dados deste DTO
     */
    public User toUser() {
        User user = new User();
        user.setUsername(this.username);
        user.setFullName(this.fullName);
        user.setEmail(this.email);
        user.setPassword(this.password); // A senha será codificada no serviço
        user.setRole(this.role);
        user.setVpnEnabled(this.vpnEnabled);
        user.setActive(true); // Novo usuário é ativado por padrão
        return user;
    }

    /**
     * Atualiza uma entidade User existente com os dados deste DTO.
     * A senha será codificada apenas se for fornecida.
     *
     * @param user a entidade User a ser atualizada
     * @param passwordEncoder codificador de senha
     * @return a entidade User atualizada
     */
    public User updateUser(User user, PasswordEncoder passwordEncoder) {
        if (this.fullName != null && !this.fullName.trim().isEmpty()) {
            user.setFullName(this.fullName.trim());
        }
        
        if (this.email != null && !this.email.trim().isEmpty()) {
            user.setEmail(this.email.trim().toLowerCase());
        }
        
        if (this.password != null && !this.password.trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(this.password));
        }
        
        // Apenas administradores podem alterar o papel do usuário
        if (this.role != null && user.getRole() != this.role) {
            user.setRole(this.role);
        }
        
        if (this.username != null && !this.username.trim().isEmpty()) {
            user.setUsername(this.username.trim().toLowerCase());
        }
        
        user.setVpnEnabled(this.vpnEnabled);
        return user;
    }
}
