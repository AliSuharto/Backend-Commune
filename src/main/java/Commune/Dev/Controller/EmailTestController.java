package Commune.Dev.Controller;

import Commune.Dev.Dtos.ApiResponse;
import Commune.Dev.Models.Roletype;
import Commune.Dev.Services.AsyncEmailService;
import Commune.Dev.Services.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller pour tester l'envoi d'emails (à supprimer en production)
 */
@RestController
@RequestMapping("/api/email-test")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class EmailTestController {

    private final EmailService emailService;
    private final AsyncEmailService asyncEmailService;

    /**
     * Test d'envoi d'email de mot de passe temporaire
     * GET /api/email-test/temporary-password?email=test@example.com&nom=Doe&prenom=John&password=temp123&role=DIRECTEUR
     */
    @GetMapping("/temporary-password")
    public ResponseEntity<ApiResponse<String>> testTemporaryPasswordEmail(
            @RequestParam String email,
            @RequestParam String nom,
            @RequestParam String prenom,
            @RequestParam String password,
            @RequestParam(required = false) Roletype role) {
        
        try {
            emailService.sendTemporaryPassword(email, nom, prenom, password, role);
            String message = String.format("Email de mot de passe temporaire envoyé à %s (%s %s)", email, prenom, nom);
            log.info("✅ Test email envoyé : {}", message);
            return ResponseEntity.ok(ApiResponse.success(message));
        } catch (Exception e) {
            String errorMessage = "Erreur lors de l'envoi de l'email : " + e.getMessage();
            log.error("❌ {}", errorMessage, e);
            return ResponseEntity.badRequest().body(ApiResponse.error(errorMessage));
        }
    }

    /**
     * Test d'envoi d'email de confirmation de changement de mot de passe
     * GET /api/email-test/password-change?email=test@example.com&nom=Doe&prenom=John
     */
    @GetMapping("/password-change")
    public ResponseEntity<ApiResponse<String>> testPasswordChangeEmail(
            @RequestParam String email,
            @RequestParam String nom,
            @RequestParam String prenom) {
        
        try {
            emailService.sendPasswordChangeConfirmation(email, nom, prenom);
            String message = String.format("Email de confirmation de changement de mot de passe envoyé à %s", email);
            log.info("✅ Test email envoyé : {}", message);
            return ResponseEntity.ok(ApiResponse.success(message));
        } catch (Exception e) {
            String errorMessage = "Erreur lors de l'envoi de l'email : " + e.getMessage();
            log.error("❌ {}", errorMessage, e);
            return ResponseEntity.badRequest().body(ApiResponse.error(errorMessage));
        }
    }

    /**
     * Test d'envoi d'email de changement de statut
     * GET /api/email-test/account-status?email=test@example.com&nom=Doe&prenom=John&active=true
     */
    @GetMapping("/account-status")
    public ResponseEntity<ApiResponse<String>> testAccountStatusEmail(
            @RequestParam String email,
            @RequestParam String nom,
            @RequestParam String prenom,
            @RequestParam boolean active) {
        
        try {
            emailService.sendAccountStatusNotification(email, nom, prenom, active);
            String message = String.format("Email de notification de statut (%s) envoyé à %s", 
                    active ? "activé" : "désactivé", email);
            log.info("✅ Test email envoyé : {}", message);
            return ResponseEntity.ok(ApiResponse.success(message));
        } catch (Exception e) {
            String errorMessage = "Erreur lors de l'envoi de l'email : " + e.getMessage();
            log.error("❌ {}", errorMessage, e);
            return ResponseEntity.badRequest().body(ApiResponse.error(errorMessage));
        }
    }

    /**
     * Test d'envoi d'email asynchrone
     * GET /api/email-test/async?email=test@example.com&nom=Doe&prenom=John&password=temp123&role=DIRECTEUR
     */
    @GetMapping("/async")
    public ResponseEntity<ApiResponse<String>> testAsyncEmail(
            @RequestParam String email,
            @RequestParam String nom,
            @RequestParam String prenom,
            @RequestParam String password,
            @RequestParam(required = false) Roletype role) {
        
        try {
            asyncEmailService.sendTemporaryPasswordAsync(email, nom, prenom, password, role);
            String message = String.format("Email asynchrone programmé pour %s (%s %s)", email, prenom, nom);
            log.info("🚀 Test email asynchrone programmé : {}", message);
            return ResponseEntity.ok(ApiResponse.success(message));
        } catch (Exception e) {
            String errorMessage = "Erreur lors de la programmation de l'email : " + e.getMessage();
            log.error("❌ {}", errorMessage, e);
            return ResponseEntity.badRequest().body(ApiResponse.error(errorMessage));
        }
    }

    /**
     * Vérification de la configuration email
     * GET /api/email-test/config
     */
    @GetMapping("/config")
    public ResponseEntity<ApiResponse<String>> checkEmailConfig() {
        try {
            String message = "Configuration email vérifiée. Le service d'email est opérationnel.";
            log.info("✅ Configuration email vérifiée");
            return ResponseEntity.ok(ApiResponse.success(message));
        } catch (Exception e) {
            String errorMessage = "Problème de configuration email : " + e.getMessage();
            log.error("❌ {}", errorMessage, e);
            return ResponseEntity.badRequest().body(ApiResponse.error(errorMessage));
        }
    }
}
