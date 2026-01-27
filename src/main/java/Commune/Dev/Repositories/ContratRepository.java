package Commune.Dev.Repositories;

import Commune.Dev.Models.Contrat;
import Commune.Dev.Models.DroitAnnuel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContratRepository extends JpaRepository<Contrat, Integer> {

    // Trouver les contrats par marchand
    List<Contrat> findByIdMarchand(Integer idMarchand);

    Optional<Contrat> findTopByIdMarchandOrderByDateOfStartDesc(Integer idMarchand);

    // Trouver les contrats par place
    List<Contrat> findByIdPlace(Integer idPlace);

    List<Contrat> findByDroitAnnuelId(Integer droitAnnuelId);

    // Trouver les contrats par catégorie
    List<Contrat> findByCategorieId(Integer categorieId);

    // Vérifier si un marchand a déjà un contrat actif
    @Query("SELECT c FROM Contrat c WHERE c.idMarchand = :idMarchand AND c.isActif= true")
    List<Contrat> findContratsByMarchand(@Param("idMarchand") Integer idMarchand);

    // Vérifier si une place a déjà un contrat actif
    @Query("SELECT c FROM Contrat c WHERE c.idPlace = :idPlace AND c.isActif = true")
    Optional<Contrat> findContratByPlace(@Param("idPlace") Integer idPlace);

    // Récupérer tous les contrats avec leurs relations
    @Query("SELECT c FROM Contrat c " +
            "LEFT JOIN FETCH c.marchand " +
            "LEFT JOIN FETCH c.place " +
            "LEFT JOIN FETCH c.categorie")
    List<Contrat> findAllWithRelations();



    @Query("SELECT c FROM Contrat c WHERE c.isActif = true")
    List<Contrat> findAllActifs();

    @Query("""
        SELECT DISTINCT c FROM Contrat c
        JOIN FETCH c.marchand m
        JOIN FETCH c.place p
        LEFT JOIN FETCH m.paiements pa
        LEFT JOIN FETCH pa.agent ag
        WHERE c.isActif = true
    """)
    List<Contrat> findContratsActifsAvecTout();

    // Ou plus précis :


}

