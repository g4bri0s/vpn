package meli.integrador.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import meli.integrador.common.UserRole;
import org.springframework.lang.NonNull;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade que representa um usuário do sistema de Painel VPN
 */
@Data
@Entity
@Getter
@Setter
@Table(name = "vpn_users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @NonNull
    @Column(unique = true, nullable = false)
    private String username;

    @NonNull
    @Column(nullable = false)
    private String fullName;

    @NonNull
    @Column(unique = true, nullable = false)
    private String email;

    @NonNull
    @Column(nullable = false)
    private String password;

    @Column(name = "vpn_certificate_identifier", length = 7, unique = true)
    private String vpnCertificateIdentifier;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "vpn_enabled")
    private boolean vpnEnabled = false;

    @Column(name = "certificate_id")
    private String certificateId;

    @Column(name = "certificate_expiry")
    private LocalDateTime certificateExpiry;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "login_attempts", nullable = false)
    private int loginAttempts = 0;

    @Column(name = "account_non_locked")
    private boolean accountNonLocked = true;

    @Column(name = "lock_time")
    private LocalDateTime lockTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role = UserRole.USER;

    public User() {
    }

    public User(String username, String fullName, String email, String password, UserRole role) {
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Increments the failed login attempts counter and locks the account if the maximum attempts are reached.
     * @param maxAttempts Maximum number of allowed failed login attempts
     * @return true if the account is now locked, false otherwise
     */
    public boolean incrementFailedLoginAttempts(int maxAttempts) {
        this.loginAttempts++;
        if (this.loginAttempts >= maxAttempts) {
            this.accountNonLocked = false;
            this.lockTime = LocalDateTime.now();
            return true;
        }
        return false;
    }

    /**
     * Resets the failed login attempts counter and unlocks the account.
     */
    public void resetLoginAttempts() {
        this.loginAttempts = 0;
        this.accountNonLocked = true;
        this.lockTime = null;
    }

    /**
     * Checks if the account is locked due to too many failed login attempts.
     * @param lockDurationMinutes Duration in minutes for which the account should remain locked
     * @return true if the account is locked, false otherwise
     */
    public boolean isAccountLocked(int lockDurationMinutes) {
        if (this.accountNonLocked) {
            return false;
        }
        
        if (this.lockTime == null) {
            return true;
        }
        
        // If lock duration has passed, automatically unlock the account
        if (this.lockTime.plusMinutes(lockDurationMinutes).isBefore(LocalDateTime.now())) {
            this.resetLoginAttempts();
            return false;
        }
        
        return true;
    }
}
