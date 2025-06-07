package meli.integrador.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exceção lançada quando se tenta criar um usuário com um identificador que já existe.
 * Pode ser um nome de usuário, email, etc.
 */
@ResponseStatus(code = HttpStatus.CONFLICT)
public class UserAlreadyExistException extends RuntimeException {
    
    /**
     * Constrói uma nova exceção com o campo e valor que já existe.
     *
     * @param field O campo que já existe (ex: "username", "email")
     * @param value O valor que já está em uso
     */
    public UserAlreadyExistException(String field, String value) {
        super(String.format("Já existe um usuário com %s: %s", field, value));
    }
}
