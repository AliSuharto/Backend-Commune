package Commune.Dev.Repositories;

import Commune.Dev.Models.Marchee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MarcheeRepository extends JpaRepository<Marchee, Integer> {

    // Recherche par nom (insensible à la casse)
    List<Marchee> findByNomContainingIgnoreCase(String nom);

    // Recherche par adresse (insensible à la casse)
    List<Marchee> findByAdresseContainingIgnoreCase(String adresse);

    // Recherche par nom exact
    List<Marchee> findByNom(String nom);

    // Recherche par adresse exacte
    List<Marchee> findByAdresse(String adresse);

    // Recherche par capacité minimum
    List<Marchee> findByNbrPlaceGreaterThanEqual(Integer nbrPlace);

    // Recherche par capacité maximum
    List<Marchee> findByNbrPlaceLessThanEqual(Integer nbrPlace);

    // Recherche par range de capacité
    List<Marchee> findByNbrPlaceBetween(Integer minPlaces, Integer maxPlaces);

    // Tri par capacité
    List<Marchee> findAllByOrderByNbrPlaceAsc();

    List<Marchee> findAllByOrderByNbrPlaceDesc();

    // Tri par nom
    List<Marchee> findAllByOrderByNomAsc();

    List<Marchee> findAllByOrderByNomDesc();

    // Requêtes personnalisées avec @Query

    // Trouver les marchés avec des places disponibles
    @Query("SELECT DISTINCT m FROM Marchee m JOIN m.places p WHERE p.isOccuped = false OR p.isOccuped IS NULL")
    List<Marchee> findMarcheesWithAvailablePlaces();

    // Trouver les marchés sans zones
    @Query("SELECT m FROM Marchee m WHERE m.zones IS EMPTY OR m.zones IS NULL")
    List<Marchee> findMarcheesWithoutZones();

    // Trouver les marchés avec zones
    @Query("SELECT DISTINCT m FROM Marchee m WHERE SIZE(m.zones) > 0")
    List<Marchee> findMarcheesWithZones();

    // Trouver les marchés par nombre minimum de zones
    @Query("SELECT m FROM Marchee m WHERE SIZE(m.zones) >= :minZones")
    List<Marchee> findMarcheesWithMinZones(@Param("minZones") int minZones);

    // Compter les places réelles par marché
    @Query("SELECT m.id, m.nom, COUNT(p.id) FROM Marchee m LEFT JOIN m.places p GROUP BY m.id, m.nom")
    List<Object[]> countActualPlacesByMarchee();

    // Trouver les marchés surchargés (plus de places réelles que déclarées)
    @Query("SELECT m FROM Marchee m WHERE SIZE(m.places) > m.nbrPlace")
    List<Marchee> findOverloadedMarchees();

    // Trouver les marchés sous-utilisés (moins de 50% d'occupation)
    @Query("SELECT m FROM Marchee m WHERE " +
            "(SELECT COUNT(p) FROM Place p WHERE p.marchee.id = m.id AND p.isOccuped = true) < " +
            "(SELECT COUNT(p) FROM Place p WHERE p.marchee.id = m.id) * 0.5")
    List<Marchee> findUnderUtilizedMarchees();

    // Statistiques par marché
    @Query("SELECT m.id as marcheeId, m.nom as marcheeName, " +
            "COUNT(DISTINCT p.id) as totalPlaces, " +
            "COUNT(DISTINCT CASE WHEN p.isOccuped = true THEN p.id END) as occupiedPlaces, " +
            "COUNT(DISTINCT CASE WHEN p.isOccuped = false OR p.isOccuped IS NULL THEN p.id END) as availablePlaces, " +
            "COUNT(DISTINCT z.id) as totalZones, " +
            "COUNT(DISTINCT s.id) as totalSalles " +
            "FROM Marchee m " +
            "LEFT JOIN m.places p " +
            "LEFT JOIN m.zones z " +
            "LEFT JOIN m.salles s " +
            "GROUP BY m.id, m.nom")
    List<Object[]> getMarcheeStatistics();

    // Trouver le marché avec le plus de places
    @Query("SELECT m FROM Marchee m WHERE SIZE(m.places) = (SELECT MAX(SIZE(m2.places)) FROM Marchee m2)")
    List<Marchee> findMarcheesWithMostPlaces();

    // Trouver le marché avec le moins de places
    @Query("SELECT m FROM Marchee m WHERE SIZE(m.places) = (SELECT MIN(SIZE(m2.places)) FROM Marchee m2)")
    List<Marchee> findMarcheesWithLeastPlaces();

    // Recherche combinée nom et capacité minimum
    @Query("SELECT m FROM Marchee m WHERE m.nom LIKE %:nom% AND m.nbrPlace >= :minCapacity")
    List<Marchee> findByNomContainingAndMinCapacity(@Param("nom") String nom, @Param("minCapacity") Integer minCapacity);

    // Trouver les marchés par zone spécifique
    @Query("SELECT DISTINCT m FROM Marchee m JOIN m.zones z WHERE z.id = :zoneId")
    List<Marchee> findMarcheesByZoneId(@Param("zoneId") Integer zoneId);

    // Trouver les marchés avec des places dans une catégorie spécifique
    @Query("SELECT DISTINCT m FROM Marchee m JOIN m.places p WHERE p.categorie.id = :categorieId")
    List<Marchee> findMarcheesByCategorieId(@Param("categorieId") Integer categorieId);

    // Calculer le taux d'occupation moyen par marché
    @Query("SELECT m.id, m.nom, " +
            "CASE WHEN COUNT(p.id) > 0 THEN " +
            "CAST(COUNT(CASE WHEN p.isOccuped = true THEN 1 END) AS DOUBLE) / COUNT(p.id) * 100 " +
            "ELSE 0 END as occupationRate " +
            "FROM Marchee m LEFT JOIN m.places p GROUP BY m.id, m.nom")
    List<Object[]> getOccupationRatesByMarchee();

    // Trouver les marchés avec un taux d'occupation supérieur à un seuil
    @Query("SELECT m FROM Marchee m WHERE " +
            "(SELECT COUNT(p) FROM Place p WHERE p.marchee.id = m.id AND p.isOccuped = true) * 100.0 / " +
            "(SELECT COUNT(p) FROM Place p WHERE p.marchee.id = m.id) >= :minRate")
    List<Marchee> findMarcheesWithOccupationRateAbove(@Param("minRate") double minRate);

    // Vérifier si un marché existe par nom
    boolean existsByNom(String nom);

    // Vérifier si un marché existe par nom et adresse
    boolean existsByNomAndAdresse(String nom, String adresse);

    // Compter les marchés par capacité minimum
    long countByNbrPlaceGreaterThanEqual(Integer nbrPlace);

    // Trouver les marchés sans places
    @Query("SELECT m FROM Marchee m WHERE m.places IS EMPTY OR m.places IS NULL")
    List<Marchee> findMarcheesWithoutPlaces();

    // Trouver les marchés sans salles
    @Query("SELECT m FROM Marchee m WHERE m.salles IS EMPTY OR m.salles IS NULL")
    List<Marchee> findMarcheesWithoutSalles();

    // Recherche multicritère
    @Query("SELECT m FROM Marchee m WHERE " +
            "(:nom IS NULL OR m.nom LIKE %:nom%) AND " +
            "(:adresse IS NULL OR m.adresse LIKE %:adresse%) AND " +
            "(:minCapacity IS NULL OR m.nbrPlace >= :minCapacity) AND " +
            "(:maxCapacity IS NULL OR m.nbrPlace <= :maxCapacity)")
    List<Marchee> findMarcheesByMultipleCriteria(
            @Param("nom") String nom,
            @Param("adresse") String adresse,
            @Param("minCapacity") Integer minCapacity,
            @Param("maxCapacity") Integer maxCapacity);

}