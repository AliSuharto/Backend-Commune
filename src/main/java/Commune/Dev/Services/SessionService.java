package Commune.Dev.Services;

import Commune.Dev.Dtos.*;

import Commune.Dev.Models.Session;
import Commune.Dev.Models.Session.SessionStatus;
import Commune.Dev.Models.User;
import Commune.Dev.Repositories.SessionRepository;
import Commune.Dev.Repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final SessionMapper sessionMapper;

    @Autowired
    private TaskScheduler taskScheduler;

    private void scheduleAutoClose(Long sessionId) {
        taskScheduler.schedule(
                () -> autoCloseSession(sessionId),
                Instant.now().plus(13, ChronoUnit.HOURS)
        );
    }


    @Transactional
    public void autoCloseSession(Long sessionId) {
        Session session = sessionRepository.findById(sessionId).orElse(null);

        if (session != null && session.getStatus() == SessionStatus.OUVERTE) {
            session.setEndTime(LocalDateTime.now());
            session.setStatus(SessionStatus.FERMEE);
            sessionRepository.save(session);
        }
    }

    @Transactional(readOnly = true)
    public List<SessionDTO> getAllSessions() {
        List<Session> sessions = sessionRepository.findAll();
        return sessionMapper.toDTOList(sessions);
    }

    @Transactional(readOnly = true)
    public SessionDTO getSessionById(Long id) {
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session non trouvée avec l'id: " + id));
        return sessionMapper.toDTO(session);
    }

    @Transactional(readOnly = true)
    public List<SessionDTO> getSessionsByUserId(Long userId) {
        List<Session> sessions = sessionRepository.findByUserId(userId);
        return sessionMapper.toDTOList(sessions);
    }

    @Transactional(readOnly = true)
    public List<SessionDTO> getSessionsByStatus(Session.SessionStatus status) {
        List<Session> sessions = sessionRepository.findByStatus(status);
        return sessionMapper.toDTOList(sessions);
    }

    @Transactional
    public SessionCreatedResponseDTO createSession(CreateSessionDTO dto) {

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        // Empêche plusieurs sessions ouvertes
        List<Session> ouvertes = sessionRepository
                .findByUserIdAndStatus(user.getId(), SessionStatus.OUVERTE);

        if (!ouvertes.isEmpty()) {
            throw new RuntimeException("Une session est déjà ouverte pour cet utilisateur");
        }

        Session session = new Session();
        session.setUser(user);
        session.setNomSession(dto.getNomSession());
        session.setStartTime(LocalDateTime.now());
        session.setStatus(SessionStatus.OUVERTE);
        session.setTotalCollected(BigDecimal.ZERO);
        session.setSynced(false);
        session.setIsValid(false);

        Session saved = sessionRepository.save(session);

        // Fermeture automatique après 13h
        scheduleAutoClose(saved.getId());

        return SessionCreatedResponseDTO.builder()
                .sessionId(saved.getId())
                .nomSession(saved.getNomSession())
                .message("Session créée et ouverte avec succès")
                .build();
    }


    @Transactional
    public SessionDTO closeSession(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session non trouvée avec l'id: " + sessionId));

        if (session.getStatus() != SessionStatus.OUVERTE) {
            throw new RuntimeException("Seules les sessions ouvertes peuvent être fermées");
        }

        session.setEndTime(LocalDateTime.now());
        session.setStatus(SessionStatus.FERMEE);

        Session updatedSession = sessionRepository.save(session);
        return sessionMapper.toDTO(updatedSession);
    }

    @Transactional
    public SessionDTO validateSession(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session non trouvée avec l'id: " + sessionId));

        if (session.getStatus() != SessionStatus.FERMEE && session.getStatus() != SessionStatus.EN_VALIDATION) {
            throw new RuntimeException("Seules les sessions fermées ou en validation peuvent être validées");
        }

        session.setStatus(SessionStatus.VALIDEE);
        session.setIsValid(true);
        session.setSynced(true);

        Session updatedSession = sessionRepository.save(session);
        return sessionMapper.toDTO(updatedSession);
    }

    @Transactional
    public SessionDTO rejectSession(Long sessionId, String motif) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session non trouvée avec l'id: " + sessionId));

        if (session.getStatus() != SessionStatus.FERMEE && session.getStatus() != SessionStatus.EN_VALIDATION) {
            throw new RuntimeException("Seules les sessions fermées ou en validation peuvent être rejetées");
        }

        session.setStatus(SessionStatus.REJETEE);
        session.setIsValid(false);

        // Ajouter le motif aux notes
        String notes = session.getNotes() != null ? session.getNotes() + "\n" : "";
        notes += "Motif de rejet: " + motif;
        session.setNotes(notes);

        Session updatedSession = sessionRepository.save(session);
        return sessionMapper.toDTO(updatedSession);
    }

    @Transactional
    public SessionDTO submitSessionForValidation(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session non trouvée avec l'id: " + sessionId));

        if (session.getStatus() != SessionStatus.FERMEE) {
            throw new RuntimeException("Seules les sessions fermées peuvent être soumises pour validation");
        }

        session.setStatus(SessionStatus.EN_VALIDATION);

        Session updatedSession = sessionRepository.save(session);
        return sessionMapper.toDTO(updatedSession);
    }

    public Session getOpenSessionByUser(Long userId) {
        System.out.println(userId);

        return sessionRepository.findOpenSessionByUser(userId, SessionStatus.OUVERTE)
                .orElseThrow(() -> new RuntimeException(
                        "Aucune session ouverte trouvée pour l'utilisateur ID : " + userId

                )

                );
    }


}