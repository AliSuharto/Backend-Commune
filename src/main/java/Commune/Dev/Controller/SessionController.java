package Commune.Dev.Controller;

import Commune.Dev.Models.Session;
import Commune.Dev.Services.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SessionController {

    private final SessionService sessionService;

    // Créer une nouvelle session
    @PostMapping
    public ResponseEntity<Session> createSession(@RequestBody Session session) {
        Session createdSession = sessionService.createSession(session);
        return new ResponseEntity<>(createdSession, HttpStatus.CREATED);
    }

    // Obtenir toutes les sessions
    @GetMapping
    public ResponseEntity<List<Session>> getAllSessions() {
        List<Session> sessions = sessionService.getAllSessions();
        return ResponseEntity.ok(sessions);
    }

    // Obtenir une session par ID
    @GetMapping("/{id}")
    public ResponseEntity<Session> getSessionById(@PathVariable Long id) {
        Session session = sessionService.getSessionById(id);
        return ResponseEntity.ok(session);
    }

    // Obtenir les sessions par utilisateur
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Session>> getSessionsByUser(@PathVariable Long userId) {
        List<Session> sessions = sessionService.getSessionsByUser(userId);
        return ResponseEntity.ok(sessions);
    }

    // Obtenir les sessions par type
    @GetMapping("/type/{type}")
    public ResponseEntity<List<Session>> getSessionsByType(@PathVariable Session.SessionType type) {
        List<Session> sessions = sessionService.getSessionsByType(type);
        return ResponseEntity.ok(sessions);
    }

    // Obtenir les sessions par statut
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Session>> getSessionsByStatus(@PathVariable Session.SessionStatus status) {
        List<Session> sessions = sessionService.getSessionsByStatus(status);
        return ResponseEntity.ok(sessions);
    }

    // Ouvrir une session
    @PostMapping("/{id}/ouvrir")
    public ResponseEntity<Session> ouvrirSession(@PathVariable Long id) {
        Session session = sessionService.ouvrirSession(id);
        return ResponseEntity.ok(session);
    }

    // Fermer une session
    @PostMapping("/{id}/fermer")
    public ResponseEntity<Session> fermerSession(@PathVariable Long id) {
        Session session = sessionService.fermerSession(id);
        return ResponseEntity.ok(session);
    }

    // Valider une session
    @PostMapping("/{id}/valider")
    public ResponseEntity<Session> validerSession(@PathVariable Long id) {
        Session session = sessionService.validerSession(id);
        return ResponseEntity.ok(session);
    }

    // Rejeter une session
    @PostMapping("/{id}/rejeter")
    public ResponseEntity<Session> rejeterSession(@PathVariable Long id, @RequestBody String motif) {
        Session session = sessionService.rejeterSession(id, motif);
        return ResponseEntity.ok(session);
    }

    // Synchroniser une session
    @PostMapping("/{id}/synchroniser")
    public ResponseEntity<Session> synchroniserSession(@PathVariable Long id) {
        Session session = sessionService.synchroniserSession(id);
        return ResponseEntity.ok(session);
    }

    // Mettre à jour une session
    @PutMapping("/{id}")
    public ResponseEntity<Session> updateSession(@PathVariable Long id, @RequestBody Session session) {
        Session updatedSession = sessionService.updateSession(id, session);
        return ResponseEntity.ok(updatedSession);
    }

    // Supprimer une session
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
        sessionService.deleteSession(id);
        return ResponseEntity.noContent().build();
    }

    // Obtenir les sessions ouvertes
    @GetMapping("/ouvertes")
    public ResponseEntity<List<Session>> getSessionsOuvertes() {
        List<Session> sessions = sessionService.getSessionsByStatus(Session.SessionStatus.OUVERTE);
        return ResponseEntity.ok(sessions);
    }

    // Obtenir les sessions non synchronisées
    @GetMapping("/non-synchronisees")
    public ResponseEntity<List<Session>> getSessionsNonSynchronisees() {
        List<Session> sessions = sessionService.getSessionsNonSynchronisees();
        return ResponseEntity.ok(sessions);
    }

    // Obtenir le montant total collecté pour une session
    @GetMapping("/{id}/total")
    public ResponseEntity<Double> getTotalCollected(@PathVariable Long id) {
        Double total = sessionService.getTotalCollected(id);
        return ResponseEntity.ok(total);
    }
}
