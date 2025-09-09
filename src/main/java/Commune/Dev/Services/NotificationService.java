package Commune.Dev.Services;

import Commune.Dev.Models.NotificationType;
import Commune.Dev.Models.User;
import Commune.Dev.Models.UserNotification;
import Commune.Dev.Repositories.UserNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final UserNotificationRepository notificationRepository;

    public void createNotification(User user, String title, String message,
                                   NotificationType type, User createdBy) {
        UserNotification notification = new UserNotification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setCreatedBy(createdBy);
        notification.setCreatedAt(LocalDateTime.now());

        notificationRepository.save(notification);
        log.info("Notification créée pour l'utilisateur : {}", user.getEmail());
    }

    public List<UserNotification> getUserNotifications(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public List<UserNotification> getUnreadNotifications(User user) {
        return notificationRepository.findByUserAndIsReadOrderByCreatedAtDesc(user, false);
    }

    public long getUnreadCount(User user) {
        return notificationRepository.countUnreadByUser(user);
    }

    public void markAsRead(Long notificationId, User user) {
        UserNotification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification non trouvée"));

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Non autorisé");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }
}
