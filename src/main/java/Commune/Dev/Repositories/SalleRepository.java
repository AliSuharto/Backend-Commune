package Commune.Dev.Repositories;

import Commune.Dev.Models.Salle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SalleRepository extends JpaRepository<Salle, Integer>, JpaSpecificationExecutor<Salle> {

    // Custom finder methods
    List<Salle> findByNomContainingIgnoreCase(String nom);

    // Salles directement dans un marché (sans zone)
    @Query("SELECT s FROM Salle s WHERE s.marchee.id = :marcheeId AND s.zone IS NULL")
    List<Salle> findByMarcheeIdDirectly(@Param("marcheeId") Integer marcheeId);

    // Salles dans une zone spécifique
    @Query("SELECT s FROM Salle s WHERE s.zone.id = :zoneId")
    List<Salle> findByZoneId(@Param("zoneId") Integer zoneId);

    // Toutes les salles d'un marché (directes + via zones)
    @Query("SELECT s FROM Salle s WHERE s.marchee.id = :marcheeId OR s.zone.marchee.id = :marcheeId")
    List<Salle> findAllByMarcheeId(@Param("marcheeId") Integer marcheeId);

    // Salles dans une zone spécifique d'un marché
    @Query("SELECT s FROM Salle s WHERE s.zone.id = :zoneId AND s.zone.marchee.id = :marcheeId")
    List<Salle> findByMarcheeIdAndZoneId(@Param("marcheeId") Integer marcheeId, @Param("zoneId") Integer zoneId);

    @Query("SELECT s FROM Salle s LEFT JOIN FETCH s.places WHERE s.id = :id")
    Optional<Salle> findByIdWithPlaces(@Param("id") Integer id);

    @Query("SELECT COUNT(p) FROM Place p WHERE p.salle.id = :salleId")
    Long countPlacesBySalleId(@Param("salleId") Integer salleId);

    boolean existsByNomIgnoreCase(String nom);
}