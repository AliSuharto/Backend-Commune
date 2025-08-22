package Commune.Dev.Repositories;

import Commune.Dev.Models.Marchands;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MarchandsRepository extends JpaRepository<Marchands, Integer> {

    Optional<Marchands> findByNumCIN(String numCIN);

    List<Marchands> findByNomContainingIgnoreCase(String nom);

    List<Marchands> findByPrenomContainingIgnoreCase(String prenom);

    @Query("SELECT m FROM Marchands m WHERE m.nom LIKE %:nom% AND m.prenom LIKE %:prenom%")
    List<Marchands> findByNomAndPrenom(@Param("nom") String nom, @Param("prenom") String prenom);
}

