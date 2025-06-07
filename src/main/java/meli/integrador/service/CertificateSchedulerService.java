package meli.integrador.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import meli.integrador.dto.CertificateResponse;
import meli.integrador.model.User;
import meli.integrador.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Serviço agendado para tarefas periódicas relacionadas a certificados VPN
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateSchedulerService {

    private final UserRepository userRepository;
    private final CertificateService certificateService;
    private final NotificationService notificationService;

    /**
     * Verifica e notifica sobre certificados que irão expirar em breve
     * Executa diariamente às 8h da manhã
     */
    @Scheduled(cron = "0 0 8 * * ?") // Todos os dias às 8h
    @Transactional(readOnly = true) // Good practice for read-heavy scheduled tasks
    public void checkExpiringCertificates() {
        log.info("Iniciando verificação de certificados prestes a expirar");

        LocalDateTime warningThreshold = LocalDateTime.now().plusDays(30);
        // Assuming findUsersWithCertificatesExpiringSoon takes the expiry date as parameter
        // and returns users whose certs expire BETWEEN now AND warningThreshold
        List<User> expiringUsers = userRepository.findUsersWithCertificatesExpiringSoon(warningThreshold);

        log.info("Encontrados {} usuários com certificados prestes a expirar", expiringUsers.size());

        for (User user : expiringUsers) {
            try {
                if (user.getVpnCertificateIdentifier() != null && user.getEmail() != null) { // Ensure user has cert and email
                    notifyCertificateExpiration(user);
                }
            } catch (Exception e) {
                log.error("Erro ao notificar sobre certificado prestes a expirar para o usuário: {}", user.getId(), e);
            }
        }
    }

    /**
     * Revoga automaticamente certificados expirados
     * Executa diariamente à meia-noite
     */
    @Scheduled(cron = "0 0 0 * * ?") // Todos os dias à meia-noite
    @Transactional // This method performs writes
    public void revokeExpiredCertificates() {
        log.info("Iniciando revogação de certificados expirados");

        // Need a method in UserRepository like: findByVpnEnabledTrueAndCertificateExpiryBefore(LocalDateTime.now())
        // For now, let's assume such a method exists or adapt if needed.
        // List<User> expiredUsers = userRepository.findByVpnEnabledTrueAndCertificateExpiryBefore(LocalDateTime.now());
        // As a temporary measure, or if the above is not available, we can iterate, but less efficient.
        // The CertificateService.listExpiredCertificates() returns DTOs, not entities, so not ideal here.
        // Let's use the existing deactivateExpiredCertificates which returns count and then fetch them if needed,
        // or better, have CertificateService.revokeCertificate handle UserNotFound gracefully or we fetch users first.

        // Ideal: Get users whose certs are expired and VPN is enabled
        List<User> usersToRevoke = userRepository.findAllByVpnEnabledTrueAndCertificateExpiryBefore(LocalDateTime.now());
        // Note: findAllByVpnEnabledTrueAndCertificateExpiryBefore needs to be added to UserRepository

        log.info("Encontrados {} certificados expirados para revogação", usersToRevoke.size());

        for (User user : usersToRevoke) {
            try {
                if (user.getVpnCertificateIdentifier() != null) {
                    certificateService.revokeCertificate(user.getVpnCertificateIdentifier());
                    log.info("Certificado revogado por expiração: {} para o usuário {}", user.getVpnCertificateIdentifier(), user.getUsername());
                }
            } catch (Exception e) {
                log.error("Erro ao revogar certificado expirado: {} para o usuário {}: {}", 
                        user.getVpnCertificateIdentifier(), user.getUsername(), e.getMessage(), e);
            }
        }
        // Alternative: userRepository.deactivateExpiredCertificates(); // This updates DB directly
        // If using deactivateExpiredCertificates(), ensure it aligns with revokeCertificate logic (e.g., script execution)
        // For now, revoking one by one via service is safer to ensure script execution.
    }

    /**
     * Gera relatório de certificados ativos
     * Executa toda segunda-feira às 9h
     */
    @Scheduled(cron = "0 0 9 ? * MON") // Toda segunda-feira às 9h
    public void generateCertificateReport() {
        log.info("Gerando relatório de certificados ativos");

        try {
            CertificateResponse.CertificateStats stats = certificateService.getCertificateStats();

            log.info("Relatório de certificados - Total: {}, Ativos: {}, Expirados: {}, Revogados (Aprox.): {}, Prestes a expirar (30d): {}",
                    stats.getTotalCertificates(), 
                    stats.getActiveCertificates(), 
                    stats.getExpiredCertificates(), 
                    stats.getRevokedCertificates(), 
                    stats.getExpiringSoonCount()); // Corrected method name

        } catch (Exception e) {
            log.error("Erro ao gerar relatório de certificados", e);
        }
    }

    /**
     * Envia notificação sobre certificado prestes a expirar
     */
    private void notifyCertificateExpiration(User user) { // Changed parameter from VpnCertificate to User
        try {
            // Details are fetched within notification if needed or use user object directly
            // CertificateResponse certInfo = certificateService.getCertificateDetails(user.getVpnCertificateIdentifier());
            // certInfo.getUserId() is the user's UUID.

            if (user.getCertificateExpiry() == null || user.getEmail() == null) {
                log.warn("Não é possível notificar usuário {} sobre expiração: dados incompletos (email ou data de expiração do certificado).", user.getId());
                return;
            }

            long daysToExpire = ChronoUnit.DAYS.between(LocalDateTime.now(), user.getCertificateExpiry());
            if (daysToExpire < 0) daysToExpire = 0; // Avoid negative days if already past

            String subject = String.format("Seu certificado VPN expira em %d dias", daysToExpire);
            String message = String.format(
                "Olá %s,\n\n" +
                "Seu certificado VPN (%s) irá expirar em %d dias (em %s).\n" +
                "Por favor, renove seu certificado o mais breve possível para evitar interrupções no serviço.\n\n" +
                "Atenciosamente,\nEquipe de Suporte VPN",
                user.getFullName() != null ? user.getFullName() : user.getUsername(), // Use full name if available
                user.getVpnCertificateIdentifier(),
                daysToExpire,
                user.getCertificateExpiry().toLocalDate() // Format date for readability
            );

            // Envia notificação por e-mail
            notificationService.sendEmail(
                user.getEmail(), // Send to user's email
                subject,
                message
            );

            log.info("Notificação de expiração enviada para o usuário: {}, email: {}", user.getId(), user.getEmail());

        } catch (Exception e) {
            log.error("Falha ao enviar notificação de expiração para o usuário: {}: {}",
                    user.getId(), e.getMessage(), e);
            // Do not rethrow; allow other notifications to proceed
        }
    }
}
