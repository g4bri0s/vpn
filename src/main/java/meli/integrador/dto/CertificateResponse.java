package meli.integrador.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.io.Resource;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO para respostas relacionadas a certificados VPN
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateResponse {
    
    private String certificateId;
    private String userId;
    private String commonName;
    private LocalDateTime issueDate;
    private LocalDateTime expiryDate;
    private boolean revoked;
    private String status;
    private String serialNumber;
    private String fingerprint;
    private Map<String, Object> metadata; // Metadados adicionais
    
    /**
     * DTO para respostas de validação de certificado
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidityResponse {
        private boolean valid;
        private String status;
        private String message;
        private LocalDateTime validUntil;
        private LocalDateTime checkedAt;
        private String certificateId;
        private String commonName;
        private Map<String, Object> details;
    }
    
    /**
     * DTO para respostas de download de arquivo
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DownloadFile {
        private String filename;
        private Resource resource;
        private String contentType;
        private long size;
        private Map<String, String> metadata;
    }
    
    /**
     * DTO para respostas de erro de certificado
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorResponse {
        private String error;
        private String message;
        private LocalDateTime timestamp;
        private String errorCode;
        private Map<String, Object> details;
    }
    
    /**
     * Estatísticas de certificados
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CertificateStats {
        private long totalCertificates;
        private long activeCertificates;
        private long expiredCertificates;
        private long revokedCertificates;
        private long expiringSoonCount;
        private Map<String, Long> certificatesByStatus;
        private Map<String, Long> certificatesByUser;
        private Map<String, Long> revocationReasons;
        private LocalDateTime generatedAt;
        
        // Métodos de conveniência
        public double getActivePercentage() {
            return totalCertificates > 0 ? (activeCertificates * 100.0) / totalCertificates : 0;
        }
        
        public double getExpiredPercentage() {
            return totalCertificates > 0 ? (expiredCertificates * 100.0) / totalCertificates : 0;
        }
        
        public double getRevokedPercentage() {
            return totalCertificates > 0 ? (revokedCertificates * 100.0) / totalCertificates : 0;
        }
    }
    
    /**
     * Resumo de certificado para listagens
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CertificateSummary {
        private String certificateId;
        private String commonName;
        private String userId;
        private LocalDateTime issueDate;
        private LocalDateTime expiryDate;
        private String status;
        private String serialNumber;
        private Map<String, String> links; // Links HATEOAS
    }
    
    /**
     * Dados para geração de relatórios
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportData {
        private String reportId;
        private String reportType;
        private LocalDateTime generatedAt;
        private LocalDateTime periodStart;
        private LocalDateTime periodEnd;
        private Map<String, Object> data;
        private String format; // PDF, CSV, EXCEL, etc.
        private String downloadUrl;
    }
}
