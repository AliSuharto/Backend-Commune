package Commune.Dev.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "Tarif")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tarif {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_periodicite")
    private TypePeriodicite typePeriodicite;

    @Column(name = "montant", precision = 10, scale = 2)
    private BigDecimal montant;

    // Relations
    @OneToMany(mappedBy = "tarif", cascade = CascadeType.ALL)
    private List<Categorie> categories;

    public enum TypePeriodicite {
        Journalier, Mensuel, Hebdomadaire
    }
}
