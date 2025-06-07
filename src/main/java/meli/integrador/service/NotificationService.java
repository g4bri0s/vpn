package meli.integrador.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import meli.integrador.model.User;
import meli.integrador.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Serviço para envio de notificações por e-mail
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender mailSender;
    private final UserService userService; // Assuming UserService has findById(UUID)

    @Value("${spring.mail.username:noreply@example.com}")
    private String fromEmailAddress;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
        "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    private static final Pattern UUID_PATTERN = 
        Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    private void validateEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("Email cannot be blank.");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }
    }

    private void validateUuid(String uuidString) {
        if (!StringUtils.hasText(uuidString)) {
            throw new IllegalArgumentException("User ID cannot be blank.");
        }
        if (!UUID_PATTERN.matcher(uuidString).matches()) {
            throw new IllegalArgumentException("Invalid User ID format (must be UUID): " + uuidString);
        }
    }

    private void validateStringNotBlank(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(fieldName + " cannot be blank.");
        }
    }
    
    /**
     * Envia um e-mail simples
     * 
     * @param toEmail Endereço de e-mail do destinatário
     * @param subject Assunto do e-mail
     * @param message Corpo da mensagem
     */
    @Async
    public void sendSimpleEmail(String toEmail, String subject, String message) {
        validateEmail(toEmail);
        validateStringNotBlank(subject, "Email subject");
        validateStringNotBlank(message, "Email message");

        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(toEmail);
            mailMessage.setSubject(subject);
            mailMessage.setText(message);
            mailMessage.setFrom(fromEmailAddress);
            
            mailSender.send(mailMessage);
            log.info("E-mail simples enviado para: {}", toEmail);
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail simples para: {}: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Falha ao enviar e-mail simples", e);
        }
    }
    
    /**
     * Envia um e-mail para um usuário pelo ID. Busca o e-mail do usuário e chama sendSimpleEmail.
     * 
     * @param userId ID do usuário destinatário (UUID format)
     * @param subject Assunto do e-mail
     * @param message Corpo da mensagem
     * @throws UserNotFoundException se o usuário não for encontrado.
     */
    @Async
    public void sendEmail(String userId, String subject, String message) {
        validateUuid(userId);
        validateStringNotBlank(subject, "Email subject");
        validateStringNotBlank(message, "Email message");
        
        try {
            User user = userService.findById(UUID.fromString(userId))
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
            
            if (!StringUtils.hasText(user.getEmail())) {
                log.error("Usuário {} não possui e-mail cadastrado.", userId);
                return; 
            }

            sendSimpleEmail(user.getEmail(), subject, message);
            log.info("E-mail (via sendEmail by ID) encaminhado para usuário {} (email: {})", userId, user.getEmail());
            
        } catch (UserNotFoundException e) {
            log.error("Falha ao enviar e-mail para o usuário ID {}: Usuário não encontrado.", userId);
            throw e; 
        } catch (Exception e) {
            log.error("Falha ao processar envio de e-mail para o usuário ID {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Falha ao enviar e-mail para o usuário", e);
        }
    }
}
