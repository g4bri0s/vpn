package meli.integrador.common;

/**
 * Enum que representa os papéis de usuário no sistema de Painel VPN
 */
public enum UserRole {
    USER,       // Usuário comum com acesso básico
    ADMIN,      // Administrador com acesso total
    AUDITOR     // Apenas visualização, sem permissões de escrita
}
