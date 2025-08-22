package Commune.Dev.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Place")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nom")
    private String nom;

    @Column(name = "adresse")
    private String adresse;

    @Column(name = "isOccuped")
    private Boolean isOccuped;

    @Column(name = "date_debut_occupation")
    private LocalDateTime dateDebutOccupation;

    @Column(name = "date_fin_occupation")
    private LocalDateTime dateFinOccupation;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    //@JoinColumn(name = "id_marchee", insertable = false, updatable = false)
    @JoinColumn(name = "id_marchee")
    private Marchee marchee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_marchands")
    private Marchands marchands;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_zone")
    private Zone zone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_hall")
    private Salle salle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categorie_id")
    private Categorie categorie;

    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL)
    private List<CarteMarchands> carteMarchands;

    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL)
    private List<HistoriqueChangementCategorie> historiqueChangements;

}

