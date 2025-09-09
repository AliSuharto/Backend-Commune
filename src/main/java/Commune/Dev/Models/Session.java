package Commune.Dev.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Type de session (PERCEPTEUR ou REGISSEUR)
    @Enumerated(EnumType.STRING)
    private SessionType type;

    // Utilisateur qui a ouvert la session
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Liste des marchés associés à cette session
    @ManyToMany
    @JoinTable(
            name = "session_marchees",
            joinColumns = @JoinColumn(name = "session_id"),
            inverseJoinColumns = @JoinColumn(name = "marchee_id")
    )
    private List<Marchee> marchees;

    // Date et heure d'ouverture
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    // Date et heure de fermeture
    @Column(name = "end_time")
    private LocalDateTime endTime;

    // Statut (OUVERTE, FERMEE, EN_VALIDATION, VALIDEE, REJETEE)
    @Enumerated(EnumType.STRING)
    private SessionStatus status;

    // Montant total collecté pendant la session
    @Column(name = "total_collected")
    private Double totalCollected;

    // Nombre de transactions effectuées
    @Column(name = "transaction_count")
    private Integer transactionCount;

    // Observations ou remarques
    private String notes;

    // Synchronisation hors ligne (pour percepteur)
    @Column(name = "synced")
    private Boolean synced = false;


    public enum SessionType {
        PERCEPTEUR,
        REGISSEUR
    }
    public enum SessionStatus {
        OUVERTE,
        FERMEE,
        EN_VALIDATION,
        VALIDEE,
        REJETEE
    }
}