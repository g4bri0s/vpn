package meli.integrador.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Invalid CPF")
public class InvalidCPFException extends RuntimeException{
    public InvalidCPFException(String cpf) {
        super("Invalid CPF: " + cpf);
    }
}
