package Commune.Dev.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalleResponseDTO {
    private Integer id;
    private String nom;
    private String description;

    // Emplacement de la salle
    private Integer marcheeId;
    private String marcheeNom;
    private Integer zoneId;
    private String zoneNom;

    // Informations sur le type d'emplacement
    private String emplacementType; // "MARCHE_DIRECT" ou "ZONE"
    private Long nbPlaces;
}
