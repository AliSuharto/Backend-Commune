package Commune.Dev.Models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "categorie")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Categorie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(unique = true)
    private CategorieNom nom;

    @Size(max = 10)
    private String description;

    private LocalDateTime dateCreation;

    private BigDecimal montant;

    // Relations


    @OneToMany(mappedBy = "categorie", cascade = CascadeType.ALL)
    private List<Place> places;

    @OneToMany(mappedBy = "categorie", cascade = CascadeType.ALL)
    private List<Contrat> contrats;

    public enum CategorieNom {
        A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z
    }
}
