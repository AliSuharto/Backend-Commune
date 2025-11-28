package Commune.Dev.Models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Marchee")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Marchee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom")
    private String nom;

    @Column(name = "adresse")
    private String adresse;
    @Column(name = "nbrPlace")
    private Integer nbrPlace;
    private String description;

    private Boolean isActif;

    @ManyToMany(mappedBy = "marchees")
    @JsonManagedReference("marchee-user")
    private List<User> users;

    // Relations
    @OneToMany(mappedBy = "marchee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference("marchee-zones")
    private List<Zone> zones = new ArrayList<>();

    @OneToMany(mappedBy = "marchee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference("marchee-halls")
    private List<Halls> halls = new ArrayList<>();

    @OneToMany(mappedBy = "marchee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference("marchee-place")
    private List<Place> places = new ArrayList<>();

    // Méthodes utilitaires pour maintenir la cohérence
    public void addZone(Zone zone) {
        zones.add(zone);
        zone.setMarchee(this);
    }

    public void removeZone(Zone zone) {
        zones.remove(zone);
        zone.setMarchee(null);
    }

    public void addHall(Halls hall) {
        halls.add(hall);
        hall.setMarchee(this);
    }

    public void removeHall(Halls hall) {
        halls.remove(hall);
        hall.setMarchee(null);
    }

    public void addPlace(Place place) {
        places.add(place);
        place.setMarchee(this);
    }

    public void removePlace(Place place) {
        places.remove(place);
        place.setMarchee(null);
    }
}

