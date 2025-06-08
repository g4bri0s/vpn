package meli.integrador.repository;

import meli.integrador.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositório para operações de banco de dados relacionadas a usuários do
 * Painel VPN
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

       /**
        * Encontra um usuário pelo email
        */
       Optional<User> findByEmail(String email);

       /**
        * Verifica se existe um usuário com o nome de usuário fornecido
        */
       boolean existsByUsername(String username);

       /**
        * Verifica se existe um usuário com o email fornecido
        */
       boolean existsByEmail(String email);

       /**
        * Atualiza o status de ativação do usuário
        */
       @Modifying
       @Transactional
       @Query("UPDATE User u SET u.isActive = :isActive, u.updatedAt = CURRENT_TIMESTAMP WHERE u.id = :userId")
       void updateUserStatus(@Param("userId") UUID userId, @Param("isActive") boolean isActive);

       /**
        * Encontra um usuário pelo seu identificador de certificado VPN.
        *
        * @param vpnCertificateIdentifier O identificador do certificado VPN.
        * @return Um Optional contendo o usuário, se encontrado.
        */
       Optional<User> findByVpnCertificateIdentifier(String vpnCertificateIdentifier);

       /**
        * Verifica se existe um usuário com o identificador de certificado VPN
        * fornecido.
        *
        * @param vpnCertificateIdentifier O identificador do certificado VPN.
        * @return true se um usuário com o identificador existir, false caso contrário.
        */
       boolean existsByVpnCertificateIdentifier(String vpnCertificateIdentifier);
}
