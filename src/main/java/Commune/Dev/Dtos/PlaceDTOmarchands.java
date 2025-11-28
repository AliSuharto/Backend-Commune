package Commune.Dev.Dtos;

import lombok.*;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceDTOmarchands {
    private Integer id;
    private String nom;
    private LocalDateTime dateDebutOccupation;
    private LocalDateTime dateFinOccupation;
    private String marcheeName;
    private String salleName;
    private Integer categorieId;
    private String categorieName;
    private String zoneName;
}
