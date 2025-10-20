package Commune.Dev.Repositories;

import Commune.Dev.Models.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    // Trouver les sessions par utilisateur
    List<Session> findByUserId(Long userId);

    // Trouver les sessions par type
    List<Session> findByType(Session.SessionType type);

    // Trouver les sessions par statut
    List<Session> findByStatus(Session.SessionStatus status);

    // Trouver les sessions non synchronisées
    List<Session> findBySynced(Boolean synced);

    // Trouver les sessions par utilisateur et statut
    List<Session> findByUserIdAndStatus(Long userId, Session.SessionStatus status);

    // Trouver les sessions par type et statut
    List<Session> findByTypeAndStatus(Session.SessionType type, Session.SessionStatus status);

    // Trouver les sessions ouvertes par utilisateur
    List<Session> findByUserIdAndStatusOrderByStartTimeDesc(Long userId, Session.SessionStatus status);
}