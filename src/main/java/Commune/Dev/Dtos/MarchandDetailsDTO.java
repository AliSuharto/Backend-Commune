package Commune.Dev.Dtos;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class MarchandDetailsDTO {
    private Integer id;
    private String nom;
    private String statut;
    private String activite;
    private String telephone;
    private String cin;
    private String nif;
    private String stat;
    private LocalDate debutContrat;

    private List<PlaceDTOmarchands> places = new ArrayList<>();
    private List<PaiementDTO> paiements = new ArrayList<>();
}
