package Commune.Dev.Services;

import Commune.Dev.Models.Session;
import Commune.Dev.Repositories.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SessionService {

    private final SessionRepository sessionRepository;

    // Créer une nouvelle session
    public Session createSession(Session session) {
        if (session.getStartTime() == null) {
            session.setStartTime(LocalDateTime.now());
        }
        if (session.getStatus() == null) {
            session.setStatus(Session.SessionStatus.OUVERTE);
        }
        if (session.getTotalCollected() == null) {
            session.setTotalCollected(0.0);
        }
        if (session.getSynced() == null) {
            session.setSynced(false);
        }
        return sessionRepository.save(session);
    }

    // Obtenir toutes les sessions
    public List<Session> getAllSessions() {
        return sessionRepository.findAll();
    }

    // Obtenir une session par ID
    public Session getSessionById(Long id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session non trouvée avec l'ID : " + id));
    }

    // Obtenir les sessions par utilisateur
    public List<Session> getSessionsByUser(Long userId) {
        return sessionRepository.findByUserId(userId);
    }

    // Obtenir les sessions par type
    public List<Session> getSessionsByType(Session.SessionType type) {
        return sessionRepository.findByType(type);
    }

    // Obtenir les sessions par statut
    public List<Session> getSessionsByStatus(Session.SessionStatus status) {
        return sessionRepository.findByStatus(status);
    }

    // Ouvrir une session
    public Session ouvrirSession(Long id) {
        Session session = getSessionById(id);
        session.setStatus(Session.SessionStatus.OUVERTE);
        session.setStartTime(LocalDateTime.now());
        return sessionRepository.save(session);
    }

    // Fermer une session
    public Session fermerSession(Long id) {
        Session session = getSessionById(id);
        if (!session.getStatus().equals(Session.SessionStatus.OUVERTE)) {
            throw new RuntimeException("Seules les sessions ouvertes peuvent être fermées");
        }
        session.setStatus(Session.SessionStatus.FERMEE);
        session.setEndTime(LocalDateTime.now());
        return sessionRepository.save(session);
    }

    // Valider une session
    public Session validerSession(Long id) {
        Session session = getSessionById(id);
        if (!session.getStatus().equals(Session.SessionStatus.FERMEE) &&
                !session.getStatus().equals(Session.SessionStatus.EN_VALIDATION)) {
            throw new RuntimeException("Seules les sessions fermées ou en validation peuvent être validées");
        }
        session.setStatus(Session.SessionStatus.VALIDEE);
        return sessionRepository.save(session);
    }

    // Rejeter une session
    public Session rejeterSession(Long id, String motif) {
        Session session = getSessionById(id);
        session.setStatus(Session.SessionStatus.REJETEE);
        if (motif != null && !motif.isEmpty()) {
            String notes = session.getNotes() != null ? session.getNotes() + "\n" : "";
            session.setNotes(notes + "Motif du rejet : " + motif);
        }
        return sessionRepository.save(session);
    }

    // Synchroniser une session
    public Session synchroniserSession(Long id) {
        Session session = getSessionById(id);
        session.setSynced(true);
        return sessionRepository.save(session);
    }

    // Obtenir les sessions non synchronisées
    public List<Session> getSessionsNonSynchronisees() {
        return sessionRepository.findBySynced(false);
    }

    // Mettre à jour une session
    public Session updateSession(Long id, Session sessionDetails) {
        Session session = getSessionById(id);

        if (sessionDetails.getType() != null) {
            session.setType(sessionDetails.getType());
        }
        if (sessionDetails.getStatus() != null) {
            session.setStatus(sessionDetails.getStatus());
        }
        if (sessionDetails.getTotalCollected() != null) {
            session.setTotalCollected(sessionDetails.getTotalCollected());
        }
        if (sessionDetails.getNotes() != null) {
            session.setNotes(sessionDetails.getNotes());
        }
        if (sessionDetails.getEndTime() != null) {
            session.setEndTime(sessionDetails.getEndTime());
        }

        return sessionRepository.save(session);
    }

    // Supprimer une session
    public void deleteSession(Long id) {
        Session session = getSessionById(id);
        sessionRepository.delete(session);
    }

    // Obtenir le montant total collecté
    public Double getTotalCollected(Long id) {
        Session session = getSessionById(id);
        return session.getTotalCollected() != null ? session.getTotalCollected() : 0.0;
    }

    // Calculer et mettre à jour le montant total collecté depuis les paiements
    public Session calculerTotalCollecte(Long id) {
        Session session = getSessionById(id);
        Double total = session.getPaiements() != null
                ? session.getPaiements().stream()
                .mapToDouble(p -> p.getMontant() != null ? p.getMontant() : 0.0)
                .sum()
                : 0.0;
        session.setTotalCollected(total);
        return sessionRepository.save(session);
    }
}
