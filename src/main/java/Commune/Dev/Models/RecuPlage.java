package Commune.Dev.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "recu_plages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecuPlage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "percepteur_id", nullable = false)
    private Long percepteurId;

    @Column(nullable = false)
    private String debut;

    @Column(nullable = false)
    private String fin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeRecu type;

    @Column
    private Integer multiplicateur;

    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
    }
}
