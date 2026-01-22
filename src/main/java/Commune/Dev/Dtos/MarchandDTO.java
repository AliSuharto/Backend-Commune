package Commune.Dev.Dtos;

import Commune.Dev.Models.Place;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarchandDTO {
    private Integer id;
    private String nom;
    private String prenom;
    private String adress;
    private String description;
    private String activite;
    private String numCIN;
    private String photo;
    private String numTel1;
    private String numTel2;
    private LocalDateTime dateEnregistrement;
    private String stat;
    private String nif;
    private Boolean estEndette;
    private String Categorie;
    private Boolean hasPlace;
    private List<PlaceDTOmarchands> places;
}
