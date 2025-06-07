package meli.integrador.repository;

import meli.integrador.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositório para operações de banco de dados relacionadas a usuários do Painel VPN
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Encontra um usuário pelo nome de usuário
     */
    Optional<User> findByUsername(String username);

    /**
     * Encontra um usuário pelo email
     */
    Optional<User> findByEmail(String email);

    /**
     * Verifica se existe um usuário com o nome de usuário fornecido
     */
    boolean existsByUsername(String username);

    /**
     * Verifica se existe um usuário com o email fornecido
     */
    boolean existsByEmail(String email);

    /**
     * Atualiza a data do último login do usuário
     */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.lastLogin = :lastLogin WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") UUID userId, @Param("lastLogin") LocalDateTime lastLogin);

    /**
     * Atualiza o status de ativação do usuário
     */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.isActive = :isActive, u.updatedAt = CURRENT_TIMESTAMP WHERE u.id = :userId")
    void updateUserStatus(@Param("userId") UUID userId, @Param("isActive") boolean isActive);

    /**
     * Atualiza as informações do certificado do usuário
     */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.vpnCertificateIdentifier = :vpnCertificateIdentifier, " +
           "u.certificateExpiry = :certificateExpiry, " +
           "u.vpnEnabled = true, " +
           "u.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE u.id = :userId")
    void updateCertificateInfo(
            @Param("userId") UUID userId,
            @Param("vpnCertificateIdentifier") String vpnCertificateIdentifier,
            @Param("certificateExpiry") LocalDateTime certificateExpiry);

    /**
     * Desativa todos os certificados expirados
     */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.vpnEnabled = false, " +
           "u.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE u.certificateExpiry < CURRENT_TIMESTAMP AND u.vpnEnabled = true")
    int deactivateExpiredCertificates();

    /**
     * Encontra usuários com certificados que irão expirar em breve
     */
    @Query("SELECT u FROM User u WHERE u.certificateExpiry IS NOT NULL " +
           "AND u.certificateExpiry BETWEEN CURRENT_TIMESTAMP AND :expiryDate " +
           "AND u.vpnEnabled = true")
    List<User> findUsersWithCertificatesExpiringSoon(@Param("expiryDate") LocalDateTime expiryDate);

    /**
     * Encontra todos os usuários ativos com VPN ativada
     */
    Page<User> findByIsActiveTrueAndVpnEnabledTrue(Pageable pageable);
    
    /**
     * Encontra usuários por papel (role)
     */
    Page<User> findByRole(String role, Pageable pageable);

    /**
     * Encontra um usuário pelo seu identificador de certificado VPN.
     *
     * @param vpnCertificateIdentifier O identificador do certificado VPN.
     * @return Um Optional contendo o usuário, se encontrado.
     */
    Optional<User> findByVpnCertificateIdentifier(String vpnCertificateIdentifier);

    /**
     * Verifica se existe um usuário com o identificador de certificado VPN fornecido.
     *
     * @param vpnCertificateIdentifier O identificador do certificado VPN.
     * @return true se um usuário com o identificador existir, false caso contrário.
     */
    boolean existsByVpnCertificateIdentifier(String vpnCertificateIdentifier);

    /**
     * Conta o número de usuários que possuem um identificador de certificado VPN não nulo.
     *
     * @return O número total de usuários com um identificador de certificado VPN.
     */
    long countByVpnCertificateIdentifierIsNotNull();

    /**
     * Conta o número de usuários com VPN ativa e cujo certificado expira após a data especificada.
     *
     * @param dateTime A data para comparar a expiração do certificado.
     * @return O número de usuários ativos com certificado válido após a data.
     */
    long countByVpnEnabledTrueAndCertificateExpiryAfter(LocalDateTime dateTime);

    /**
     * Conta o número de usuários com VPN ativa e cujo certificado expirou antes da data especificada.
     *
     * @param dateTime A data para comparar a expiração do certificado.
     * @return O número de usuários ativos com certificado expirado antes da data.
     */
    long countByVpnEnabledTrueAndCertificateExpiryBefore(LocalDateTime dateTime);

    /**
     * Conta o número de usuários que possuem um identificador de certificado VPN,
     * mas a VPN não está habilitada e o certificado expirou antes da data especificada.
     * Usado como uma aproximação para certificados revogados ou explicitamente desabilitados.
     *
     * @param dateTime A data para comparar a expiração do certificado.
     * @return O número de usuários correspondentes.
     */
    long countByVpnCertificateIdentifierIsNotNullAndVpnEnabledFalseAndCertificateExpiryBefore(LocalDateTime dateTime);

    /**
     * Conta o número de usuários com VPN ativa e cujo certificado expira dentro do intervalo de datas especificado.
     *
     * @param startDateTime O início do intervalo de datas.
     * @param endDateTime O fim do intervalo de datas.
     * @return O número de usuários com certificados expirando no intervalo.
     */
    long countByVpnEnabledTrueAndCertificateExpiryBetween(LocalDateTime startDateTime, LocalDateTime endDateTime);

    /**
     * Encontra todos os usuários com VPN ativa e cujo certificado expirou antes da data especificada.
     *
     * @param dateTime A data para comparar a expiração do certificado.
     * @return Uma lista de usuários com VPN ativa e certificado expirado.
     */
    List<User> findAllByVpnEnabledTrueAndCertificateExpiryBefore(LocalDateTime dateTime);
}
