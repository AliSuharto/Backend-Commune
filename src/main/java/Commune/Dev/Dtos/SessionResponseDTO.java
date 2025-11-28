package Commune.Dev.Dtos;

import Commune.Dev.Models.Session;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SessionResponseDTO {

    private Long id;
    private String nomSession;
    private String status;
    private LocalDateTime startTime;
    private Long userId;

    public static SessionResponseDTO fromEntity(Session session) {
        SessionResponseDTO dto = new SessionResponseDTO();
        dto.setId(session.getId());
        dto.setNomSession(session.getNomSession());
        dto.setStatus(session.getStatus().name());
        dto.setStartTime(session.getStartTime());
        dto.setUserId(session.getUser().getId());
        return dto;
    }
}
