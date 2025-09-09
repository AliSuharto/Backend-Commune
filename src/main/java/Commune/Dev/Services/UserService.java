package Commune.Dev.Services;

import Commune.Dev.Dtos.CreateOrdonnateurRequest;
import Commune.Dev.Dtos.CreateUserRequest;
import Commune.Dev.Dtos.UpdateUserRequest;
import Commune.Dev.Dtos.UserResponse;
import Commune.Dev.Exception.BusinessException;
import Commune.Dev.Exception.UnauthorizedException;
import Commune.Dev.Exception.UserAlreadyExistsException;
import Commune.Dev.Exception.UserNotFoundException;
import Commune.Dev.Models.*;
import Commune.Dev.Repositories.CommuneRepository;
import Commune.Dev.Repositories.UserActivityRepository;
import Commune.Dev.Repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final CommuneRepository communeRepository;
    private final UserActivityRepository userActivityRepository;
    private final PasswordService passwordService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AuditService auditService;
    private final NotificationService notificationService;



    @Transactional
    public UserResponse createUser(CreateUserRequest request, User createdBy) {
        // Seul l'ORDONNATEUR peut créer des utilisateurs
        if (createdBy.getRole() != Roletype.ORDONNATEUR && createdBy.getRole() != Roletype.DIRECTEUR) {
            throw new UnauthorizedException("Seul l'ORDONNATEUR ou le DIRECTEUR peut créer des utilisateurs");
        }

        // Vérifier l'unicité de l'email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Cet email est déjà utilisé");
        }

        // Générer un mot de passe temporaire
        String temporaryPassword = passwordService.generateTemporaryPassword();

        // Créer l'utilisateur
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordService.encodePassword(temporaryPassword));
        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());
        user.setPseudo(request.getPseudo());
        user.setRole(request.getRole());
        user.setTelephone(request.getTelephone());
        user.setCreatedBy(createdBy);
        user.setMustChangePassword(true); // Obligatoire de changer le mot de passe
        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        // Créer l'activité utilisateur
        UserActivity activity = new UserActivity();
        activity.setUser(savedUser);
        userActivityRepository.save(activity);

        // Envoyer l'email avec le mot de passe temporaire (avec rôle spécifié)
        emailService.sendTemporaryPassword(
                savedUser.getEmail(),
                savedUser.getNom(),
                savedUser.getPrenom(),
                temporaryPassword,
                savedUser.getRole()
        );

        // Logger l'action
        auditService.logAction(
                savedUser,
                AuditAction.CREATE_USER,
                createdBy,
                "Utilisateur créé avec le rôle " + savedUser.getRole()
        );

        // Créer une notification pour l'utilisateur
        notificationService.createNotification(
                savedUser,
                "Compte créé",
                "Votre compte a été créé. Vous avez reçu un email avec votre mot de passe temporaire.",
                NotificationType.ACCOUNT_CREATED,
                createdBy
        );

        log.info("Utilisateur créé avec succès : {} par {}", savedUser.getEmail(), createdBy.getEmail());

        return convertToUserResponse(savedUser);
    }

    @Transactional
    public UserResponse updateUser(Long userId, UpdateUserRequest request, User updatedBy) {
        // Seul le DIRECTEUR peut modifier les autres utilisateurs
        if (updatedBy.getRole() != Roletype.DIRECTEUR) {
            throw new UnauthorizedException("Seul le DIRECTEUR peut modifier les utilisateurs");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé"));

        StringBuilder modifications = new StringBuilder();

        // Nom
        if (request.getNom() != null && !request.getNom().equals(user.getNom())) {
            auditService.logAction(user, AuditAction.UPDATE_NAME, "nom",
                    user.getNom(), request.getNom(), updatedBy, "Nom modifié");
            modifications.append("Nom changé de '").append(user.getNom())
                    .append("' à '").append(request.getNom()).append("'\n");
            user.setNom(request.getNom());
        }

        // Prénom
        if (request.getPrenom() != null && !request.getPrenom().equals(user.getPrenom())) {
            auditService.logAction(user, AuditAction.UPDATE_NAME, "prenom",
                    user.getPrenom(), request.getPrenom(), updatedBy, "Prénom modifié");
            modifications.append("Prénom changé de '").append(user.getPrenom())
                    .append("' à '").append(request.getPrenom()).append("'\n");
            user.setPrenom(request.getPrenom());
        }

        // Email
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new UserAlreadyExistsException("Cet email est déjà utilisé");
            }
            auditService.logAction(user, AuditAction.UPDATE_EMAIL, "email",
                    user.getEmail(), request.getEmail(), updatedBy, "Email modifié");
            modifications.append("Email changé de '").append(user.getEmail())
                    .append("' à '").append(request.getEmail()).append("'\n");
            user.setEmail(request.getEmail());
        }

        // Rôle
        if (request.getRole() != null && !request.getRole().equals(user.getRole())) {
            auditService.logAction(user, AuditAction.CHANGE_ROLE, "role",
                    user.getRole().toString(), request.getRole().toString(), updatedBy, "Rôle modifié");
            modifications.append("Rôle changé de '").append(user.getRole())
                    .append("' à '").append(request.getRole()).append("'\n");
            user.setRole(request.getRole());
        }

        // Téléphone
        if (request.getTelephone() != null && !request.getTelephone().equals(user.getTelephone())) {
            modifications.append("Téléphone modifié\n");
            user.setTelephone(request.getTelephone());
        }

        // Statut actif/inactif
        if (request.getIsActive() != null && !request.getIsActive().equals(user.getIsActive())) {
            AuditAction action = request.getIsActive() ? AuditAction.ENABLE_ACCOUNT : AuditAction.DISABLE_ACCOUNT;
            String desc = request.getIsActive() ? "Compte activé" : "Compte désactivé";

            auditService.logAction(user, action, "isActive",
                    user.getIsActive().toString(), request.getIsActive().toString(), updatedBy, desc);

            modifications.append("Compte ").append(request.getIsActive() ? "activé" : "désactivé").append("\n");
            user.setIsActive(request.getIsActive());
            
            // Envoyer email de notification de changement de statut
            emailService.sendAccountStatusNotification(
                    user.getEmail(),
                    user.getNom(),
                    user.getPrenom(),
                    request.getIsActive()
            );
        }

        user.setUpdatedAt(LocalDateTime.now());
        User savedUser = userRepository.save(user);

        // Envoyer notification et email si des modifications ont été faites
        if (modifications.length() > 0) {
            // Créer une notification
            notificationService.createNotification(
                    user,
                    "Profil modifié",
                    "Votre profil a été modifié par le directeur:\n" + modifications.toString(),
                    NotificationType.PROFILE_UPDATED,
                    updatedBy
            );

            // Envoyer un email
            emailService.sendAccountModificationNotification(
                    user.getEmail(),
                    user.getNom(),
                    user.getPrenom(),
                    modifications.toString()
            );
        }

        log.info("Utilisateur {} modifié par {}", savedUser.getEmail(), updatedBy.getEmail());

        return convertToUserResponse(savedUser);
    }

    @Transactional
    public void deleteUser(Long userId, User deletedBy) {
        // Seul le DIRECTEUR peut supprimer
        if (deletedBy.getRole() != Roletype.DIRECTEUR) {
            throw new UnauthorizedException("Seul le DIRECTEUR peut supprimer les utilisateurs");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé"));

        // Vérifier l'activité de l'utilisateur
        UserActivity activity = userActivityRepository.findByUser(user).orElse(null);

        if (activity != null && activity.getHasUsedApp()) {
            // Suppression logique : désactiver seulement
            user.setIsActive(false);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);

            // Logger et notifier
            auditService.logAction(user, AuditAction.DISABLE_ACCOUNT, deletedBy,
                    "Compte désactivé (suppression logique - utilisateur avait utilisé l'app)");

            notificationService.createNotification(
                    user,
                    "Compte désactivé",
                    "Votre compte a été désactivé par le directeur.",
                    NotificationType.ACCOUNT_DISABLED,
                    deletedBy
            );

            log.info("Utilisateur {} désactivé (suppression logique) par {}", user.getEmail(), deletedBy.getEmail());
        } else {
            // Suppression physique
            auditService.logAction(user, AuditAction.DELETE_ACCOUNT, deletedBy,
                    "Utilisateur supprimé (suppression physique - n'avait jamais utilisé l'app)");

            userRepository.delete(user);
            log.info("Utilisateur {} supprimé physiquement par {}", user.getEmail(), deletedBy.getEmail());
        }
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé"));
        return convertToUserResponse(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé"));
    }

    @Transactional
    public void recordLogin(User user) {
        UserActivity activity = userActivityRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Activité utilisateur non trouvée"));

        LocalDateTime now = LocalDateTime.now();

        if (activity.getFirstLogin() == null) {
            activity.setFirstLogin(now);
        }

        activity.setLastLogin(now);
        activity.setHasUsedApp(true);
        activity.setLoginCount(activity.getLoginCount() + 1);

        userActivityRepository.save(activity);
        log.info("Connexion enregistrée pour l'utilisateur : {}", user.getEmail());
    }

    public UserResponse convertToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setNom(user.getNom());
        response.setPrenom(user.getPrenom());
        response.setPseudo(user.getPseudo());
        response.setPhotoUrl(user.getPhotoUrl());
        response.setRole(user.getRole());
        response.setIsActive(user.getIsActive());
        response.setMustChangePassword(user.getMustChangePassword());
        response.setTelephone(user.getTelephone());
        response.setCreatedByName(user.getCreatedBy() != null ?
                user.getCreatedBy().getNom() + " " + user.getCreatedBy().getPrenom() : null);
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());

        return response;
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
    }

}

