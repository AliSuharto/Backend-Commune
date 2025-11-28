package Commune.Dev.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarcheeResponseDTO {
    private Long id;
    private String nom;
    private String adresse;
    private Long totalPlaces;
    private Long placesOccupees;
    private double tauxOccupation;
}
