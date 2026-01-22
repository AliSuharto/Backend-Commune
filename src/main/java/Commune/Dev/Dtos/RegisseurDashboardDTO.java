package Commune.Dev.Dtos;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class RegisseurDashboardDTO {
    private Integer nbrPlaces;
    private Integer nbrMarchands;
    private Integer nbrMarchandsEndette;
    private Integer nbrMarchandsAjour;
    private Integer nbrMarchandsRetardLeger;
    private Integer nbrMarchandsRetardSignificatif;
    private Integer nbrMarchandsRetardCritique;
    private Integer nbrMarchandsRetardProlonger;
    private Integer nbrQuittancesUtilise;
    private Integer nbrQuittancesLibre;
    private Integer nbrSession;
    private Integer nbrSessionValide;
    private Integer nbrSessionEnvalidation;
    private String nomSessionOuvert;
    private List<SessionRegisseur> sessions;
    private List<QuittanceRegisseur> Quittances;

    @Data
    public static class SessionRegisseur {
        private Integer id;
        private String nomSession;
        private BigDecimal montant;
        private String statut;
        private LocalDateTime dateValidation;
        private LocalDateTime dateOuverture;
        private LocalDateTime dateFermeture;
        private List<PaiementRegisseur> paiements;

        @Data
        public static class PaiementRegisseur {
            private Integer id;
            private String motif;
            private String nomAgent;
            private String montant;
            private LocalDateTime datePaiement;
            private String nomMarchands;
            private Integer idSession;
        }
    }

    @Data
    public static class QuittanceRegisseur {
        private Integer id;
        private String nom;
        private String statut;
        private Integer montant;
        private Integer idSession;
        private LocalDateTime dateUtilisation;
    }
}