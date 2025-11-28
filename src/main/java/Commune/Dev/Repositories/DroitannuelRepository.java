package Commune.Dev.Repositories;

import Commune.Dev.Models.DroitAnnuel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface DroitannuelRepository extends JpaRepository<DroitAnnuel, Integer> {

    Optional<DroitAnnuel> findByMontant(BigDecimal montant);
}
