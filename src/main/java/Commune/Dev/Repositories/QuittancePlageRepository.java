package Commune.Dev.Repositories;

import Commune.Dev.Models.QuittancePlage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuittancePlageRepository extends JpaRepository<QuittancePlage, Long> {
    List<QuittancePlage> findByPercepteurIdOrderByCreatedAtDesc(Long percepteurId);

    List<QuittancePlage> findByPercepteurId(Long percepteurId);
}

