package Commune.Dev.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "Contrat")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Contrat {

    @Id
    private Integer id;

    @Column(name = "id_place")
    private Integer idPlace;

    @Column(name = "id_marchand")
    private Integer idMarchand;

    @Column(name = "nom")
    private String nom;

    @Column(name = "dateOfStart")
    private LocalDateTime dateOfStart;

    @Column(name = "description")
    private String description;

    @Column(name = "categorie_id")
    private Integer categorieId;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_place", insertable = false, updatable = false)
    private Place place;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_marchand", insertable = false, updatable = false)
    private Marchands marchand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categorie_id", insertable = false, updatable = false)
    private Categorie categorie;
}
