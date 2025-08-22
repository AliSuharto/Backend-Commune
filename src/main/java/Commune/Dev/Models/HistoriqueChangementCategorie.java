package Commune.Dev.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "Histoique_changementCategorie")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoriqueChangementCategorie {

    @Id
    private Integer id;

    @Column(name = "id_place")
    private Integer idPlace;

    @Column(name = "id_categorie")
    private Integer idCategorie;

    @Column(name = "description")
    private String description;

    @Column(name = "date_changement")
    private LocalDateTime dateChangement;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_place", insertable = false, updatable = false)
    private Place place;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categorie", insertable = false, updatable = false)
    private Categorie categorie;
}

