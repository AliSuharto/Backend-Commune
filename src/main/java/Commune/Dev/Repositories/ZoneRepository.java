package Commune.Dev.Repositories;

import Commune.Dev.Models.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ZoneRepository extends JpaRepository<Zone, Integer> {

    // Recherche par marché (obligatoire)
    List<Zone> findByIdMarchee(Integer idMarchee);

    // Recherche par nom (insensible à la casse)
    List<Zone> findByNomContainingIgnoreCase(String nom);

    // Recherche par nom dans un marché spécifique
    List<Zone> findByIdMarcheeAndNomContainingIgnoreCase(Integer idMarchee, String nom);

    // Recherche par nom exact
    List<Zone> findByNom(String nom);

    // Compter les zones par marché
    long countByIdMarchee(Integer idMarchee);

    // Supprimer toutes les zones d'un marché
    @Modifying
    @Transactional
    void deleteByIdMarchee(Integer idMarchee);

    // Requêtes personnalisées avec @Query

    // Trouver les zones avec le nombre de places
    @Query("SELECT z FROM Zone z LEFT JOIN z.places p WHERE z.idMarchee = :idMarchee GROUP BY z.id")
    List<Zone> findByIdMarcheeWithPlaceCount(@Param("idMarchee") Integer idMarchee);

    // Trouver les zones avec des places disponibles
    @Query("SELECT DISTINCT z FROM Zone z JOIN z.places p WHERE z.idMarchee = :idMarchee AND p.isOccuped = false")
    List<Zone> findZonesWithAvailablePlaces(@Param("idMarchee") Integer idMarchee);

    // Trouver les zones sans places
    @Query("SELECT z FROM Zone z WHERE z.idMarchee = :idMarchee AND z.id NOT IN (SELECT DISTINCT p.zone.id FROM Place p WHERE p.zone IS NOT NULL)")
    List<Zone> findZonesWithoutPlaces(@Param("idMarchee") Integer idMarchee);

    // Trouver les zones avec le plus de places
    @Query("SELECT z FROM Zone z JOIN z.places p WHERE z.idMarchee = :idMarchee GROUP BY z.id ORDER BY COUNT(p) DESC")
    List<Zone> findZonesOrderByPlaceCountDesc(@Param("idMarchee") Integer idMarchee);

    // Compter les places totales par zone dans un marché
    @Query("SELECT z.id, z.nom, COUNT(p.id) FROM Zone z LEFT JOIN z.places p WHERE z.idMarchee = :idMarchee GROUP BY z.id, z.nom")
    List<Object[]> getZonePlaceStatistics(@Param("idMarchee") Integer idMarchee);

    // Trouver les zones par description contenant un texte
    List<Zone> findByDescriptionContainingIgnoreCase(String description);

    // Trouver les zones par marché et description
    List<Zone> findByIdMarcheeAndDescriptionContainingIgnoreCase(Integer idMarchee, String description);

    // Vérifier si une zone existe dans un marché
    boolean existsByIdMarcheeAndNom(Integer idMarchee, String nom);

    // Trouver les zones avec des salles
    @Query("SELECT DISTINCT z FROM Zone z WHERE z.idMarchee = :idMarchee AND SIZE(z.salles) > 0")
    List<Zone> findZonesWithSalles(@Param("idMarchee") Integer idMarchee);

    // Trouver les zones sans salles
    @Query("SELECT z FROM Zone z WHERE z.idMarchee = :idMarchee AND (z.salles IS EMPTY OR z.salles IS NULL)")
    List<Zone> findZonesWithoutSalles(@Param("idMarchee") Integer idMarchee);

    // Statistiques complètes des zones d'un marché
    @Query("SELECT z.id as zoneId, z.nom as zoneName, " +
            "COUNT(DISTINCT p.id) as totalPlaces, " +
            "COUNT(DISTINCT CASE WHEN p.isOccuped = false THEN p.id END) as availablePlaces, " +
            "COUNT(DISTINCT CASE WHEN p.isOccuped = true THEN p.id END) as occupiedPlaces, " +
            "COUNT(DISTINCT s.id) as totalSalles " +
            "FROM Zone z " +
            "LEFT JOIN z.places p " +
            "LEFT JOIN z.salles s " +
            "WHERE z.idMarchee = :idMarchee " +
            "GROUP BY z.id, z.nom")
    List<Object[]> getCompleteZoneStatistics(@Param("idMarchee") Integer idMarchee);
}