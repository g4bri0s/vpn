package meli.integrador.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import meli.integrador.common.UserRole;
import meli.integrador.model.User;
import meli.integrador.validation.ValidationGroups;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * DTO para resposta de usuários do Painel VPN.
 * Contém informações do usuário que podem ser expostas via API.
 */
/**
 * DTO que representa um usuário do sistema de Painel VPN.
 * Contém informações do usuário que podem ser expostas via API.
 * Inclui validações para garantir a integridade dos dados.
 */
@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Representa um usuário do sistema de Painel VPN")
public class UserResponse {
    
    public static final String USERNAME_PATTERN = "^[a-zA-Z0-9._-]{3,50}$";
    public static final String NAME_PATTERN = "^[\\p{L} .'-]+";
    public static final String EMAIL_PATTERN = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

    @Schema(
        description = "Identificador único do usuário",
        example = "123e4567-e89b-12d3-a456-426614174000",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    @JsonProperty(access = Access.READ_ONLY)
    private String id;

    @NotBlank(groups = {ValidationGroups.CreateUser.class}, message = "{user.username.required}")
    @Size(min = 3, max = 50, message = "{user.username.size}")
    @Pattern(regexp = USERNAME_PATTERN, message = "{user.username.invalid}")
    @Schema(
        description = "Nome de usuário único. Deve conter apenas letras, números, pontos, sublinhados e hífens.",
        example = "joaosilva",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String username;

    @NotBlank(groups = {ValidationGroups.CreateUser.class}, message = "{user.fullname.required}")
    @Size(min = 3, max = 100, message = "{user.fullname.size}")
    @Pattern(regexp = NAME_PATTERN, message = "{user.fullname.invalid}")
    @Schema(
        description = "Nome completo do usuário. Deve conter apenas letras, espaços, apóstrofos e hífens.",
        example = "João da Silva",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String fullName;

    @NotBlank(groups = {ValidationGroups.CreateUser.class}, message = "{user.email.required}")
    @Email(message = "{user.email.invalid}")
    @Pattern(regexp = EMAIL_PATTERN, message = "{user.email.invalid}")
    @Schema(
        description = "Endereço de e-mail do usuário",
        example = "usuario@exemplo.com",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String email;

    @NotNull(groups = {ValidationGroups.AdminOperation.class}, message = "{user.role.required}")
    @Schema(
        description = "Papel do usuário no sistema. Define as permissões de acesso.",
        requiredMode = Schema.RequiredMode.REQUIRED,
        defaultValue = "USER"
    )
    private UserRole role = UserRole.USER;

    @Schema(
        description = "Indica se a conta do usuário está ativa",
        example = "true",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    @JsonProperty(access = Access.READ_ONLY)
    private boolean isActive = true;

    @Schema(
        description = "Indica se o usuário tem permissão para usar a VPN",
        example = "false",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    @JsonProperty(access = Access.READ_ONLY)
    private boolean vpnEnabled = false;

    @Schema(
        description = "ID do certificado digital do usuário, se existir",
        example = "cert-12345",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    @JsonProperty(access = Access.READ_ONLY)
    private String certificateId;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(
        description = "Data de expiração do certificado digital",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    @JsonProperty(access = Access.READ_ONLY)
    private LocalDateTime certificateExpiry;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(
        description = "Data do último login do usuário",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    @JsonProperty(access = Access.READ_ONLY)
    private LocalDateTime lastLogin;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(
        description = "Data de criação do usuário",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    @JsonProperty(access = Access.READ_ONLY)
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(
        description = "Data da última atualização do usuário",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    @JsonProperty(access = Access.READ_ONLY)
    private LocalDateTime updatedAt;

    @Schema(
        description = "Identificador único de 7 caracteres para o certificado VPN do usuário, se existir",
        example = "XYZ1234",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    @JsonProperty(access = Access.READ_ONLY)
    private String vpnCertificateIdentifier;

    /**
     * Construtor que cria um UserResponse a partir de uma entidade User.
     *
     * @param user Entidade User a ser convertida
     * @throws IllegalArgumentException se o usuário for nulo
     */
    public UserResponse(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User não pode ser nulo");
        }
        this.id = user.getId().toString(); 
        this.username = user.getUsername();
        this.fullName = user.getFullName();
        this.email = user.getEmail();
        this.role = user.getRole(); // Explicitly using getter, though Lombok should do this
        this.isActive = user.isActive();
        this.vpnEnabled = user.isVpnEnabled();
        this.certificateId = user.getCertificateId();
        this.certificateExpiry = user.getCertificateExpiry();
        this.vpnCertificateIdentifier = user.getVpnCertificateIdentifier();
        this.lastLogin = user.getLastLogin();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
    }

    /**
     * Verifica se este objeto é igual a outro objeto.
     * Dois UserResponse são considerados iguais se tiverem o mesmo ID, username ou email.
     *
     * @param o objeto a ser comparado
     * @return true se os objetos forem iguais, false caso contrário
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserResponse that = (UserResponse) o;
        
        // Verifica se algum dos identificadores principais é igual
        if (id != null && id.equals(that.id)) return true;
        if (username != null && username.equalsIgnoreCase(that.username)) return true;
        return email != null && email.equalsIgnoreCase(that.email);
    }

    /**
     * Retorna o código hash para este objeto.
     * O código hash é baseado no ID, username e email do usuário.
     *
     * @return o código hash
     */
    @Override
    public int hashCode() {
        return Objects.hash(
            id != null ? id.toLowerCase() : null,
            username != null ? username.toLowerCase() : null,
            email != null ? email.toLowerCase() : null
        );
    }
    
    /**
     * Verifica se o usuário tem um papel específico.
     *
     * @param role o papel a ser verificado
     * @return true se o usuário tiver o papel especificado, false caso contrário
     */
    public boolean hasRole(UserRole role) {
        return this.role != null && this.role == role;
    }
    
    /**
     * Verifica se o usuário tem permissão de administrador.
     *
     * @return true se o usuário for um administrador, false caso contrário
     */
    public boolean isAdmin() {
        return hasRole(UserRole.ADMIN);
    }
    
    /**
     * Verifica se o usuário tem um certificado válido (não expirado).
     *
     * @return true se o usuário tiver um certificado válido, false caso contrário
     */
    public boolean hasValidCertificate() {
        if (certificateId == null || certificateExpiry == null) {
            return false;
        }
        return certificateExpiry.isAfter(LocalDateTime.now());
    }
}
