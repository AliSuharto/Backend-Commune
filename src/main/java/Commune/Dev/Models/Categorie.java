package Commune.Dev.Models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "categorie")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Categorie {

    @Id
    @NotNull
    private Integer id;

    @Enumerated(EnumType.STRING)
    private CategorieNom nom;

    @Size(max = 10)
    private String description;

    private LocalDateTime dateCreation;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarif_id", insertable = false, updatable = false)
    private Tarif tarif;

    @OneToMany(mappedBy = "categorie", cascade = CascadeType.ALL)
    private List<Place> places;

    @OneToMany(mappedBy = "categorie", cascade = CascadeType.ALL)
    private List<Contrat> contrats;

    public enum CategorieNom {
        A, B, C, D, E, F, G
    }
}
