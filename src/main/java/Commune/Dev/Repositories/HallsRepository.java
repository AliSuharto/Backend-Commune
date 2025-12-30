package Commune.Dev.Repositories;

import Commune.Dev.Models.Halls;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface HallsRepository extends JpaRepository<Halls, Integer>, JpaSpecificationExecutor<Halls> {

    Optional<Halls> findByNom(String nom);


    // Custom finder methods
    List<Halls> findByNomContainingIgnoreCase(String nom);
    List<Halls> findByCodeUnique(String codeUnique);

    // Salles directement dans un marché (sans zone)
    @Query("SELECT s FROM Halls s WHERE s.marchee.id = :marcheeId AND s.zone IS NULL")
    List<Halls> findByMarcheeIdDirectly(@Param("marcheeId") Integer marcheeId);

    // Salles dans une zone spécifique
    @Query("SELECT s FROM Halls s WHERE s.zone.id = :zoneId")
    List<Halls> findByZoneId(@Param("zoneId") Integer zoneId);

    // Toutes les salles d'un marché (directes + via zones)
    @Query("SELECT s FROM Halls s WHERE s.marchee.id = :marcheeId OR s.zone.marchee.id = :marcheeId")
    List<Halls> findAllByMarcheeId(@Param("marcheeId") Integer marcheeId);

    // Salles dans une zone spécifique d'un marché
    @Query("SELECT s FROM Halls s WHERE s.zone.id = :zoneId AND s.zone.marchee.id = :marcheeId")
    List<Halls> findByMarcheeIdAndZoneId(@Param("marcheeId") Integer marcheeId, @Param("zoneId") Integer zoneId);

    @Query("SELECT h FROM Halls h LEFT JOIN FETCH h.places WHERE h.id = :id")
    Optional<Halls> findByIdWithPlaces(@Param("id") Integer id);

    @Query("SELECT COUNT(p) FROM Place p WHERE p.hall.id = :hallId")
    Long countPlacesBySalleId(@Param("hallId") Integer hallId);

    boolean existsByCodeUniqueIgnoreCase(String codeUnique);


}