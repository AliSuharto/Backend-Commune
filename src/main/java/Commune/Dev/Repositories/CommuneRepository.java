package Commune.Dev.Repositories;

import Commune.Dev.Models.Commune;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CommuneRepository extends JpaRepository<Commune, Long> {


}
