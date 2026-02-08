package Commune.Dev.Repositories;

import Commune.Dev.Models.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Integer> {

    Optional<Place> findByNomAndHallId(String nom, Integer hallId);
    Optional<Place> findByNomAndZoneId(String nom, Integer zoneId);
    Optional<Place> findByNomAndMarcheeId(String nom, Integer marcheeId);
    @Query("SELECT COUNT(p) FROM Place p")
    Integer countAllPlaces();



    List<Place> findByMarcheeId(Long marcheeId);
    List<Place> findByHallId(Long hallId);
    List<Place> findByZoneId(Long zoneId);

    // Méthodes pour vérifier l'existence d'une place avec un parent spécifique
    boolean existsByMarcheeIdAndNom(Long marcheeId, String nom);
    boolean existsByHallIdAndNom(Long hallId, String nom);
    boolean existsByZoneIdAndNom(Long zoneId, String nom);





    List<Place> findByIsOccupedTrue();

    List<Place> findByIsOccupedFalse();
    // Recherche par nom (insensible à la casse)
    List<Place> findByNomContainingIgnoreCase(String nom);


    // Recherche par adresse (insensible à la casse)
    List<Place> findByAdresseContainingIgnoreCase(String adresse);

    // Recherche par statut d'occupation
    List<Place> findByIsOccuped(Boolean isOccuped);

    // Compter par statut d'occupation
    long countByIsOccuped(Boolean isOccuped);

    // Recherche par nom exact
    List<Place> findByNom(String nom);

    // Recherche par adresse exacte
    List<Place> findByAdresse(String adresse);

    // Requêtes personnalisées avec @Query

    // Trouver les places occupées entre deux dates
    @Query("SELECT p FROM Place p WHERE p.isOccuped = true AND p.dateDebutOccupation BETWEEN :startDate AND :endDate")
    List<Place> findOccupiedPlacesBetweenDates(@Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);

    // Trouver les places qui seront libres après une certaine date
    @Query("SELECT p FROM Place p WHERE p.isOccuped = true AND p.dateFinOccupation <= :date")
    List<Place> findPlacesToBeFreedAfter(@Param("date") LocalDateTime date);

    // Trouver les places par marchée
    @Query("SELECT p FROM Place p WHERE p.marchee.id = :marcheeId")
    List<Place> findByMarcheeId(@Param("marcheeId") Integer marcheeId);

    // Trouver les places par marchand
    @Query("SELECT p FROM Place p WHERE p.marchands.id = :marchandId")
    List<Place> findByMarchandId(@Param("marchandId") Integer marchandId);

    // Trouver les places par salle
    @Query("SELECT p FROM Place p WHERE p.hall.id = :hallId")
    List<Place> findBySalleId(@Param("hallId") Integer hallId);

    // Trouver les places par catégorie
    @Query("SELECT p FROM Place p WHERE p.categorie.id = :categorieId")
    List<Place> findByCategorieId(@Param("categorieId") Integer categorieId);

    // Compter les places par marchée
    @Query("SELECT COUNT(p) FROM Place p WHERE p.marchee.id = :marcheeId")
    long countByMarcheeId(@Param("marcheeId") Integer marcheeId);

    // Compter les places disponibles par catégorie
    @Query("SELECT COUNT(p) FROM Place p WHERE p.categorie.id = :categorieId AND p.isOccuped = false")
    long countAvailablePlacesByCategorieId(@Param("categorieId") Integer categorieId);

    // Trouver les places avec occupation expirée
    @Query("SELECT p FROM Place p WHERE p.isOccuped = true AND p.dateFinOccupation < :currentDate")
    List<Place> findPlacesWithExpiredOccupation(@Param("currentDate") LocalDateTime currentDate);

    // Recherche combinée nom et statut
    @Query("SELECT p FROM Place p WHERE p.nom LIKE %:nom% AND p.isOccuped = :isOccuped")
    List<Place> findByNomContainingAndIsOccuped(@Param("nom") String nom, @Param("isOccuped") Boolean isOccuped);


    @Query("SELECT p FROM Place p WHERE p.zone.id = :zoneId")
    List<Place> findByZoneId(@Param("zoneId") Integer zoneId);

    // Trouver les places sans zone assignée
    @Query("SELECT p FROM Place p WHERE p.zone IS NULL")
    List<Place> findByZoneIsNull();

    // Trouver les places par zone et statut d'occupation
    @Query("SELECT p FROM Place p WHERE p.zone.id = :zoneId AND p.isOccuped = :isOccuped")
    List<Place> findByZoneIdAndIsOccuped(@Param("zoneId") Integer zoneId, @Param("isOccuped") Boolean isOccuped);

    // Compter les places par zone
    @Query("SELECT COUNT(p) FROM Place p WHERE p.zone.id = :zoneId")
    long countByZoneId(@Param("zoneId") Integer zoneId);

    // Compter les places disponibles par zone
    @Query("SELECT COUNT(p) FROM Place p WHERE p.zone.id = :zoneId AND p.isOccuped = :isOccuped")
    long countByZoneIdAndIsOccuped(@Param("zoneId") Integer zoneId, @Param("isOccuped") Boolean isOccuped);

    // Trouver les places par nom dans une zone spécifique
    @Query("SELECT p FROM Place p WHERE p.zone.id = :zoneId AND p.nom LIKE %:nom%")
    List<Place> findByZoneIdAndNomContaining(@Param("zoneId") Integer zoneId, @Param("nom") String nom);



    @Query("""
    SELECT p FROM Place p
    LEFT JOIN p.hall h
    LEFT JOIN p.zone z
    LEFT JOIN p.marchee m
    WHERE p.nom = :placeName
      AND (:hallName IS NULL OR h.nom = :hallName)
      AND (:zoneName IS NULL OR z.nom = :zoneName)
      AND (:marcheeName IS NULL OR m.nom = :marcheeName)
""")
    Optional<Place> findByHierarchy(
            @Param("placeName") String placeName,
            @Param("hallName") String hallName,
            @Param("zoneName") String zoneName,
            @Param("marcheeName") String marcheeName
    );


    List<Place> findByMarcheeIdIn(List<Long> marcheeIds);
    List<Place> findByZoneIdIn(List<Long> zoneIds);
    List<Place> findByHallIdIn(List<Long> hallIds);
    @Query("SELECT DISTINCT p FROM Place p " +
            "LEFT JOIN p.zone z " +
            "LEFT JOIN p.hall h " +
            "LEFT JOIN h.zone hz " +
            "WHERE p.marchee.id = :marcheeId " +
            "   OR z.marchee.id = :marcheeId " +
            "   OR h.marchee.id = :marcheeId " +
            "   OR hz.marchee.id = :marcheeId")
    List<Place> findAllByMarcheeIdRecursive(@Param("marcheeId") Long marcheeId);

    /**
     * Récupère toutes les places d'une zone :
     * 1. Places directes (place.zone_id = zoneId)
     * 2. Places dans halls de cette zone (hall.zone_id = zoneId)
     */
    @Query("SELECT DISTINCT p FROM Place p " +
            "LEFT JOIN p.hall h " +
            "WHERE p.zone.id = :zoneId " +
            "   OR h.zone.id = :zoneId")
    List<Place> findAllByZoneIdRecursive(@Param("zoneId") Long zoneId);


}