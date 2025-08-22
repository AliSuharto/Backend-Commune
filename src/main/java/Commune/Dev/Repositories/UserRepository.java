package Commune.Dev.Repositories;

import Commune.Dev.Models.Utilisateurs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface UserRepository extends JpaRepository<Utilisateurs, Integer>{


}
