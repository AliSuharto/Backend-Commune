package Commune.Dev.Repositories;

import Commune.Dev.Dtos.QuittanceDTO;
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
import java.util.Optional;

@Repository
public interface QuittanceRepository extends JpaRepository<Quittance, Long> {
    boolean existsByNomIn(List<String> noms);

    @Query("SELECT q.nom FROM Quittance q WHERE q.nom IN :noms")
    List<String> findExistingNom(@Param("noms") List<String> noms);

    Page<Quittance> findByPercepteurIdOrderByCreatedAtDesc(Long percepteurId, Pageable pageable);

    long countByPercepteurIdAndEtat(Long percepteurId, StatusQuittance etat);


    @Query("SELECT new Commune.Dev.Dtos.QuittanceDTO(" +
            "q.nom, " +
            "q.etat, " +
            "q.dateUtilisation, " +
            "p.nomMarchands, " +
            "p.montant) " +
            "FROM Quittance q " +
            "LEFT JOIN q.paiement p " +
            "WHERE q.percepteurId = :percepteurId")
    List<QuittanceDTO> findQuittanceDTOByPercepteurId(@Param("percepteurId") Long percepteurId);


    Optional<Quittance> findByNom(String nom);   //0099887A OU 0099887a
}

