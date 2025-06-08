package meli.integrador.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import meli.integrador.dto.CertificateRequest;
import meli.integrador.dto.CertificateResponse;
import meli.integrador.exception.CertificateGenerationException;
import meli.integrador.exception.UserNotFoundException;
import meli.integrador.service.CertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador para gerenciamento de certificados VPN
 */
@RestController
@RequestMapping("/api/v1/certificates")
@Tag(name = "Certificados VPN", description = "API para gerenciamento de certificados VPN")
public class VpnCertificateController {

    private final CertificateService certificateService;

    @Autowired
    public VpnCertificateController(CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    /**
     * Gera um novo certificado VPN para um usuário
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and #request.userId == authentication.principal.id)")
    @Operation(summary = "Gera um novo certificado VPN para um usuário", security = @SecurityRequirement(name = "bearerAuth"))
    public CertificateResponse generateCertificate(@RequestBody CertificateRequest request)
            throws UserNotFoundException, CertificateGenerationException {
        return certificateService.generateCertificate(
                request.getUserId(),
                request.getValidityDays());
    }

    /**
     * Renova um certificado VPN existente
     */
    @PostMapping("/{certificateId}/renew")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and @certificateService.isCertificateOwner(#certificateId, authentication.principal.id))")
    @Operation(summary = "Renova um certificado VPN existente", security = @SecurityRequirement(name = "bearerAuth"))
    public CertificateResponse renewCertificate(
            @PathVariable String certificateId,
            @RequestParam(defaultValue = "365") int validityDays)
            throws UserNotFoundException, CertificateGenerationException {
        return certificateService.renewCertificate(certificateId, validityDays);
    }

    /**
     * Revoga um certificado VPN
     */
    @DeleteMapping("/{certificateId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and @certificateService.isCertificateOwner(#certificateId, authentication.principal.id))")
    @Operation(summary = "Revoga um certificado VPN", security = @SecurityRequirement(name = "bearerAuth"))
    public void revokeCertificate(@PathVariable String certificateId)
            throws UserNotFoundException {
        certificateService.revokeCertificate(certificateId);
    }

    /**
     * Faz o download do arquivo de configuração OpenVPN para um certificado
     */
    @GetMapping("/{certificateId}/download")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and @certificateService.isCertificateOwner(#certificateId, authentication.principal.id))")
    @Operation(summary = "Faz o download do arquivo de configuração OpenVPN", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Resource> downloadConfigFile(@PathVariable String certificateId)
            throws UserNotFoundException {
        CertificateResponse.DownloadFile file = certificateService.downloadConfigFile(certificateId);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file.getResource());
    }

    /**
     * Lista todos os certificados de um usuário
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and #userId == authentication.principal.id)")
    @Operation(summary = "Lista todos os certificados de um usuário", security = @SecurityRequirement(name = "bearerAuth"))
    public Iterable<CertificateResponse> listUserCertificates(@PathVariable String userId)
            throws UserNotFoundException {
        return certificateService.listUserCertificates(userId);
    }
}
