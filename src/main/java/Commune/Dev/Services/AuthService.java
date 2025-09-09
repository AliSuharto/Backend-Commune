package Commune.Dev.Services;

import Commune.Dev.Dtos.AuthResponse;
import Commune.Dev.Dtos.ChangePasswordRequest;
import Commune.Dev.Dtos.LoginRequest;
import Commune.Dev.Dtos.UserResponse;
import Commune.Dev.Exception.UnauthorizedException;
import Commune.Dev.Exception.UserNotFoundException;
import Commune.Dev.Models.User;
import Commune.Dev.Repositories.UserRepository;
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

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("Email ou mot de passe incorrect"));

        if (!passwordService.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Email ou mot de passe incorrect");
        }

        if (!user.getIsActive()) {
            throw new UnauthorizedException("Votre compte est désactivé");
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
}
