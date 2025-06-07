package meli.integrador.repository;

import meli.integrador.model.VpnCertificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositório para operações de banco de dados relacionadas a certificados VPN
 */
@Repository
public interface VpnCertificateRepository extends JpaRepository<VpnCertificate, String> {
    
    /**
     * Encontra um certificado pelo seu ID único
     * 
     * @param certificateId ID do certificado
     * @return O certificado encontrado, se existir
     */
    Optional<VpnCertificate> findByCertificateId(String certificateId);
    
    /**
     * Encontra todos os certificados de um usuário, ordenados por data de emissão (mais recente primeiro)
     * 
     * @param userId ID do usuário
     * @return Lista de certificados do usuário
     */
    List<VpnCertificate> findByUserIdOrderByIssueDateDesc(String userId);
    
    /**
     * Verifica se existe um certificado com o ID e usuário fornecidos
     * 
     * @param certificateId ID do certificado
     * @param userId ID do usuário
     * @return true se o certificado existir e pertencer ao usuário, false caso contrário
     */
    boolean existsByCertificateIdAndUserId(String certificateId, String userId);
    
    /**
     * Conta quantos certificados ativos um usuário possui
     * 
     * @param userId ID do usuário
     * @param expiryDate Data atual para verificação de expiração
     * @return Número de certificados ativos
     */
    @Query("SELECT COUNT(c) FROM VpnCertificate c WHERE c.userId = :userId AND c.revoked = false AND c.expiryDate > :expiryDate")
    long countByUserIdAndActive(@Param("userId") String userId, @Param("expiryDate") LocalDateTime expiryDate);
    
    /**
     * Encontra certificados que irão expirar em breve
     * 
     * @param startDate Data de início para verificação
     * @param endDate Data de fim para verificação
     * @return Lista de certificados que expiram no período especificado
     */
    @Query("SELECT c FROM VpnCertificate c WHERE c.expiryDate BETWEEN :startDate AND :endDate AND c.revoked = false")
    List<VpnCertificate> findExpiringCertificates(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Encontra certificados expirados que ainda não foram revogados
     * 
     * @param currentDate Data atual para verificação de expiração
     * @return Lista de certificados expirados não revogados
     */
    @Query("SELECT c FROM VpnCertificate c WHERE c.expiryDate <= :currentDate AND c.revoked = false")
    List<VpnCertificate> findExpiredCertificates(@Param("currentDate") LocalDateTime currentDate);
    
    /**
     * Encontra certificados por status de revogação
     * 
     * @param revoked Status de revogação a ser filtrado
     * @return Lista de certificados com o status de revogação especificado
     */
    List<VpnCertificate> findByRevoked(boolean revoked);
    
    /**
     * Encontra certificados por usuário e status de revogação
     * 
     * @param userId ID do usuário
     * @param revoked Status de revogação a ser filtrado
     * @return Lista de certificados do usuário com o status de revogação especificado
     */
    List<VpnCertificate> findByUserIdAndRevoked(String userId, boolean revoked);
    
    /**
     * Conta o número de certificados não revogados
     * 
     * @return Número de certificados ativos
     */
    @Query("SELECT COUNT(c) FROM VpnCertificate c WHERE c.revoked = false")
    long countByRevokedFalse();
    
    /**
     * Conta o número de certificados não revogados que expiram após a data especificada
     * 
     * @param expiryDate Data de expiração para verificação
     * @return Número de certificados ativos que expiram após a data especificada
     */
    @Query("SELECT COUNT(c) FROM VpnCertificate c WHERE c.revoked = false AND c.expiryDate > :expiryDate")
    long countByRevokedFalseAndExpiryDateAfter(@Param("expiryDate") LocalDateTime expiryDate);
    
    /**
     * Conta o número de certificados não revogados que expiraram antes da data especificada
     * 
     * @param expiryDate Data de expiração para verificação
     * @return Número de certificados ativos que já expiraram
     */
    @Query("SELECT COUNT(c) FROM VpnCertificate c WHERE c.revoked = false AND c.expiryDate < :expiryDate")
    long countByRevokedFalseAndExpiryDateBefore(@Param("expiryDate") LocalDateTime expiryDate);
    
    /**
     * Conta o número de certificados com base no status de revogação
     * 
     * @param revoked Status de revogação a ser contado
     * @return Número de certificados com o status de revogação especificado
     */
    @Query("SELECT COUNT(c) FROM VpnCertificate c WHERE c.revoked = :revoked")
    long countByRevoked(@Param("revoked") boolean revoked);
    
    /**
     * Conta o número de certificados não revogados que expiram entre as datas especificadas
     * 
     * @param startDate Data de início do intervalo
     * @param endDate Data de fim do intervalo
     * @return Número de certificados ativos que expiram no intervalo especificado
     */
    @Query("SELECT COUNT(c) FROM VpnCertificate c WHERE c.revoked = false AND c.expiryDate BETWEEN :startDate AND :endDate")
    long countByRevokedFalseAndExpiryDateBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}
