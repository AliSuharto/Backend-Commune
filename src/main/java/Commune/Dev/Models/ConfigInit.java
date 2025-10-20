package Commune.Dev.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConfigInit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated
    private ModeCalculPaiement modeCalculPaiement;

    @Column(name = "montant_detention_place", nullable = false, precision = 10, scale = 2)
    private BigDecimal montantDetentionPlace;

    @Column(name = "actif")
    private Boolean actif = true;

    //    date d'envoie d'alerte de retard de [paiement] (1er alerte)
    private Integer alertDate;

    // date d'envoie d'alerte de retard de [paiement] (2em alerte)

    private Integer criticDate;

    private LocalDateTime dateCreation;

    private LocalDateTime derniereModif;


    public enum ModeCalculPaiement {
        MOIS_ATTRIBUTION("Payer dès le mois d'attribution"),
        TRENTE_JOURS("Premier paiement 30 jours après attribution"),
        MOIS_SUIVANT("Payer à partir du mois suivant");

        private String description;

        ModeCalculPaiement(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }


}