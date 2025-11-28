package Commune.Dev.Controller;
import Commune.Dev.Dtos.CreateSessionDTO;
import Commune.Dev.Dtos.SessionCreatedResponseDTO;
import Commune.Dev.Dtos.SessionDTO;
import Commune.Dev.Dtos.SessionResponseDTO;
import Commune.Dev.Models.Session;
import Commune.Dev.Services.SessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    // Créer une nouvelle session
    @PostMapping
    public ResponseEntity<SessionCreatedResponseDTO> createSession(@Valid @RequestBody CreateSessionDTO createSessionDTO) {
        SessionCreatedResponseDTO response = sessionService.createSession(createSessionDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Récupérer toutes les sessions
    @GetMapping
    public ResponseEntity<List<SessionDTO>> getAllSessions() {
        return ResponseEntity.ok(sessionService.getAllSessions());
    }

    // Récupérer une session par ID
    @GetMapping("/{id}")
    public ResponseEntity<SessionDTO> getSessionById(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.getSessionById(id));
    }

    @GetMapping("/user/{userId}/open")
    public ResponseEntity<?> getOpenSessionByUser(@PathVariable Long userId) {


        try {

            Session session = sessionService.getOpenSessionByUser(userId);
            return ResponseEntity.ok(SessionResponseDTO.fromEntity(session));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Récupérer les sessions d'un utilisateur
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SessionDTO>> getSessionsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(sessionService.getSessionsByUserId(userId));
    }

    // Récupérer les sessions par statut
    @GetMapping("/status/{status}")
    public ResponseEntity<List<SessionDTO>> getSessionsByStatus(@PathVariable Session.SessionStatus status) {
        return ResponseEntity.ok(sessionService.getSessionsByStatus(status));
    }

    // Fermer une session
    @PutMapping("/{id}/close")
    public ResponseEntity<SessionDTO> closeSession(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.closeSession(id));
    }

    // Soumettre une session pour validation
    @PutMapping("/{id}/submit")
    public ResponseEntity<SessionDTO> submitForValidation(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.submitSessionForValidation(id));
    }

    // Valider une session
    @PutMapping("/{id}/validate")
    public ResponseEntity<SessionDTO> validateSession(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.validateSession(id));
    }

    // Rejeter une session
    @PutMapping("/{id}/reject")
    public ResponseEntity<SessionDTO> rejectSession(
            @PathVariable Long id,
            @RequestParam String motif) {
        return ResponseEntity.ok(sessionService.rejectSession(id, motif));
    }
}