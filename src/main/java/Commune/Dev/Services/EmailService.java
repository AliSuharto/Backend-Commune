package Commune.Dev.Services;

import Commune.Dev.Models.Roletype;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Envoie un email avec le mot de passe temporaire pour un nouvel utilisateur
     */
    @Async("taskExecutor")
    public void sendTemporaryPassword(String to, String nom, String prenom, String temporaryPassword) {
        sendTemporaryPassword(to, nom, prenom, temporaryPassword, null);
    }

    /**
     * Envoie un email avec le mot de passe temporaire pour un nouvel utilisateur avec rôle spécifique
     */
    @Async("taskExecutor")
    public void sendTemporaryPassword(String to, String nom, String prenom, String temporaryPassword, Roletype role) {
        try {
            log.info("📧 [ASYNC] Début d'envoi d'email de mot de passe temporaire à : {} (Rôle: {})", to, role);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setFrom(fromEmail);
            message.setSubject("🔑 Votre compte a été créé - Système de Gestion Communale");

            String roleDescription = getRoleDescription(role);

            message.setText(
                    String.format(
                            "Bonjour %s %s,\n\n" +
                                    "🎉 Félicitations ! Votre compte a été créé avec succès dans le Système de Gestion Communale.\n\n" +
                                    "👤 VOS INFORMATIONS DE CONNEXION :\n" +
                                    "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                                    "📧 Email : %s\n" +
                                    "🔐 Mot de passe temporaire : %s\n" +
                                    "🏷️ Rôle : %s\n\n" +
                                    "⚠️ IMPORTANT - SÉCURITÉ :\n" +
                                    "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                                    "• Vous DEVEZ changer ce mot de passe lors de votre première connexion\n" +
                                    "• Gardez ces informations confidentielles\n" +
                                    "• Ne partagez jamais votre mot de passe\n\n" +
                                    "🌐 ACCÈS AU SYSTÈME :\n" +
                                    "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                                    "URL : http://localhost:8080\n" +
                                    "Support : Contactez votre administrateur système\n\n" +
                                    "Cordialement,\n" +
                                    "🏛️ L'équipe du Système de Gestion Communale",
                            prenom, nom, to, temporaryPassword, roleDescription
                    )
            );

            mailSender.send(message);
            log.info("✅ [ASYNC] Email de mot de passe temporaire envoyé avec succès à : {} (Rôle: {})", to, role);
        } catch (Exception e) {
            log.error("❌ [ASYNC] Erreur lors de l'envoi de l'email à : {} - {}", to, e.getMessage(), e);
            // Ne pas relancer l'exception pour ne pas bloquer le thread
        }
    }

    /**
     * Envoie une notification de modification de compte
     */
    @Async("taskExecutor")
    public void sendAccountModificationNotification(String to, String nom, String prenom, String modification) {
        try {
            log.info("📧 [ASYNC] Début d'envoi d'email de modification à : {}", to);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setFrom(fromEmail);
            message.setSubject("🔄 Modification de votre compte - Système de Gestion Communale");
            message.setText(
                    String.format(
                            "Bonjour %s %s,\n\n" +
                                    "📝 NOTIFICATION DE MODIFICATION :\n" +
                                    "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                                    "Votre compte a été modifié :\n\n" +
                                    "%s\n\n" +
                                    "⚠️ SÉCURITÉ :\n" +
                                    "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                                    "Si vous n'êtes pas à l'origine de cette modification, \n" +
                                    "contactez immédiatement votre administrateur système.\n\n" +
                                    "Cordialement,\n" +
                                    "🏛️ L'équipe du Système de Gestion Communale",
                            prenom, nom, modification
                    )
            );

            mailSender.send(message);
            log.info("✅ [ASYNC] Email de notification de modification envoyé avec succès à : {}", to);
        } catch (Exception e) {
            log.error("❌ [ASYNC] Erreur lors de l'envoi de l'email de notification à : {} - {}", to, e.getMessage(), e);
            // Ne pas relancer l'exception
        }
    }

    /**
     * Envoie un email de confirmation de changement de mot de passe
     */
    @Async("taskExecutor")
    public void sendPasswordChangeConfirmation(String to, String nom, String prenom) {
        try {
            log.info("📧 [ASYNC] Début d'envoi d'email de confirmation de mot de passe à : {}", to);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setFrom(fromEmail);
            message.setSubject("✅ Mot de passe modifié avec succès");
            message.setText(
                    String.format(
                            "Bonjour %s %s,\n\n" +
                                    "🔐 CONFIRMATION DE SÉCURITÉ :\n" +
                                    "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                                    "Votre mot de passe a été modifié avec succès.\n\n" +
                                    "📅 Date : %s\n" +
                                    "🕐 Heure : %s\n\n" +
                                    "⚠️ Si ce n'était pas vous :\n" +
                                    "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                                    "Contactez immédiatement votre administrateur système.\n\n" +
                                    "Cordialement,\n" +
                                    "🏛️ L'équipe du Système de Gestion Communale",
                            prenom, nom,
                            java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                            java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
                    )
            );

            mailSender.send(message);
            log.info("✅ [ASYNC] Email de confirmation de changement de mot de passe envoyé avec succès à : {}", to);
        } catch (Exception e) {
            log.error("❌ [ASYNC] Erreur lors de l'envoi de l'email de confirmation à : {} - {}", to, e.getMessage(), e);
            // Ne pas relancer l'exception
        }
    }

    /**
     * Envoie un email d'activation/désactivation de compte
     */
    @Async("taskExecutor")
    public void sendAccountStatusNotification(String to, String nom, String prenom, boolean isActive) {
        try {
            log.info("📧 [ASYNC] Début d'envoi d'email de statut de compte à : {} (Statut: {})", to, isActive ? "Activé" : "Désactivé");

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setFrom(fromEmail);

            String status = isActive ? "✅ ACTIVÉ" : "❌ DÉSACTIVÉ";
            String emoji = isActive ? "🎉" : "⚠️";

            message.setSubject(String.format("%s Votre compte a été %s", emoji, isActive ? "activé" : "désactivé"));
            message.setText(
                    String.format(
                            "Bonjour %s %s,\n\n" +
                                    "📢 CHANGEMENT DE STATUT :\n" +
                                    "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                                    "Statut de votre compte : %s\n\n" +
                                    "%s\n\n" +
                                    "Pour plus d'informations, contactez votre administrateur système.\n\n" +
                                    "Cordialement,\n" +
                                    "🏛️ L'équipe du Système de Gestion Communale",
                            prenom, nom, status,
                            isActive ?
                                    "Vous pouvez maintenant vous connecter au système." :
                                    "Votre accès au système a été temporairement suspendu."
                    )
            );

            mailSender.send(message);
            log.info("✅ [ASYNC] Email de notification de statut envoyé avec succès à : {} (Statut: {})", to, isActive ? "Activé" : "Désactivé");
        } catch (Exception e) {
            log.error("❌ [ASYNC] Erreur lors de l'envoi de l'email de statut à : {} - {}", to, e.getMessage(), e);
            // Ne pas relancer l'exception
        }
    }

    /**
     * Retourne la description du rôle en français
     */
    private String getRoleDescription(Roletype role) {
        if (role == null) return "Utilisateur";

        return switch (role) {
            case ORDONNATEUR -> "Ordonnateur - Responsable de la gestion communale";
            case DIRECTEUR -> "Directeur - Supervision et gestion";
            case PERCEPTEUR -> "Percepteur - Gestion des recettes";
            case CREATEUR_MARCHE -> "Createur de marchee - Creer le marchee";
            case REGISSEUR_MOBILE -> "Régisseur Mobile - Collecte sur terrain";
            case REGISSEUR -> "Régisseur Fixe - Gestion d'un point fixe";
            case REGISSEUR_FIXE -> "Régisseur Fixe - Gestion d'un point fixe";
            case REGISSEUR_PRINCIPAL -> "Régisseur Principal - Coordination des régisseurs";
        };
    }
}