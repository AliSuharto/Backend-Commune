package Commune.Dev.Repositories;

import Commune.Dev.Models.OtpTemp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpTempRepository extends JpaRepository<OtpTemp, Long> {
    Optional<OtpTemp> findTopByEmailOrderByCreatedAtDesc(String email);
    void deleteByEmail(String email);
}

