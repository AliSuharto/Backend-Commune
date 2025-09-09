package Commune.Dev.Repositories;

import Commune.Dev.Models.RecuPlage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RecuPlageRepository extends JpaRepository<RecuPlage, Long> {
    List<RecuPlage> findByPercepteurIdOrderByCreatedAtDesc(Long percepteurId);
}

