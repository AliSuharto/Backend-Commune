package Commune.Dev.Services;

import Commune.Dev.Models.Roletype;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from}")
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
                                    "URL : https://egmc-plateforme-vpwk.vercel.app/login\n" +
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
    @Async
    public void sendAccountStatusChangeNotification(
            String email,
            String nom,
            String prenom,
            boolean isActivated,
            String changedByRole,
            String motif
    ) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(email);

            String subject = isActivated
                    ? "Votre compte a été réactivé"
                    : "Votre compte a été désactivé";
            helper.setSubject(subject);

            String statusColor = isActivated ? "#28a745" : "#dc3545";
            String statusText = isActivated ? "ACTIVÉ" : "DÉSACTIVÉ";
            String actionText = isActivated
                    ? "Vous pouvez maintenant vous connecter à votre compte."
                    : "Vous ne pouvez plus accéder à votre compte pour le moment.";

            String htmlContent = "<!DOCTYPE html>" +
                    "<html lang='fr'>" +
                    "<head>" +
                    "    <meta charset='UTF-8'>" +
                    "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                    "    <title>" + subject + "</title>" +
                    "</head>" +
                    "<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;'>" +
                    "    <div style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 30px; text-align: center; border-radius: 10px 10px 0 0;'>" +
                    "        <h1 style='color: white; margin: 0; font-size: 28px;'>Changement de Statut</h1>" +
                    "    </div>" +
                    "    <div style='background-color: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; box-shadow: 0 4px 6px rgba(0,0,0,0.1);'>" +
                    "        <p style='font-size: 16px; margin-bottom: 20px;'>Bonjour <strong>" + prenom + " " + nom + "</strong>,</p>" +
                    "        " +
                    "        <div style='background-color: white; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid " + statusColor + ";'>" +
                    "            <p style='margin: 0 0 15px 0; font-size: 16px;'>" +
                    "                <strong>Statut du compte :</strong> " +
                    "                <span style='color: " + statusColor + "; font-weight: bold; font-size: 18px;'>" + statusText + "</span>" +
                    "            </p>" +
                    "            <p style='margin: 0; font-size: 14px; color: #666;'>" +
                    "                <strong>Motif :</strong> " + motif +
                    "            </p>" +
                    "        </div>" +
                    "        " +
                    "        <p style='font-size: 15px; margin: 20px 0;'>" + actionText + "</p>" +
                    "        " +
                    (isActivated ?
                            "        <div style='text-align: center; margin: 30px 0;'>" +
                                    "            <a href='https://egmc-plateforme-vpwk.vercel.app/login' style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 12px 30px; text-decoration: none; border-radius: 25px; font-weight: bold; display: inline-block;'>Se connecter</a>" +
                                    "        </div>"
                            : "") +
                    "        " +
                    "        <div style='margin-top: 30px; padding-top: 20px; border-top: 1px solid #ddd;'>" +
                    "            <p style='font-size: 13px; color: #666; margin: 5px 0;'>Si vous avez des questions, veuillez contacter votre administrateur.</p>" +
                    "        </div>" +
                    "    </div>" +
                    "    " +
                    "    <div style='text-align: center; margin-top: 20px; color: #888; font-size: 12px;'>" +
                    "        <p>© 2025 Système de Gestion Communale. Tous droits réservés.</p>" +
                    "    </div>" +
                    "</body>" +
                    "</html>";

            helper.setText(htmlContent, true);
            mailSender.send(message);

            log.info("Email de changement de statut ({}) envoyé à : {}", statusText, email);
        } catch (MessagingException e) {
            log.error("Erreur lors de l'envoi de l'email de changement de statut à : {}", email, e);
            throw new RuntimeException("Erreur lors de l'envoi de l'email", e);
        }
    }

// Ajoutez cette méthode à votre EmailService existant
//    @Async
//    public void sendTemporaryPassword(String email, String nom, String prenom, String temporaryPassword) {
//        String subject = "Réinitialisation de votre mot de passe";
//
//        String body = String.format("""
//        Bonjour %s %s,
//
//        Vous avez demandé la réinitialisation de votre mot de passe.
//
//        Voici votre mot de passe temporaire : %s
//
//        Pour des raisons de sécurité, vous devrez changer ce mot de passe lors de votre prochaine connexion.
//
//        Si vous n'avez pas fait cette demande, veuillez contacter l'administrateur immédiatement.
//
//        Cordialement,
//        L'équipe Commune
//        """,
//                prenom,
//                nom,
//                temporaryPassword
//        );
//
//        try {
//            sendEmail(email, subject, body);
//            log.info("Email de mot de passe temporaire envoyé à : {}", email);
//        } catch (Exception e) {
//            log.error("Erreur lors de l'envoi de l'email de mot de passe temporaire à : {}", email, e);
//            throw new RuntimeException("Erreur lors de l'envoi de l'email");
//        }
//    }


}