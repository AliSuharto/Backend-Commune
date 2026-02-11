package Commune.Dev.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String from;

    @Autowired
    // Injection par constructeur explicite
    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtpEmail(String to, String code) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setFrom(from);
        msg.setSubject("Code de vérification");
        msg.setText("Votre code de vérification : " + code + "\nValable pendant 10 minutes.");
        mailSender.send(msg);
    }

    public void sendAccountDetails(String to, String username, String password) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Détails de votre compte");
        message.setText("Votre compte a été créé avec succès.\n"
                + "Identifiant: " + username + "\n"
                + "Mot de passe temporaire: " + password + "\n"
                + "Veuillez changer votre mot de passe après la première connexion.");

        mailSender.send(message);
    }


}


