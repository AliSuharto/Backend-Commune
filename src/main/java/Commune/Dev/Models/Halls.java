package Commune.Dev.Models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "hall",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"nom", "numero"})
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Halls {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom")
    private String nom;

    @Column(nullable =true)
    private Integer numero;

    @Column(name = "description")
    private String description;

    private String codeUnique;

    private Long NbrPlace;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marchee_id",nullable = true)
    @JsonBackReference("marchee-halls")
    private Marchee marchee;

    // Relation ManyToOne vers Zone (optionnelle)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = true)
    @JsonBackReference("zone-halls")
    private Zone zone;

    // Relation OneToMany vers Places
    @OneToMany(mappedBy = "hall", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference("hall-places")
    private List<Place> places = new ArrayList<>();

    // Méthodes utilitaires
    public void addPlace(Place place) {
        places.add(place);
        place.setHall(this);
        place.setMarchee(this.marchee); // Maintenir la cohérence
        if (this.zone != null) {
            place.setZone(this.zone); // Si le hall est dans une zone
        }
    }

    public void removePlace(Place place) {
        places.remove(place);
        place.setHall(null);
    }



    @PrePersist
    @PreUpdate
    public void generateCodeUnique() {
        String base = nom.replaceAll("\\s+", "").toUpperCase(); // Supprime espaces, met en majuscule
        if (numero != null) {
            this.codeUnique = base + "-" + String.format("%02d", numero);
        } else {
            this.codeUnique = base;
        }
    }
}

