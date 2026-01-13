package Commune.Dev.Services;

import Commune.Dev.Dtos.*;
import Commune.Dev.Models.Roletype;
import Commune.Dev.Models.Session;
import Commune.Dev.Models.Session.SessionStatus;
import Commune.Dev.Models.User;
import Commune.Dev.Repositories.SessionRepository;
import Commune.Dev.Repositories.UserRepository;
import Commune.Dev.Request.ValidateSessionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final SessionMapper sessionMapper;

    /* =======================================================================
       🔥 FERMETURE AUTOMATIQUE DES SESSIONS APRÈS 13h
       ======================================================================= */
    @Scheduled(cron = "0 */1 * * * *") // Toutes les minutes (fiable et léger)
    @Transactional
    public void autoCloseExpiredSessions() {

        LocalDateTime now = LocalDateTime.now();
        List<Session> sessions = sessionRepository.findByStatus(SessionStatus.OUVERTE);

        for (Session s : sessions) {

            // Sécurité : session sans startTime = ignorer
            if (s.getStartTime() == null) continue;

            long hours = ChronoUnit.HOURS.between(s.getStartTime(), now);

            if (hours >= 13) {
                s.setEndTime(now);
                s.setStatus(SessionStatus.EN_VALIDATION);
                sessionRepository.save(s);
            }
        }
    }

    /* =======================================================================
       🔥 CRUD + LOGIQUE MÉTIER
       ======================================================================= */

    @Transactional(readOnly = true)
    public List<SessionDTO> getAllSessions() {
        return sessionMapper.toDTOList(sessionRepository.findAll());
    }

    @Transactional(readOnly = true)
    public List<SessionDTO> getSessionEnAttenteDeValidation() {
        return sessionMapper.toDTOList(sessionRepository.findByStatus(Session.SessionStatus.EN_VALIDATION));
    }

    @Transactional(readOnly = true)
    public SessionDTO getSessionById(Long id) {
        return sessionMapper.toDTO(
                sessionRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Session non trouvée avec l'id: " + id))
        );
    }

    @Transactional(readOnly = true)
    public List<SessionDTO> getSessionsByUserId(Long userId) {
        return sessionMapper.toDTOList(sessionRepository.findByUserId(userId));
    }

    @Transactional(readOnly = true)
    public List<SessionDTO> getSessionsByStatus(SessionStatus status) {
        return sessionMapper.toDTOList(sessionRepository.findByStatus(status));
    }

    /* =======================================================================
       🔥 CRÉATION D’UNE NOUVELLE SESSION
       ======================================================================= */
    @Transactional
    public SessionCreatedResponseDTO createSession(CreateSessionDTO dto) {

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        // Vérifie si une session ouverte existe déjà
        List<Session> ouvertes = sessionRepository.findByUserIdAndStatus(
                user.getId(),
                SessionStatus.OUVERTE
        );

        if (!ouvertes.isEmpty()) {
            throw new RuntimeException("Une session est déjà ouverte pour cet utilisateur");
        }

        // Création
        Session session = new Session();
        session.setUser(user);
        session.setNomSession(dto.getNomSession());
        session.setStartTime(LocalDateTime.now());
        session.setStatus(SessionStatus.OUVERTE);
        session.setTotalCollected(BigDecimal.ZERO);
        session.setSynced(false);
        session.setIsValid(false);

        Session saved = sessionRepository.save(session);

        return SessionCreatedResponseDTO.builder()
                .sessionId(saved.getId())
                .nomSession(saved.getNomSession())
                .message("Session créée et ouverte avec succès")
                .build();
    }
