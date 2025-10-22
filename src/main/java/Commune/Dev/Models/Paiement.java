package Commune.Dev.Models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Paiement")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Paiement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le montant doit être positif")
    @Column(precision = 10, scale = 5)
    private BigDecimal montant;

    @PastOrPresent(message = "La date de paiement ne peut pas être dans le futur")
    private LocalDateTime datePaiement;

    @Enumerated(EnumType.STRING)
    private ModePaiement modePaiement = ModePaiement.cash;

    private String moisdePaiement;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_marchand", insertable = false, updatable = false)
    @JsonBackReference("marchand-paiement")
    private Marchands marchand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_agent", insertable = false, updatable = false)
    private Utilisateurs agent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quittance_id", insertable = false, updatable = false)
    private Quittance quittance;

    @OneToMany(mappedBy = "paiement", cascade = CascadeType.ALL)
    private List<Recu> recus;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private Session session;

    public enum ModePaiement {
        cash, mobile_money, autres
    }
}
