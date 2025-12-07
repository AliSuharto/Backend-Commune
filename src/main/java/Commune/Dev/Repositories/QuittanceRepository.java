package Commune.Dev.Repositories;

import Commune.Dev.Models.Quittance;
import Commune.Dev.Models.QuittancePlage;

import Commune.Dev.Models.StatusQuittance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuittanceRepository extends JpaRepository<Quittance, Long> {
    boolean existsByNomIn(List<String> noms);

    @Query("SELECT q.nom FROM Quittance q WHERE q.nom IN :noms")
    List<String> findExistingNom(@Param("noms") List<String> noms);

    Page<Quittance> findByPercepteurIdOrderByCreatedAtDesc(Long percepteurId, Pageable pageable);

    long countByPercepteurIdAndEtat(Long percepteurId, StatusQuittance etat);
}

