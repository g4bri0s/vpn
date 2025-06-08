package meli.integrador.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import meli.integrador.dto.CertificateResponse;
import meli.integrador.exception.CertificateGenerationException;
import meli.integrador.exception.UserNotFoundException;
import meli.integrador.model.User;
import meli.integrador.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

/**
 * Serviço de gerenciamento de certificados VPN
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateService {

    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String ALL_CHARS = UPPER + DIGITS;
    private static final int ID_LENGTH = 7;
    private static final int MAX_ID_GENERATION_ATTEMPTS = 10;

    private final UserRepository userRepository;
    private final Random random = new SecureRandom();

    @Value("${vpn.script.path:/etc/openvpn/scripts/cert.sh}")
    private String scriptPath;

    @Value("${vpn.certificates.directory:/etc/openvpn/clients}")
    private String certificatesDirectory;

    // Helper validation methods
    private void validateUserUuid(String userUuid) {
        Objects.requireNonNull(userUuid, "User UUID cannot be null.");
        if (!StringUtils.hasText(userUuid)) {
            throw new IllegalArgumentException("User UUID cannot be blank.");
        }
        try {
            UUID.fromString(userUuid); // Validate UUID format
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid User UUID format: " + userUuid, e);
        }
    }

    private void validateVpnCertificateIdentifier(String vpnCertId) {
        Objects.requireNonNull(vpnCertId, "VPN certificate identifier cannot be null.");
        if (!StringUtils.hasText(vpnCertId)) {
            throw new IllegalArgumentException("VPN certificate identifier cannot be blank.");
        }
        if (vpnCertId.length() != ID_LENGTH) {
            throw new IllegalArgumentException("VPN certificate identifier must be " + ID_LENGTH + " characters long.");
        }
        if (!vpnCertId.matches("^[A-Z][A-Z0-9]{6}$")) {
            throw new IllegalArgumentException(
                    "VPN certificate identifier must start with an uppercase letter and be followed by 6 uppercase letters or digits.");
        }
    }

    private void validatePositiveInteger(Integer value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " cannot be null.");
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive.");
        }
    }

    private String generateUniqueVpnCertificateIdentifier() throws CertificateGenerationException {
        for (int attempt = 0; attempt < MAX_ID_GENERATION_ATTEMPTS; attempt++) {
            StringBuilder sb = new StringBuilder(ID_LENGTH);
            sb.append(UPPER.charAt(random.nextInt(UPPER.length())));
            for (int i = 1; i < ID_LENGTH; i++) {
                sb.append(ALL_CHARS.charAt(random.nextInt(ALL_CHARS.length())));
            }
            String potentialId = sb.toString();
            if (!userRepository.existsByVpnCertificateIdentifier(potentialId)) {
                return potentialId;
            }
            log.warn("Generated VPN certificate identifier {} already exists. Attempt {}/{}", potentialId, attempt + 1,
                    MAX_ID_GENERATION_ATTEMPTS);
        }
        throw new CertificateGenerationException("Failed to generate a unique VPN certificate identifier after "
                + MAX_ID_GENERATION_ATTEMPTS + " attempts.");
    }

    @Transactional
    public CertificateResponse generateCertificate(String userUuid, Integer validityDays)
            throws UserNotFoundException, CertificateGenerationException {
        validateUserUuid(userUuid);
        validatePositiveInteger(validityDays, "Validity days");

        User user = userRepository.findById(UUID.fromString(userUuid))
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado com UUID: " + userUuid));

        String vpnCertId = user.getVpnCertificateIdentifier();
        boolean newIdentifierGenerated = false;
        if (vpnCertId == null || vpnCertId.trim().isEmpty()) {
            vpnCertId = generateUniqueVpnCertificateIdentifier();
            user.setVpnCertificateIdentifier(vpnCertId);
            newIdentifierGenerated = true;
        }

        try {
            executeCertificateScript(vpnCertId, "generate");

            LocalDateTime issueDate = LocalDateTime.now();
            LocalDateTime expiryDate = issueDate.plusDays(validityDays != null ? validityDays : 7); // Default to 7 days

            user.setCertificateExpiry(expiryDate);
            user.setVpnEnabled(true);
            userRepository.save(user); // Save user to persist vpnCertId and certificate details

            log.info("Certificado gerado para usuário UUID: {}, Identificador VPN: {}", userUuid, vpnCertId);

            return CertificateResponse.builder()
                    .certificateId(vpnCertId)
                    .userId(String.valueOf(user.getId()))
                    .commonName(user.getUsername())
                    .issueDate(issueDate)
                    .expiryDate(expiryDate)
                    .status("ATIVO")
                    .build();
        } catch (Exception e) {
            // If a new identifier was generated but script execution failed, roll back the
            // identifier to allow retry.
            // This is a simple rollback; more sophisticated state management might be
            // needed in complex scenarios.
            if (newIdentifierGenerated) {
                user.setVpnCertificateIdentifier(null);
                userRepository.save(user);
            }
            log.error("Erro ao gerar certificado para o usuário UUID {}: {}", userUuid, e.getMessage(), e);
            throw new CertificateGenerationException(
                    "Falha ao gerar certificado para o usuário UUID " + userUuid + ": " + e.getMessage(), e);
        }
    }

    @Transactional
    public Resource downloadUserCertificate(String vpnCertId)
            throws CertificateGenerationException, UserNotFoundException {
        validateVpnCertificateIdentifier(vpnCertId);

        User user = userRepository.findByVpnCertificateIdentifier(vpnCertId)
                .orElseThrow(() -> new UserNotFoundException(
                        "Usuário não encontrado para o identificador de certificado VPN: " + vpnCertId));

        Path zipPath = Paths.get(certificatesDirectory, vpnCertId, vpnCertId + ".zip");
        try {
            if (!Files.exists(zipPath)) {
                log.error("Arquivo de certificado .zip não encontrado para o identificador VPN: {}", vpnCertId);
                throw new CertificateGenerationException(
                        "Arquivo de certificado não encontrado para o identificador VPN: " + vpnCertId);
            }
            return new ByteArrayResource(Files.readAllBytes(zipPath));
        } catch (IOException e) {
            log.error("Erro ao ler arquivo de certificado para o identificador VPN {}: {}", vpnCertId, e.getMessage());
            throw new CertificateGenerationException(
                    "Erro ao gerar download do certificado para o identificador VPN: " + vpnCertId, e);
        }
    }

    @Transactional
    public CertificateResponse renewCertificate(String vpnCertId, Integer validityDays)
            throws UserNotFoundException, CertificateGenerationException {
        validateVpnCertificateIdentifier(vpnCertId);
        validatePositiveInteger(validityDays, "Validity days");

        User user = userRepository.findByVpnCertificateIdentifier(vpnCertId)
                .orElseThrow(() -> new UserNotFoundException(
                        "Usuário não encontrado para o identificador de certificado VPN: " + vpnCertId));

        try {
            executeCertificateScript(vpnCertId, "generate");

            LocalDateTime issueDate = LocalDateTime.now();
            LocalDateTime expiryDate = issueDate.plusDays(validityDays != null ? validityDays : 7);

            user.setCertificateExpiry(expiryDate);
            user.setVpnEnabled(true);
            userRepository.save(user);

            log.info("Certificado renovado para o identificador VPN: {}, Novo vencimento: {}", vpnCertId, expiryDate);

            return CertificateResponse.builder()
                    .certificateId(vpnCertId)
                    .userId(String.valueOf(user.getId()))
                    .commonName(user.getUsername())
                    .issueDate(issueDate)
                    .expiryDate(expiryDate)
                    .status("ATIVO (RENOVADO)")
                    .build();
        } catch (Exception e) {
            log.error("Erro ao renovar certificado para o identificador VPN {}: {}", vpnCertId, e.getMessage(), e);
            throw new CertificateGenerationException(
                    "Falha ao renovar certificado para o identificador VPN " + vpnCertId + ": " + e.getMessage(), e);
        }
    }

    @Transactional
    public void revokeCertificate(String vpnCertId) throws UserNotFoundException, CertificateGenerationException {
        validateVpnCertificateIdentifier(vpnCertId);

        User user = userRepository.findByVpnCertificateIdentifier(vpnCertId)
                .orElseThrow(() -> new UserNotFoundException(
                        "Usuário não encontrado para o identificador de certificado VPN: " + vpnCertId));

        try {
            executeCertificateScript(vpnCertId, "revoke");

            user.setVpnEnabled(false);
            user.setCertificateExpiry(LocalDateTime.now().minusSeconds(1));

            userRepository.save(user);

            log.info("Certificado {} revogado com sucesso para o usuário {}.", vpnCertId, user.getUsername());
        } catch (IOException | InterruptedException e) {
            log.error("Erro ao executar script de revogação para {}: {}", vpnCertId, e.getMessage(), e);
            Thread.currentThread().interrupt();
            throw new CertificateGenerationException(
                    "Falha ao revogar certificado (script execution) " + vpnCertId + ": " + e.getMessage(), e);
        } catch (CertificateGenerationException e) {
            log.error("Erro ao revogar certificado {}: {}", vpnCertId, e.getMessage(), e);
            throw e;
        }
    }

    public CertificateResponse.DownloadFile downloadConfigFile(String vpnCertId)
            throws UserNotFoundException, CertificateGenerationException {
        validateVpnCertificateIdentifier(vpnCertId);

        User user = userRepository.findByVpnCertificateIdentifier(vpnCertId)
                .orElseThrow(() -> new UserNotFoundException(
                        "Usuário não encontrado para o identificador de certificado VPN: " + vpnCertId));

        Path zipPath = Paths.get(certificatesDirectory, vpnCertId, vpnCertId + ".zip");
        try {
            if (!Files.exists(zipPath)) {
                log.error("Arquivo de configuração .zip não encontrado para o identificador VPN: {}", vpnCertId);
                throw new CertificateGenerationException(
                        "Arquivo de configuração (.zip) não encontrado para o identificador VPN: " + vpnCertId);
            }
            byte[] fileContent = Files.readAllBytes(zipPath);
            return CertificateResponse.DownloadFile.builder()
                    .filename(vpnCertId + ".zip")
                    .resource(new ByteArrayResource(fileContent))
                    .contentType("application/octet-stream")
                    .size(fileContent.length)
                    .build();
        } catch (IOException e) {
            log.error("Erro ao ler arquivo de configuração .zip para o identificador VPN {}: {}", vpnCertId,
                    e.getMessage());
            throw new CertificateGenerationException(
                    "Erro ao preparar download do arquivo de configuração para o identificador VPN: " + vpnCertId, e);
        }
    }

    public List<CertificateResponse> listUserCertificates(String userUuid) throws UserNotFoundException {
        validateUserUuid(userUuid);

        User user = userRepository.findById(UUID.fromString(userUuid)) // Convert String to UUID
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado com UUID: " + userUuid));

        List<CertificateResponse> certificates = new ArrayList<>();
        if (user.getVpnCertificateIdentifier() != null && user.isVpnEnabled()) {
            certificates.add(CertificateResponse.builder()
                    .certificateId(user.getVpnCertificateIdentifier())
                    .userId(String.valueOf(user.getId()))
                    .commonName(user.getUsername())
                    .issueDate(user.getCertificateExpiry() != null ? user.getCertificateExpiry().minusDays(7)
                            : user.getUpdatedAt())
                    .expiryDate(user.getCertificateExpiry())
                    .status(user.getCertificateExpiry() != null
                            && user.getCertificateExpiry().isAfter(LocalDateTime.now()) ? "ATIVO" : "EXPIRADO")
                    .build());
        }
        return certificates;
    }

    public boolean isCertificateOwner(String vpnCertId, String userUuid) {
        validateVpnCertificateIdentifier(vpnCertId);
        validateUserUuid(userUuid);

        if (vpnCertId == null || userUuid == null)
            return false;
        return userRepository.findByVpnCertificateIdentifier(vpnCertId)
                .map(user -> user.getId().equals(UUID.fromString(userUuid)))
                .orElse(false);
    }

    private void executeCertificateScript(String clientNameOrId, String action)
            throws IOException, InterruptedException, CertificateGenerationException {
    }
}
