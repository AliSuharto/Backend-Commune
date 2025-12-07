package Commune.Dev.Models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "recu_plages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuittancePlage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne
    @JoinColumn(name = "percepteur_id")
    private User percepteur;

    @ManyToOne
    @JoinColumn(name = "controlleur_id")
    private User controlleur;


    @Column(nullable = false)
    private String debut;

    @Column(nullable = false)
    private String fin;

    private Integer multiplicateur;

    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt;

    private Integer nombreQuittance;

    private Integer QuittanceRestant;

    private String code;


    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "quittancePlage", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Quittance> quittances;

    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
    }

}
