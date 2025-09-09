package Commune.Dev.Dtos;

import Commune.Dev.Models.NotificationType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationResponse {
    private Long id;
    private String title;
    private String message;
    private NotificationType type;
    private Boolean isRead;
    private String createdByName;
    private LocalDateTime createdAt;
}
