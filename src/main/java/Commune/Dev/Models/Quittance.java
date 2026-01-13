package Commune.Dev.Models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "Quittance")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Quittance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private Long percepteurId; //celui qui peut utiliser le quittance.

    private LocalDateTime dateUtilisation;

    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusQuittance etat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quittance_plage_id", nullable = true)
    @JsonBackReference
    private QuittancePlage quittancePlage;

    @OneToOne(mappedBy = "quittance", fetch = FetchType.LAZY)
    private Paiement paiement;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Quittance quittance = (Quittance) o;
        return id != null && Objects.equals(id, quittance.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
