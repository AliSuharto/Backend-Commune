package Commune.Dev.Models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Zone")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Zone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom")
    private String nom;

    @Column(name = "description")
    private String description;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marchee_id", nullable = false)
    @JsonBackReference("marchee-zones")
    private Marchee marchee;


    @ManyToMany(mappedBy = "zones")
    @JsonIgnore
    private List<User> users;

    // Relations OneToMany
    @OneToMany(mappedBy = "zone", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference("zone-halls")
    private List<Halls> halls = new ArrayList<>();

    @OneToMany(mappedBy = "zone", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference("zone-places")
    private List<Place> places = new ArrayList<>();

    // Méthodes utilitaires

    public void addHall(Halls hall) {
        halls.add(hall);
        hall.setZone(this);
        hall.setMarchee(this.marchee); // Maintenir la cohérence
    }

    public void removeHall(Halls hall) {
        halls.remove(hall);
        hall.setZone(null);
    }

    public void addPlace(Place place) {
        places.add(place);
        place.setZone(this);
        place.setMarchee(this.marchee); // Maintenir la cohérence
    }

    public void removePlace(Place place) {
        places.remove(place);
        place.setZone(null);
    }
}

