package meli.integrador.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import meli.integrador.dto.CertificateResponse;
import meli.integrador.exception.CertificateGenerationException;
import meli.integrador.service.CertificateService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador para operações administrativas relacionadas a certificados VPN
 * Acesso restrito a usuários com perfil de administrador
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/certificates")
@Tag(name = "Admin - Certificados VPN", description = "API administrativa para gerenciamento de certificados VPN")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCertificateController {

    private final CertificateService certificateService;

    /**
     * Gera e faz o download da Lista de Revogação de Certificados (CRL)
     */
    @GetMapping("/crl")
    @Operation(summary = "Gera e faz o download da Lista de Revogação de Certificados (CRL)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Resource> downloadCrl() {
        try {
            CertificateResponse.DownloadFile crlFile = certificateService.generateCrl();

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + crlFile.getFilename() + "\"")
                    .body(crlFile.getResource());

        } catch (CertificateGenerationException e) {
            log.error("Falha ao gerar CRL", e);
            throw new RuntimeException("Falha ao gerar CRL: " + e.getMessage(), e);
        }
    }

    /**
     * Força a revogação de um certificado por um administrador
     */
    @PostMapping("/{certificateId}/revoke")
    @Operation(summary = "Força a revogação de um certificado", description = "Permite que um administrador revogue um certificado manualmente", security = @SecurityRequirement(name = "bearerAuth"))
    public void adminRevokeCertificate(
            @PathVariable String certificateId,
            @RequestParam(required = false) String reason) {

        log.info("Administrador revogando manualmente o certificado: {}, motivo: {}",
                certificateId, reason != null ? reason : "Não especificado");

        certificateService.revokeCertificate(certificateId);
    }

    /**
     * Lista todos os certificados expirados
     */
    @GetMapping("/expired")
    @Operation(summary = "Lista todos os certificados expirados", security = @SecurityRequirement(name = "bearerAuth"))
    public Iterable<CertificateResponse> listExpiredCertificates() {
        return certificateService.listExpiredCertificates();
    }

    /**
     * Lista todos os certificados revogados
     */
    @GetMapping("/revoked")
    @Operation(summary = "Lista todos os certificados revogados", security = @SecurityRequirement(name = "bearerAuth"))
    public Iterable<CertificateResponse> listRevokedCertificates() {
        return certificateService.listRevokedCertificates();
    }

    /**
     * Lista todos os certificados que irão expirar em breve
     */
    @GetMapping("/expiring-soon")
    @Operation(summary = "Lista certificados que irão expirar em breve", security = @SecurityRequirement(name = "bearerAuth"))
    public Iterable<CertificateResponse> listCertificatesExpiringSoon(
            @RequestParam(defaultValue = "30") int days) {
        return certificateService.listCertificatesExpiringSoon(days);
    }

    /**
     * Obtém estatísticas sobre os certificados
     */
    @GetMapping("/stats")
    @Operation(summary = "Obtém estatísticas sobre os certificados", security = @SecurityRequirement(name = "bearerAuth"))
    public CertificateResponse.CertificateStats getCertificateStats() {
        return certificateService.getCertificateStats();
    }
}
