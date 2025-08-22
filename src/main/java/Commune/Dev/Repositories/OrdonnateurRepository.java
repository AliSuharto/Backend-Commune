package Commune.Dev.Repositories;

import Commune.Dev.Models.Ordonnateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository

public interface OrdonnateurRepository extends JpaRepository<Ordonnateur, Long> {
    Optional<Ordonnateur> findByEmail(String email);
    boolean existsByEmail(String email);
}

