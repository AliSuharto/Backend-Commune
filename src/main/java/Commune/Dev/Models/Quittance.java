package Commune.Dev.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity
@Table(name = "Quittance")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Quittance {

    @Id
    private Integer id;

    @Column(name = "description")
    private String description;

    @Column(name = "numero de debut")
    private String numeroDeDebut;

    @Column(name = "numero de fin")
    private String numeroDeFin;

    @Column(name = "quittance_restant")
    private String quittanceRestant;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "percepteur_id", insertable = false, updatable = false)
    private Utilisateurs percepteur;

    @OneToMany(mappedBy = "quittance", cascade = CascadeType.ALL)
    private List<Paiement> paiements;

    @OneToMany(mappedBy = "quittance", cascade = CascadeType.ALL)
    private List<Recu> recus;
}
