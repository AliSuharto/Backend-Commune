package Commune.Dev.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MarcheStatDTO {
    private Long id;
    private String nom;
    private String adresse;
    private Integer nbrPlace;
    private Integer nbrPlaceLibre;
    private Integer nbrPlaceOccupee;
    private Integer nbrHall;
    private Integer nbrZone;
    private Integer nbrMarchands;
    private Integer tauxOccupation; // en pourcentage

    // Montants globaux (en Ariary)
    private BigDecimal montantEstimeParMois;
    private BigDecimal montantPercuMoisDernier;
    private BigDecimal montantRestant;
    private Integer tauxPerception; // en pourcentage

    // Répartition détaillée par catégorie et fréquence
    private List<CategorieFrequenceDTO> repartitionPaiements;

    // Détails des halls et zones
    private List<HallDTO> halls;
    private List<ZoneDTO> zones;

    // ========== Classes internes ==========

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CategorieFrequenceDTO {
        private String categorie;              // "Catégorie A"
        private String frequence;              // "JOURNALIER", "HEBDOMADAIRE", "MENSUEL"
        private BigDecimal tarifBase;          // Le tarif de la catégorie (ex: 30000 Ar)
        private Integer nbrMarchands;          // Nombre de marchands avec cette config

        // Calculs automatiques pour estimation mensuelle
        private Double facteurConversion;      // 30 pour journalier, 4.33 pour hebdo, 1 pour mensuel
        private BigDecimal montantMensuelUnitaire;  // tarifBase × facteurConversion
        private BigDecimal montantEstimeTotal;      // nbrMarchands × montantMensuelUnitaire
        private BigDecimal montantPercu;            // Montant effectivement perçu
        private Integer tauxPerception;             // en pourcentage
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class HallDTO {
        private Long id;
        private String nom;
        private Integer nbrPlace;
        private Integer nbrPlaceLibre;
        private Integer nbrPlaceOccupee;
        private Integer nbrMarchands;
        private Integer tauxOccupation;
        private BigDecimal montantEstime;
        private BigDecimal montantPercu;
        private BigDecimal montantRestant;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ZoneDTO {
        private Long id;
        private String nom;
        private Integer nbrHalls;
        private Integer nbrPlace;
        private Integer nbrPlaceLibre;
        private Integer nbrPlaceOccupee;
        private Integer nbrMarchands;
        private Integer tauxOccupation;
        private BigDecimal montantEstimeMois;
        private BigDecimal montantPercu;
        private BigDecimal montantRestant;
    }
}