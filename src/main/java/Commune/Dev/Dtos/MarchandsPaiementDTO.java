package Commune.Dev.Dtos;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class MarchandsPaiementDTO {
    private Integer id;
    private String nom;
    private String statut;
    private String activite;
    private String place;
    private String telephone;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private String cin;
    private String nif;
    private String stat;
    private String montantPlace;
    private String montantAnnuel;
    private String motifPaiementAnnuel;
    private String motifPaiementPlace;
    private String totalPaiementRestant;
    private String TotalPaiementeffectuer;
    private String frequencePaiement;
}
