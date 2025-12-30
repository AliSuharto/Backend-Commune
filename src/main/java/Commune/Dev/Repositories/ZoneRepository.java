package Commune.Dev.Repositories;

import Commune.Dev.Models.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ZoneRepository extends JpaRepository<Zone, Integer> {


    Optional<Zone> findByNom(String nom);

    // Recherche par marché (obligatoire)
    List<Zone> findByMarcheeId(Integer marcheeId);

    // Recherche par nom (insensible à la casse)
    List<Zone> findByNomContainingIgnoreCase(String nom);

    // Recherche par nom dans un marché spécifique
    List<Zone> findByMarcheeIdAndNomContainingIgnoreCase(Integer marcheeId, String nom);

    // Recherche par nom exact
//    List<Zone> findByNom(String nom);

    // Compter les zones par marché
    long countByMarcheeId(Integer marcheeId);

    // Supprimer toutes les zones d'un marché
    @Modifying
    @Transactional
    void deleteByMarcheeId(Integer marcheeId);

    // Requêtes personnalisées avec @Query

    // Trouver les zones avec le nombre de places
    @Query("SELECT z FROM Zone z LEFT JOIN z.places p WHERE z.marchee.id = :marcheeId GROUP BY z.id")
    List<Zone> findByMarcheeIdWithPlaceCount(@Param("marcheeId") Integer marcheeId);

    // Trouver les zones avec des places disponibles
    @Query("SELECT DISTINCT z FROM Zone z JOIN z.places p WHERE z.marchee.id = :marcheeId AND p.isOccuped = false")
    List<Zone> findZonesWithAvailablePlaces(@Param("marcheeId") Integer marcheeId);

    // Trouver les zones sans places
    @Query("SELECT z FROM Zone z WHERE z.marchee.id = :marcheeId AND z.id NOT IN (SELECT DISTINCT p.zone.id FROM Place p WHERE p.zone IS NOT NULL)")
    List<Zone> findZonesWithoutPlaces(@Param("marcheeId") Integer marcheeId);

    // Trouver les zones avec le plus de places
    @Query("SELECT z FROM Zone z JOIN z.places p WHERE z.marchee.id = :marcheeId GROUP BY z.id ORDER BY COUNT(p) DESC")
    List<Zone> findZonesOrderByPlaceCountDesc(@Param("marcheeId") Integer marcheeId);

    // Compter les places totales par zone dans un marché
    @Query("SELECT z.id, z.nom, COUNT(p.id) FROM Zone z LEFT JOIN z.places p WHERE z.marchee.id = :marcheeId GROUP BY z.id, z.nom")
    List<Object[]> getZonePlaceStatistics(@Param("marcheeId") Integer marcheeId);

    // Trouver les zones par description contenant un texte
    List<Zone> findByDescriptionContainingIgnoreCase(String description);

    // Trouver les zones par marché et description
    List<Zone> findByMarcheeIdAndDescriptionContainingIgnoreCase(Integer marcheeId, String description);

    // Vérifier si une zone existe dans un marché
    boolean existsByMarcheeIdAndNom(Integer marcheeId, String nom);

    // Trouver les zones avec des salles
    @Query("SELECT DISTINCT z FROM Zone z WHERE z.marchee.id = :marcheeId AND SIZE(z.halls) > 0")
    List<Zone> findZonesWithSalles(@Param("marcheeId") Integer marcheeId);

    // Trouver les zones sans salles
    @Query("SELECT z FROM Zone z WHERE z.marchee.id = :marcheeId AND (z.halls IS EMPTY OR z.halls IS NULL)")
    List<Zone> findZonesWithoutSalles(@Param("marcheeId") Integer marcheeId);

    // Statistiques complètes des zones d'un marché
    @Query("SELECT z.id as zoneId, z.nom as zoneName, " +
            "COUNT(DISTINCT p.id) as totalPlaces, " +
            "COUNT(DISTINCT CASE WHEN p.isOccuped = false THEN p.id END) as availablePlaces, " +
            "COUNT(DISTINCT CASE WHEN p.isOccuped = true THEN p.id END) as occupiedPlaces, " +
            "COUNT(DISTINCT h.id) as totalSalles " +
            "FROM Zone z " +
            "LEFT JOIN z.places p " +
            "LEFT JOIN z.halls h " +
            "WHERE z.marchee.id = :marcheeId " +
            "GROUP BY z.id, z.nom")
    List<Object[]> getCompleteZoneStatistics(@Param("marcheeId") Integer marcheeId);


}
