package Commune.Dev.Services;

import Commune.Dev.Dtos.AuthResponse;
import Commune.Dev.Dtos.ChangePasswordRequest;
import Commune.Dev.Dtos.LoginRequest;
import Commune.Dev.Dtos.UserResponse;
import Commune.Dev.Exception.UnauthorizedException;
import Commune.Dev.Exception.UserNotFoundException;
import Commune.Dev.Models.AuditAction;
import Commune.Dev.Models.User;
import Commune.Dev.Repositories.UserRepository;
import Commune.Dev.Request.ForgotPasswordRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final UserService userService;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final AuditService auditService;


    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("Email ou mot de passe incorrect"));

        if (!user.getIsActive()) {
            throw new UnauthorizedException("Votre compte est désactivé");
        }
        if (!passwordService.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Email ou mot de passe incorrect");
        }



        // Enregistrer la connexion
        userService.recordLogin(user);

        // Générer le token JWT
        String token = jwtService.generateToken((User) user);

        UserResponse userResponse = userService.convertToUserResponse(user);

        log.info("Connexion réussie pour l'utilisateur : {}", user.getEmail());

        return new AuthResponse(token, userResponse);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request, User user) {
        if (!passwordService.matches(request.getOldPassword(), user.getPassword())) {
            throw new UnauthorizedException("Ancien mot de passe incorrect");
        }

        user.setPassword(passwordService.encodePassword(request.getNewPassword()));
        user.setMustChangePassword(false);
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        // Envoyer email de confirmation de changement de mot de passe
        emailService.sendPasswordChangeConfirmation(
                user.getEmail(),
                user.getNom(),
                user.getPrenom()
        );

        log.info("Mot de passe changé pour l'utilisateur : {}", user.getEmail());
    }

    // Ajoutez cette méthode à votre AuthService existant

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        // Rechercher l'utilisateur par email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("Aucun compte ne correspond à ces informations"));

        // Vérifier que toutes les informations correspondent
        if (!user.getNom().equalsIgnoreCase(request.getNom()) ||
                !user.getPrenom().equalsIgnoreCase(request.getPrenom()) ||
                !user.getTelephone().equals(request.getTelephone())) {

            // Logger la tentative échouée pour audit de sécurité
            auditService.logAction(
                    user,
                    AuditAction.FAILED_PASSWORD_RESET,
                    user, // L'utilisateur lui-même tente l'action
                    "Tentative de réinitialisation avec informations incorrectes (nom/prénom/téléphone ne correspondent pas)"
            );

            throw new UnauthorizedException("Les informations fournies ne correspondent pas");
        }

        // Vérifier que le compte est actif
        if (!user.getIsActive()) {
            auditService.logAction(
                    user,
                    AuditAction.FAILED_PASSWORD_RESET,
                    user, // L'utilisateur lui-même tente l'action
                    "Tentative de réinitialisation sur compte désactivé"
            );

            throw new UnauthorizedException("Votre compte est désactivé");
        }

        // Générer un mot de passe temporaire (8 caractères alphanumériques)
        String temporaryPassword = passwordService.generateTemporaryPassword();

        // Encoder et sauvegarder le nouveau mot de passe
        user.setPassword(passwordService.encodePassword(temporaryPassword));
        user.setMustChangePassword(true); // Forcer le changement au prochain login
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        // Logger l'action de réinitialisation réussie avec audit détaillé
        auditService.logAction(
                user,
                AuditAction.RESET_PASSWORD,
                "password",
                "********", // On ne log jamais les mots de passe en clair
                "******** (temporaire)",
                user, // L'utilisateur lui-même effectue l'action
                "Mot de passe réinitialisé via procédure 'mot de passe oublié'. Informations validées: email, nom, prénom, téléphone"
        );

        // Logger aussi que mustChangePassword a été activé
        auditService.logAction(
                user,
                AuditAction.UPDATE_NAME,
                "mustChangePassword",
                "false",
                "true",
                user, // L'utilisateur lui-même effectue l'action
                "Flag de changement de mot de passe obligatoire activé suite à réinitialisation"
        );

        // Envoyer le mail avec le mot de passe temporaire
        emailService.sendTemporaryPassword(
                user.getEmail(),
                user.getNom(),
                user.getPrenom(),
                temporaryPassword,
                user.getRole()
        );

        log.info("Mot de passe temporaire généré pour l'utilisateur : {} via procédure 'mot de passe oublié'",
                user.getEmail());

    }





}
