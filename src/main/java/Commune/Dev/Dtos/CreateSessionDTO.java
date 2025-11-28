package Commune.Dev.Dtos;
import Commune.Dev.Models.Session.SessionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateSessionDTO {

//    @NotNull(message = "Le type de session est obligatoire")
//    private SessionType type;

    @NotNull(message = "L'ID de l'utilisateur est obligatoire")
    private Long userId;
    private String nomSession;
}

