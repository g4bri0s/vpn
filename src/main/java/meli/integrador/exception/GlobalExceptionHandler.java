package meli.integrador.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manipulador global de exceções da aplicação
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Manipula erros de validação de campos
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Validation Error");
        
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());

        body.put("errors", errors);
        body.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Manipula exceções de usuário não encontrado
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Object> handleUserNotFoundException(
            UserNotFoundException ex, WebRequest request) {
        return buildErrorResponse(
                ex,
                HttpStatus.NOT_FOUND,
                "User not found",
                request.getDescription(false).replace("uri=", ""));
    }
    
    /**
     * Manipula exceções de geração de certificado
     */
    @ExceptionHandler(CertificateGenerationException.class)
    public ResponseEntity<Object> handleCertificateGenerationException(
            CertificateGenerationException ex, WebRequest request) {
        return buildErrorResponse(
                ex,
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Certificate generation error",
                request.getDescription(false).replace("uri=", ""));
    }
    
    /**
     * Manipula exceções de usuário já existente
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Object> handleUserAlreadyExistsException(
            UserAlreadyExistsException ex, WebRequest request) {
        return buildErrorResponse(
                ex,
                HttpStatus.CONFLICT,
                "User already exists",
                request.getDescription(false).replace("uri=", ""));
    }
    
    /**
     * Manipula exceções de credenciais inválidas
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> handleBadCredentialsException(
            BadCredentialsException ex, WebRequest request) {
        return buildErrorResponse(
                ex,
                HttpStatus.UNAUTHORIZED,
                "Invalid username or password",
                request.getDescription(false).replace("uri=", ""));
    }
    
    /**
     * Manipula exceções de acesso negado
     */
    /**
     * Manipula exceções de violação de restrições de validação
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolationException(
            ConstraintViolationException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Validation Error");
        
        List<String> errors = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());
                
        body.put("errors", errors);
        body.put("path", request.getDescription(false).replace("uri=", ""));
        
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
    
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, @Nullable Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        
        if (ex instanceof MissingServletRequestParameterException) {
            MissingServletRequestParameterException msrpe = (MissingServletRequestParameterException) ex;
            String error = msrpe.getParameterName() + " parameter is missing";
            return buildErrorResponse(
                    ex,
                    HttpStatus.BAD_REQUEST,
                    error,
                    request.getDescription(false).replace("uri=", ""));
        }
        
        return super.handleExceptionInternal(ex, body, headers, statusCode, request);
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {
        return buildErrorResponse(
                ex,
                HttpStatus.FORBIDDEN,
                "Access denied",
                request.getDescription(false).replace("uri=", ""));
    }
    
    /**
     * Manipula exceções gerais não tratadas
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllUncaughtException(
            Exception ex, WebRequest request) {
        return buildErrorResponse(
                ex,
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                request.getDescription(false).replace("uri=", ""));
    }
    
    /**
     * Constrói uma resposta de erro padronizada
     */
    private ResponseEntity<Object> buildErrorResponse(
            Exception ex,
            HttpStatus status,
            String message,
            String path) {
        
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", path);
        
        // Inclui a mensagem de erro detalhada apenas em ambiente de desenvolvimento
        if (status == HttpStatus.INTERNAL_SERVER_ERROR) {
            body.put("details", ex.getMessage());
        }
        
        return new ResponseEntity<>(body, status);
    }
}
