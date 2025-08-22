package Commune.Dev.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity
@Table(name = "Salle")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Salle {

    @Id
    private Integer id;

    @Column(name = "nom")
    private String nom;

    @Column(name = "description")
    private String description;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_marchee", insertable = false, updatable = false)
    private Marchee marchee;

    @OneToMany(mappedBy = "salle", cascade = CascadeType.ALL)
    private List<Place> places;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "id_zone", insertable = false, updatable = false)
    private Zone zone;
}

