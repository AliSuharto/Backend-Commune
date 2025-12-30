package Commune.Dev.Repositories;

import Commune.Dev.Models.Ordonnateur;
import Commune.Dev.Models.Roletype;
import Commune.Dev.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

    public interface UserRepository extends JpaRepository<User, Long> {
        Optional<User> findByEmail(String email);
        Optional<User> findByPseudo(String pseudo);
        boolean existsByEmail(String email);
        boolean existsByPseudo(String pseudo);
        List<User> findByRole(Roletype role);
        List<User> findByIsActive(Boolean isActive);
        List<User> findByCreatedBy(User createdBy);

        @Query("SELECT u FROM User u WHERE u.commune.id = :communeId")
        List<User> findByCommune(@Param("communeId") Long communeId);

        @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
        long countByRole(@Param("role") Roletype role);

        // Pour vérifier s'il existe déjà un ORDONNATEUR
        boolean existsByRole(Roletype role);

        List<User> findByRoleIn(List<Roletype> roles);

}
