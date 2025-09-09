package Commune.Dev.Repositories;

import Commune.Dev.Models.NotificationType;
import Commune.Dev.Models.User;
import Commune.Dev.Models.UserNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {
    List<UserNotification> findByUserOrderByCreatedAtDesc(User user);
    List<UserNotification> findByUserAndIsReadOrderByCreatedAtDesc(User user, Boolean isRead);
    List<UserNotification> findByType(NotificationType type);

    @Query("SELECT COUNT(n) FROM UserNotification n WHERE n.user = :user AND n.isRead = false")
    long countUnreadByUser(@Param("user") User user);
}
