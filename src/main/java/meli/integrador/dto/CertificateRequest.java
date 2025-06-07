package meli.integrador.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO para requisições de geração de certificados VPN
 */
@Data
public class CertificateRequest {
    
    @NotBlank(message = "O ID do usuário é obrigatório")
    private String userId;
    
    @NotNull(message = "A validade em dias é obrigatória")
    @Min(value = 1, message = "A validade deve ser de pelo menos 1 dia")
    private Integer validityDays;
    
    // Configurações adicionais do certificado podem ser adicionadas aqui
    // como tipo de criptografia, tamanho da chave, etc.
}
