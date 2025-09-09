package Commune.Dev.Repositories;

import Commune.Dev.Models.EtatRecu;
import Commune.Dev.Models.Recu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RecuRepository extends JpaRepository<Recu, Long> {

    boolean existsByNumeroIn(List<String> numeros);

    @Query("SELECT r.numero FROM Recu r WHERE r.numero IN :numeros")
    List<String> findExistingNumeros(@Param("numeros") List<String> numeros);

    Page<Recu> findByPercepteurIdOrderByCreatedAtDesc(Long percepteurId, Pageable pageable);

    long countByPercepteurIdAndEtat(Long percepteurId, EtatRecu etat);
}
