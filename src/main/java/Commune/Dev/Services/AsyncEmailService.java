package Commune.Dev.Services;

import Commune.Dev.Models.Roletype;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service d'envoi d'emails asynchrone pour éviter de bloquer les opérations principales
 */
@Service
public class AsyncEmailService {

    private static final Logger log = LoggerFactory.getLogger(AsyncEmailService.class);
    
    private final EmailService emailService;
    
    @Autowired
    public AsyncEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    /**
     * Envoie un email de mot de passe temporaire de manière asynchrone
     */
    @Async
    public void sendTemporaryPasswordAsync(String to, String nom, String prenom, String temporaryPassword, Roletype role) {
        try {
            emailService.sendTemporaryPassword(to, nom, prenom, temporaryPassword, role);
            log.info("🚀 Email de mot de passe temporaire envoyé de manière asynchrone à : {}", to);
        } catch (Exception e) {
            log.error("❌ Erreur lors de l'envoi asynchrone de l'email à : {}", to, e);
            // En cas d'erreur, on peut implémenter une logique de retry ou de sauvegarde
        }
    }

    /**
     * Envoie une notification de modification de compte de manière asynchrone
     */
    @Async
    public void sendAccountModificationNotificationAsync(String to, String nom, String prenom, String modification) {
        try {
            emailService.sendAccountModificationNotification(to, nom, prenom, modification);
            log.info("🚀 Email de notification de modification envoyé de manière asynchrone à : {}", to);
        } catch (Exception e) {
            log.error("❌ Erreur lors de l'envoi asynchrone de notification à : {}", to, e);
        }
    }

    /**
     * Envoie une confirmation de changement de mot de passe de manière asynchrone
     */
    @Async
    public void sendPasswordChangeConfirmationAsync(String to, String nom, String prenom) {
        try {
            emailService.sendPasswordChangeConfirmation(to, nom, prenom);
            log.info("🚀 Email de confirmation de mot de passe envoyé de manière asynchrone à : {}", to);
        } catch (Exception e) {
            log.error("❌ Erreur lors de l'envoi asynchrone de confirmation à : {}", to, e);
        }
    }

    /**
     * Envoie une notification de statut de compte de manière asynchrone
     */
    @Async
    public void sendAccountStatusNotificationAsync(String to, String nom, String prenom, boolean isActive) {
        try {
            emailService.sendAccountStatusNotification(to, nom, prenom, isActive);
            log.info("🚀 Email de notification de statut envoyé de manière asynchrone à : {} (Statut: {})", 
                    to, isActive ? "Activé" : "Désactivé");
        } catch (Exception e) {
            log.error("❌ Erreur lors de l'envoi asynchrone de statut à : {}", to, e);
        }
    }
}
