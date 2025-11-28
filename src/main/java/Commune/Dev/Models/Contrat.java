package Commune.Dev.Models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Contrat")
@Data
@NoArgsConstructor
@AllArgsConstructor

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Contrat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_place")
    private Integer idPlace;

    @Column(name = "id_marchand")
    private Integer idMarchand;

    private Boolean isActif;

    @Column(name = "nom")
    private String nom;


    @Column(name = "dateOfStart")
    private LocalDate dateOfStart;

    @Column(name = "dateOfCreation")
    private LocalDateTime dateOfCreation;

    @Column(name = "description")
    private String description;

    @Column(name = "dateOfEnd")
    private LocalDateTime dateOfEnd;

    @Column(name = "droitAnnuel_id")
    private Integer droitAnnuelId;
//
    @Column(name = "categorie_id")
    private Integer categorieId;
    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_place", insertable = false, updatable = false)
    private Place place;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_marchand", insertable = false, updatable = false)
    @JsonBackReference("marchand-contrat")
    private Marchands marchand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categorie_id", insertable = false, updatable = false)
    private Categorie categorie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "droitAnnuel_id", insertable = false, updatable = false)
    private DroitAnnuel droitAnnuel;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequence_paiement")
    private FrequencePaiement frequencePaiement;

}
