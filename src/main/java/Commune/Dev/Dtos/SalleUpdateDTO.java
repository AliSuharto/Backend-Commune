package Commune.Dev.Dtos;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalleUpdateDTO {

    @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
    private String nom;

    @Size(max = 500, message = "La description ne doit pas dépasser 500 caractères")
    private String description;

    // Soit directement dans un marché, soit dans une zone
    private Integer marcheeId;  // Pour salle directement dans le marché  
    private Integer zoneId;     // Pour salle dans une zone
    private Boolean moveToMarche; // Flag pour indiquer si on veut déplacer vers marché direct
    private Boolean moveToZone;   // Flag pour indiquer si on veut déplacer vers zone
}