// Pour MOBILE
@Transactional
public SessionCreatedResponseDTO createSessionMobile(CreateSessionDTO dto) {

    User user = userRepository.findById(dto.getUserId())
            .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

    // Vérifie si une session ouverte existe déjà
    List<Session> ouvertes = sessionRepository.findByUserIdAndStatus(
            user.getId(),
            SessionStatus.OUVERTE
    );

    if (!ouvertes.isEmpty()) {
        throw new RuntimeException("Une session est déjà ouverte pour cet utilisateur");
    }

    // Création
    Session session = new Session();
    session.setUser(user);
    session.setNomSession(dto.getNomSession());
    session.setStartTime(LocalDateTime.now());
    session.setStatus(SessionStatus.OUVERTE);
    session.setTotalCollected(BigDecimal.ZERO);
    session.setSynced(false);
    session.setIsValid(false);

    Session saved = sessionRepository.save(session);

    return SessionCreatedResponseDTO.builder()
            .sessionId(saved.getId())
            .nomSession(saved.getNomSession())
            .message("Session créée et ouverte avec succès")
            .build();
}









    /* =======================================================================
       🔥 FERMETURE MANUELLE
       ======================================================================= */
    @Transactional
    public SessionDTO closeSession(Long sessionId) {

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session non trouvée avec l'id: " + sessionId));

        if (session.getStatus() != SessionStatus.OUVERTE) {
            throw new RuntimeException("Seules les sessions ouvertes peuvent être fermées");
        }

        session.setEndTime(LocalDateTime.now());
        session.setStatus(SessionStatus.FERMEE);

        return sessionMapper.toDTO(sessionRepository.save(session));
    }

    /* =======================================================================
       🔥 VALIDATION
       ======================================================================= */
    @Transactional
    public Session validerSession(ValidateSessionRequest request) {

        // récupérer la session
        Session session = sessionRepository.findById(request.getId())
                .orElseThrow(() ->
                        new RuntimeException("Session non trouvée avec l'id: " + request.getId())
                );

        // vérifier l'état
        if (session.getStatus() != SessionStatus.FERMEE &&
                session.getStatus() != SessionStatus.EN_VALIDATION) {
            throw new RuntimeException("Seules les sessions fermées ou en validation peuvent être validées.");
        }

        // récupérer le régisseur principal qui valide
        User regisseur = userRepository.findById(Long.valueOf(request.getId_regisseurPrincipal()))
                .orElseThrow(() ->
                        new RuntimeException("Régisseur principal introuvable avec l'id: " + request.getId_regisseurPrincipal())
                );

        // vérifier rôle
        if (regisseur.getRole() != Roletype.REGISSEUR_PRINCIPAL) {
            throw new RuntimeException("Vous n'êtes pas autorisé à valider cette session.");
        }

        // marquer comme validée
        session.setStatus(SessionStatus.VALIDEE);
        session.setValidation_date(LocalDateTime.now());
        session.setIsValid(true);
        session.setSynced(true);

        return sessionRepository.save(session);
    }


    /* =======================================================================
       🔥 REJET
       ======================================================================= */
    @Transactional
    public SessionDTO rejectSession(Long sessionId, String motif) {

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session non trouvée avec l'id: " + sessionId));

        if (session.getStatus() != SessionStatus.FERMEE &&
                session.getStatus() != SessionStatus.EN_VALIDATION) {
            throw new RuntimeException("Seules les sessions fermées ou en validation peuvent être rejetées");
        }

        session.setStatus(SessionStatus.REJETEE);
        session.setIsValid(false);

        // Ajout du motif
        String notes = session.getNotes() != null ? session.getNotes() + "\n" : "";
        session.setNotes(notes + "Motif de rejet: " + motif);

        return sessionMapper.toDTO(sessionRepository.save(session));
    }

    /* =======================================================================
       🔥 SOUMISSION POUR VALIDATION
       ======================================================================= */
    @Transactional
    public SessionDTO submitSessionForValidation(Long sessionId) {

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session non trouvée avec l'id: " + sessionId));

        if (session.getStatus() != SessionStatus.FERMEE) {
            throw new RuntimeException("Seules les sessions fermées peuvent être soumises pour validation");
        }

        session.setStatus(SessionStatus.EN_VALIDATION);

        return sessionMapper.toDTO(sessionRepository.save(session));
    }

    /* =======================================================================
       🔥 RETOURNER UNE SESSION OUVERTE PAR UTILISATEUR
       ======================================================================= */
    public Session getOpenSessionByUser(Long userId) {
        return sessionRepository.findOpenSessionByUser(userId, SessionStatus.OUVERTE)
                .orElseThrow(() -> new RuntimeException(
                        "Aucune session ouverte trouvée pour l'utilisateur ID : " + userId
                ));
    }
}
