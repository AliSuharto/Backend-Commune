package Commune.Dev.Controller;

import Commune.Dev.Dtos.ApiResponse;
import Commune.Dev.Dtos.NotificationResponse;
import Commune.Dev.Models.User;
import Commune.Dev.Models.UserNotification;
import Commune.Dev.Services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getMyNotifications(
            @AuthenticationPrincipal User user) {
        List<UserNotification> notifications = notificationService.getUserNotifications(user);
        List<NotificationResponse> responses = notifications.stream()
                .map(this::convertToNotificationResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUnreadNotifications(
            @AuthenticationPrincipal User user) {
        List<UserNotification> notifications = notificationService.getUnreadNotifications(user);
        List<NotificationResponse> responses = notifications.stream()
                .map(this::convertToNotificationResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/unread/count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(@AuthenticationPrincipal User user) {
        long count = notificationService.getUnreadCount(user);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<String>> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        notificationService.markAsRead(id, user);
        return ResponseEntity.ok(ApiResponse.success("Notification marquée comme lue", null));
    }

    private NotificationResponse convertToNotificationResponse(UserNotification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setTitle(notification.getTitle());
        response.setMessage(notification.getMessage());
        response.setType(notification.getType());
        response.setIsRead(notification.getIsRead());
        response.setCreatedByName(notification.getCreatedBy() != null ?
                notification.getCreatedBy().getNom() + " " + notification.getCreatedBy().getPrenom() : null);
        response.setCreatedAt(notification.getCreatedAt());
        return response;
    }
}

