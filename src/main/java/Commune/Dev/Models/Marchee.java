package Commune.Dev.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity
@Table(name = "Marchee")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Marchee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nom")
    private String nom;

    @Column(name = "adresse")
    private String adresse;

    @Column(name = "nbrPlace")
    private Integer nbrPlace;

    // Relations
    @OneToMany(mappedBy = "marchee", cascade = CascadeType.ALL)
    private List<Place> places;

    @OneToMany(mappedBy = "marchee", cascade = CascadeType.ALL)
    private List<Salle> salles;

    @OneToMany(mappedBy = "marchee", cascade = CascadeType.ALL)
    private List<Zone> zones;
}

