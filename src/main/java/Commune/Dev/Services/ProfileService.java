package Commune.Dev.Services;

import Commune.Dev.Dtos.UpdateProfileRequest;
import Commune.Dev.Dtos.UserResponse;
import Commune.Dev.Exception.UserAlreadyExistsException;
import Commune.Dev.Models.AuditAction;
import Commune.Dev.Models.NotificationType;
import Commune.Dev.Models.User;
import Commune.Dev.Repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {

    private final UserRepository userRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final UserService userService;

    @Transactional
    public UserResponse updateProfile(UpdateProfileRequest request, User user) {
        boolean hasChanges = false;

        // Mise à jour du pseudo
        if (request.getPseudo() != null && !request.getPseudo().equals(user.getPseudo())) {
            if (userRepository.existsByPseudo(request.getPseudo())) {
                throw new UserAlreadyExistsException("Ce pseudo est déjà utilisé");
            }

            auditService.logAction(user, AuditAction.UPDATE_PROFILE, "pseudo",
                    user.getPseudo(), request.getPseudo(), user, "Pseudo modifié par l'utilisateur");

            user.setPseudo(request.getPseudo());
            hasChanges = true;
        }

        // Mise à jour de la photo
        if (request.getPhotoUrl() != null && !request.getPhotoUrl().equals(user.getPhotoUrl())) {
            auditService.logAction(user, AuditAction.UPDATE_PROFILE, "photoUrl",
                    user.getPhotoUrl(), request.getPhotoUrl(), user, "Photo modifiée par l'utilisateur");

            user.setPhotoUrl(request.getPhotoUrl());
            hasChanges = true;
        }

        if (hasChanges) {
            user.setUpdatedAt(LocalDateTime.now());
            user = userRepository.save(user);

            notificationService.createNotification(
                    user,
                    "Profil mis à jour",
                    "Votre profil a été mis à jour avec succès.",
                    NotificationType.PROFILE_UPDATED,
                    user
            );

            log.info("Profil mis à jour par l'utilisateur : {}", user.getEmail());
        }

        return userService.convertToUserResponse(user);
    }
}
