package Commune.Dev.Repositories;

import Commune.Dev.Models.Contrat;
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

    // Trouver les contrats par place
    List<Contrat> findByIdPlace(Integer idPlace);

    // Trouver les contrats par catégorie
    List<Contrat> findByCategorieId(Integer categorieId);

    // Vérifier si un marchand a déjà un contrat actif
    @Query("SELECT c FROM Contrat c WHERE c.idMarchand = :idMarchand")
    List<Contrat> findContratsByMarchand(@Param("idMarchand") Integer idMarchand);

    // Vérifier si une place a déjà un contrat actif
    @Query("SELECT c FROM Contrat c WHERE c.idPlace = :idPlace")
    Optional<Contrat> findContratByPlace(@Param("idPlace") Integer idPlace);

    // Récupérer tous les contrats avec leurs relations
    @Query("SELECT c FROM Contrat c " +
            "LEFT JOIN FETCH c.marchand " +
            "LEFT JOIN FETCH c.place " +
            "LEFT JOIN FETCH c.categorie")
    List<Contrat> findAllWithRelations();
}

