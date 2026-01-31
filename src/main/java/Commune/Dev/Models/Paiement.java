package Commune.Dev.Models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "Paiement")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Paiement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String motif;

    @Enumerated(EnumType.STRING)
    private Typepaiement typePaiement;

    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le montant doit être positif")
    @Column(precision = 12, scale = 2)
    private BigDecimal montant;

     // si type de paiement est droit_place

    // si frequence mensuel par exemple,, 13 novembre 2025
    private LocalDate dateDebut;

    // 13 Decembre 2025( un mois)
    private LocalDate dateFin;

    //  si type de paiement droit_annuel, datedebut et datefin sans valeur.
    private Year anneePaye;

    //Date de paiement ici c'est la date actuel de paiement
    @PastOrPresent(message = "La date de paiement ne peut pas être dans le futur")
    private LocalDateTime datePaiement;

    @Enumerated(EnumType.STRING)
    private ModePaiement modePaiement = ModePaiement.cash;

    //Mois de paiement ici ca depoends du marchands, si
    // la dreniere mois de paiement est aout, donc meme si c'est mois de
    // Octobre qu'il a payer, le mois de paiement ce sra toujours le mois apres la
    // derniere mois qu'il a payer, donc ici c'est Septembre.
    private String moisdePaiement;

    //Si le marchands est enregistrer dans la table marchands, alors c'est
    // son nom sinon, c'est le nom de la personne quia effectuer
    // le paiement, donc le marchands ambulant.
    // Donc, s'il y a champs nom pendant l'enregistrement donc le marchands n'est pas encore
    // dans la base on attribue le champs nomMarchands par cette champs sinon on prends
    // juste le nom venant de la table marchands.
    private String nomMarchands;

    // Relations avec le marchands
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_marchand", updatable = false)
    @JsonBackReference("marchand-paiement")
    private Marchands marchand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_agent", updatable = false)
    private User agent;

    @OneToOne(optional = false)
    @JoinColumn(name = "quittance_id", nullable = false, unique = true)
    private Quittance quittance;

//    @OneToOne(mappedBy = "paiement", cascade = CascadeType.ALL)
//    private Recu recus;

    // Dans un paiement, on doit avoir l'identification du place
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_place", nullable = true)
    @JsonBackReference("paiements-places")
    private Place place;

    // toutes les paiement doivent etre effectuer dans une session,
    // donc sans session sans paiement
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private Session session;

    public enum ModePaiement {
        cash, mobile_money, autres
    }
    public enum Typepaiement{
        droit_annuel,
        droit_place,
        marchand_ambulant
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Paiement paiement = (Paiement) o;
        return id != null && Objects.equals(id, paiement.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
