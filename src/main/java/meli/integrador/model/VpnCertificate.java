package meli.integrador.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidade que representa um certificado VPN no sistema
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "vpn_certificates")
public class VpnCertificate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "certificate_id", unique = true, nullable = false)
    private String certificateId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "common_name", nullable = false)
    private String commonName;

    @Column(name = "serial_number", unique = true, nullable = false)
    private String serialNumber;

    @Column(name = "fingerprint", unique = true, nullable = false)
    private String fingerprint;

    @Column(name = "issue_date", nullable = false)
    private LocalDateTime issueDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @Column(name = "revoked", nullable = false)
    private boolean revoked;

    @Column(name = "revocation_date")
    private LocalDateTime revocationDate;
    
    @Column(name = "revocation_reason")
    private String revocationReason;

    @Column(name = "key_algorithm", nullable = false)
    private String keyAlgorithm;

    @Column(name = "key_size", nullable = false)
    private int keySize;

    @Column(name = "issuer", nullable = false)
    private String issuer;

    @Column(name = "certificate_type", nullable = false)
    private String certificateType;

    // Getters e Setters são gerados pelo Lombok
    
    /**
     * Verifica se o certificado está expirado
     * @return true se o certificado estiver expirado, false caso contrário
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }
    
    /**
     * Verifica se o certificado está ativo (não revogado e não expirado)
     * @return true se o certificado estiver ativo, false caso contrário
     */
    public boolean isActive() {
        return !revoked && !isExpired();
    }
}
