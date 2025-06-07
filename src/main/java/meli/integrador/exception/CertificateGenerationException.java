package meli.integrador.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exceção lançada quando ocorre um erro durante a geração de um certificado VPN
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class CertificateGenerationException extends RuntimeException {
    
    /**
     * Constrói uma nova exceção com a mensagem de erro especificada
     *
     * @param message A mensagem de erro detalhada
     */
    public CertificateGenerationException(String message) {
        super(message);
    }
    
    /**
     * Constrói uma nova exceção com a mensagem de erro e a causa
     *
     * @param message A mensagem de erro detalhada
     * @param cause A causa da exceção
     */
    public CertificateGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
