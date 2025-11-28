package Commune.Dev.Repositories;

import Commune.Dev.Models.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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

    //Trouver listes des sessions valider ou non valider
    List<Session> findByIsValid(Boolean isValid);

    // Trouver les sessions par utilisateur et statut
    List<Session> findByUserIdAndStatus(Long userId, Session.SessionStatus status);

    // Trouver les sessions par type et statut
    List<Session> findByTypeAndStatus(Session.SessionType type, Session.SessionStatus status);

    // Trouver les sessions ouvertes par utilisateur
    List<Session> findByUserIdAndStatusOrderByStartTimeDesc(Long userId, Session.SessionStatus status);

    @Query("""
        SELECT s FROM Session s
        JOIN FETCH s.user u
        WHERE u.id = :userId
        AND s.status = :status
    """)
    Optional<Session> findOpenSessionByUser(Long userId, Session.SessionStatus status);

}