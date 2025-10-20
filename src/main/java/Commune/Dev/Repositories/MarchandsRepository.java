package Commune.Dev.Repositories;

import Commune.Dev.Models.Marchands;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MarchandsRepository extends JpaRepository<Marchands, Integer> {

    // Recherche par nom (insensible à la casse)
    List<Marchands> findByNomContainingIgnoreCase(String nom);

    // Recherche par prénom (insensible à la casse)
    List<Marchands> findByPrenomContainingIgnoreCase(String prenom);

    // Recherche par nom ou prénom
    @Query("SELECT m FROM Marchands m WHERE " +
            "LOWER(m.nom) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(m.prenom) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Marchands> findByNomOrPrenomContaining(@Param("searchTerm") String searchTerm);

    // Marchands qui ont des places associées
    @Query("SELECT DISTINCT m FROM Marchands m WHERE m.places IS NOT EMPTY")
    List<Marchands> findMarchandsWithPlaces();

    // Marchands SANS des places associées
    @Query("SELECT m FROM Marchands m WHERE NOT EXISTS (SELECT p FROM Place p WHERE p.marchands = m AND p.isOccuped = true)")
    List<Marchands> findMarchandsSansPlace();

    // Recherche par numéro CIN
    Marchands findByNumCIN(String numCIN);

    // Vérifier si le CIN existe déjà
    boolean existsByNumCIN(String numCIN);
}