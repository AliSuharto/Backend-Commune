package Commune.Dev.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "Zone")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Zone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nom")
    private String nom;

    @Column(name = "description")
    private String description;

    @Column(name = "id_marchee")
    private Integer idMarchee;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_marchee", insertable = false, updatable = false)
    private Marchee marchee;

    @OneToMany(mappedBy = "zone", cascade = CascadeType.ALL)
    private List<Salle> salles;

    @OneToMany(mappedBy = "zone", cascade = CascadeType.ALL)
    private List<Place> places;

}
