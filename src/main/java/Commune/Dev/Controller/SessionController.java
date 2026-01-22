package Commune.Dev.Controller;
import Commune.Dev.Dtos.*;
import Commune.Dev.Models.Session;
import Commune.Dev.Models.User;
import Commune.Dev.Request.ValidateSessionRequest;
import Commune.Dev.Services.JwtManualService;
import Commune.Dev.Services.RegisseurDashboardService;
import Commune.Dev.Services.SessionService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;
    private final JwtManualService jwtManualService;
    private final RegisseurDashboardService regisseurDashboardService;

    // Créer une nouvelle session
    @PostMapping
    public ResponseEntity<SessionCreatedResponseDTO> createSession(@Valid @RequestBody CreateSessionDTO createSessionDTO) {
        SessionCreatedResponseDTO response = sessionService.createSession(createSessionDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    /**
     * Crée une nouvelle session avec validation JWT manuelle
     * Accessible uniquement aux REGISSEUR et PERCEPTEUR
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<SessionCreatedResponseDTO>> createSession(
            @Valid @RequestBody CreateSessionDTO createSessionDTO,
            HttpServletRequest request) {

        // 🔐 Extraction du token
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Token manquant"));
        }

        String token = authHeader.substring(7);

        // 🔐 Validation du token
        Claims claims;
        try {
            claims = jwtManualService.decodeAndValidate(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Token invalide ou expiré"));
        }

        // 🔐 Vérification du rôle
        String role = claims.get("role", String.class);
        Long userId = claims.get("id", Long.class);

        if (!List.of("REGISSEUR", "PERCEPTEUR").contains(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Rôle non autorisé. Seuls les REGISSEUR et PERCEPTEUR peuvent créer des sessions"));
        }

        // ✅ Création de la session
        try {
            // Optionnel : ajouter l'ID de l'utilisateur créateur dans le DTO
            SessionCreatedResponseDTO response = sessionService.createSessionMobile(createSessionDTO);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(
                            "Session créée avec succès",
                            response
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erreur lors de la création de la session: " + e.getMessage()));
        }
    }








    // Récupérer toutes les sessions
    @GetMapping
    public ResponseEntity<List<SessionDTO>> getAllSessions() {
        return ResponseEntity.ok(sessionService.getAllSessions());
    }




    @GetMapping ("/validationEnAttente")
    public ResponseEntity<List<SessionDTO>> getSessionEnAttenteDeValidation() {
        return ResponseEntity.ok(sessionService.getSessionEnAttenteDeValidation());
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
    @PostMapping("/validate")
    public Session validateSession(@RequestBody ValidateSessionRequest request) {
        return sessionService.validerSession(request);
    }

    // Rejeter une session
    @PutMapping("/{id}/reject")
    public ResponseEntity<SessionDTO> rejectSession(
            @PathVariable Long id,
            @RequestParam String motif) {
        return ResponseEntity.ok(sessionService.rejectSession(id, motif));
    }

    @GetMapping("/dashboard/{userId}")
    public ResponseEntity<RegisseurDashboardDTO> getDashboardByUserId(@PathVariable Integer userId) {
        // Endpoint pour les admins qui veulent voir le dashboard d'un régisseur spécifique
        RegisseurDashboardDTO dashboard = regisseurDashboardService.getDashboardData(userId);
        return ResponseEntity.ok(dashboard);
    }

    private Integer getUserIdFromAuthentication(Authentication authentication) {
        // Cette méthode dépend de votre implémentation de sécurité
        // Adaptez selon votre UserDetails ou principal
        if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
            // Si vous avez une classe UserDetails personnalisée avec getId()
            return Math.toIntExact(((User) authentication.getPrincipal()).getId());
        }
        throw new RuntimeException("Utilisateur non authentifié");
    }







}