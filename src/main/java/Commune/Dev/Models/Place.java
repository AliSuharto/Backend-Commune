package Commune.Dev.Models;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
    @JoinColumn(name = "marchee_id", nullable = true)
    @JsonBackReference("marchee-places")
    private Marchee marchee;

    // Relation ManyToOne vers Zone (optionnelle)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = true)
    @JsonBackReference("zone-places")
    private Zone zone;

    // Relation ManyToOne vers Hall (optionnelle)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hall_id", nullable = true)
    @JsonBackReference("hall-places")
    private Halls hall;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categorie_id", nullable = true)
    private Categorie categorie;

    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL)
    private List<CarteMarchands> carteMarchands;

    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL)
    private List<HistoriqueChangementCategorie> historiqueChangements;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_marchands", nullable = true)
    private Marchands marchands;
}

