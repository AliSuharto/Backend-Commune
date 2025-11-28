package Commune.Dev.Dtos;

import lombok.Data;

@Data
public class PlaceAttrDTO {
        private Integer id;
        private String nom;
        private String adresse;
        private Boolean isOccuped;
        private String zoneName;
        private String hallName;
        private String marcheeName;
        private MarchandDTO marchand; // Marchand actuel si occupée

}
