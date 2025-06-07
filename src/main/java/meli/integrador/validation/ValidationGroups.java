package meli.integrador.validation;

/**
 * Grupos de validação para validação em cascata.
 * Permite definir diferentes cenários de validação para o mesmo bean.
 */
public class ValidationGroups {
    
    /**
     * Grupo de validação para criação de usuário.
     * Deve ser usado quando um novo usuário está sendo criado.
     */
    public interface CreateUser {}
    
    /**
     * Grupo de validação para atualização de usuário.
     * Deve ser usado quando um usuário existente está sendo atualizado.
     */
    public interface UpdateUser {}
    
    /**
     * Grupo de validação para operações de administrador.
     * Inclui validações adicionais para operações administrativas.
     */
    public interface AdminOperation {}
    
    private ValidationGroups() {
        // Construtor privado para evitar instanciação
    }
}
