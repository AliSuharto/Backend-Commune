package Commune.Dev.Dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SessionCreatedResponseDTO {
    private Long sessionId;
    private String nomSession;
    private String message;
//    private Commune.Dev.DTOs.SessionDTO session;
}
