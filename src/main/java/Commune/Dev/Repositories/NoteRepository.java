package Commune.Dev.Repositories;

import Commune.Dev.Models.Notes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Commune.Dev.Models.Notes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Notes, Long> {

    List<Notes> findByUserId(Long userId);

    List<Notes> findByUserIdAndTitreContainingIgnoreCase(Long userId, String titre);

    // Optionnel : tri par date si besoin
    List<Notes> findByUserIdOrderByCreationDateDesc(Long userId);
    List<Notes> findByUserIdOrderByModifDateDesc(Long userId);
}
