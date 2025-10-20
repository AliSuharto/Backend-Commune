package Commune.Dev.Dtos;

import lombok.Data;

@Data
public class MarchandDTO {
    private Integer id;
    private String nom;
    private String prenom;
    private String numCIN;
    private String numTel1;
    private String adress;
    private boolean hasPlace; // Indique si le marchand a déjà une place
}
