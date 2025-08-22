package Commune.Dev.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalleCreateDTO {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
    private String nom;

    @Size(max = 500, message = "La description ne doit pas dépasser 500 caractères")
    private String description;

    // Soit directement dans un marché, soit dans une zone (qui appartient à un marché)
    private Integer marcheeId;  // Pour salle directement dans le marché
    private Integer zoneId;     // Pour salle dans une zone

    // Validation: une salle doit être soit dans un marché, soit dans une zone, mais pas les deux
    public boolean isValid() {
        return (marcheeId != null && zoneId == null) || (marcheeId == null && zoneId != null);
    }
}

