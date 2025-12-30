package Commune.Dev.Repositories;

import Commune.Dev.Models.Paiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaiementRepository extends JpaRepository<Paiement, Integer> {

    boolean existsByMarchandIdAndDatePaiementBetween(Integer marchandId, LocalDateTime start, LocalDateTime end);


    /**
     * Trouver les paiements par marchand
     */
    List<Paiement> findByMarchandId(Integer marchandId);


    Optional<Paiement> findTopByMarchandIdOrderByDatePaiementDesc(Integer idMarchand);

    /**
     * Trouver les paiements par place
     */
    List<Paiement> findByPlaceId(Integer placeId);

    /**
     * Trouver les paiements par session
     */
    List<Paiement> findBySessionId(Integer sessionId);

    /**
     * Trouver les paiements par agent
     */
    List<Paiement> findByAgentId(Integer agentId);

    /**
     * Trouver les paiements entre deux dates
     */
    List<Paiement> findByDatePaiementBetween(LocalDateTime dateDebut, LocalDateTime dateFin);

    /**
     * Trouver les paiements par mode de paiement
     */
    List<Paiement> findByModePaiement(Paiement.ModePaiement modePaiement);

    /**
     * Trouver les paiements par marchand et mois
     */
    List<Paiement> findByMarchandIdAndMoisdePaiement(Integer marchandId, String moisdePaiement);

    /**
     * Trouver les paiements par nom de marchand (pour les marchands ambulants)
     */
    List<Paiement> findByNomMarchands(String nomMarchands);

    /**
     * Trouver les paiements par session et agent
     */
    List<Paiement> findBySessionIdAndAgentId(Integer sessionId, Integer agentId);

    /**
     * Requête personnalisée : Total des paiements par marchand
     */
    @Query("SELECT SUM(p.montant) FROM Paiement p WHERE p.marchand.id = :marchandId")
    Double getTotalPaiementsByMarchand(@Param("marchandId") Integer marchandId);

    /**
     * Requête personnalisée : Total des paiements par session
     */
    @Query("SELECT SUM(p.montant) FROM Paiement p WHERE p.session.id = :sessionId")
    Double getTotalPaiementsBySession(@Param("sessionId") Integer sessionId);

    /**
     * Requête personnalisée : Total des paiements par agent
     */
    @Query("SELECT SUM(p.montant) FROM Paiement p WHERE p.agent.id = :agentId")
    Double getTotalPaiementsByAgent(@Param("agentId") Integer agentId);

    /**
     * Requête personnalisée : Nombre de paiements par marchand
     */
    @Query("SELECT COUNT(p) FROM Paiement p WHERE p.marchand.id = :marchandId")
    Long countPaiementsByMarchand(@Param("marchandId") Integer marchandId);

    /**
     * Requête personnalisée : Paiements d'une période pour un marchand
     */
    @Query("SELECT p FROM Paiement p WHERE p.marchand.id = :marchandId " +
            "AND p.datePaiement BETWEEN :dateDebut AND :dateFin")
    List<Paiement> findByMarchandIdAndPeriode(
            @Param("marchandId") Integer marchandId,
            @Param("dateDebut") LocalDateTime dateDebut,
            @Param("dateFin") LocalDateTime dateFin);

    /**
     * Requête personnalisée : Derniers paiements d'un marchand
     */
    @Query("SELECT p FROM Paiement p WHERE p.marchand.id = :marchandId " +
            "ORDER BY p.datePaiement DESC")
    List<Paiement> findLatestPaiementsByMarchand(@Param("marchandId") Integer marchandId);

    /**
     * Requête personnalisée : Paiements par place et période
     */
    @Query("SELECT p FROM Paiement p WHERE p.place.id = :placeId " +
            "AND p.datePaiement BETWEEN :dateDebut AND :dateFin")
    List<Paiement> findByPlaceIdAndPeriode(
            @Param("placeId") Integer placeId,
            @Param("dateDebut") LocalDateTime dateDebut,
            @Param("dateFin") LocalDateTime dateFin);


    Optional<Paiement> findTopByMarchandIdAndTypePaiementOrderByDatePaiementDesc(
            Integer marchandId,
            Paiement.Typepaiement typePaiement
    );

    Collection<? extends Paiement> findByPlaceIdIn(ArrayList<Integer> integers);

    Collection<? extends Paiement> findByMarchandIdIn(ArrayList<Integer> integers);
}
