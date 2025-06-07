package meli.integrador.exception;

/**
 * Exceção lançada quando um usuário não é encontrado no sistema.
 */
public class UserNotFoundException extends RuntimeException {
    
    /**
     * Constrói uma nova exceção com uma mensagem de erro detalhada.
     *
     * @param field O campo que foi usado na busca (ex: id, email, username)
     * @param value O valor que foi pesquisado
     */
    public UserNotFoundException(String field, String value) {
        super(String.format("Usuário não encontrado com %s: %s", field, value));
    }

    /**
     * Constrói uma nova exceção com uma mensagem de erro personalizada.
     *
     * @param message A mensagem de erro detalhada
     */
    public UserNotFoundException(String message) {
        super(message);
    }
}
