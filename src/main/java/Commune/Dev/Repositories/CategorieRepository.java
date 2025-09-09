package Commune.Dev.Repositories;

import Commune.Dev.Models.Categorie;
import Commune.Dev.Models.Categorie.CategorieNom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategorieRepository extends JpaRepository<Categorie, Integer> {

    // Vérifier si une catégorie existe par nom
    boolean existsByNom(CategorieNom nom);

    // Trouver par nom
    Optional<Categorie> findByNom(CategorieNom nom);

    // Requête personnalisée pour récupérer sans les relations
    @Query("SELECT c FROM Categorie c WHERE c.id = :id")
    Optional<Categorie> findByIdWithoutRelations(Integer id);

    // Récupérer toutes les catégories sans relations
    @Query("SELECT c FROM Categorie c")
    List<Categorie> findAllWithoutRelations();
}
