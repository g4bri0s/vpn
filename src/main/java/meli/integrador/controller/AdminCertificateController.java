package meli.integrador.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import meli.integrador.service.CertificateService;
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
}
