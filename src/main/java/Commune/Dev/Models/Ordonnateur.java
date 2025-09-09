package Commune.Dev.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "ordonnateur")
public class Ordonnateur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;



    private String prenom;



    @Column(unique = true, nullable = false)
    private String email;



    @Column(nullable = false)
    private String password; // hashée

    private boolean enabled = true;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant createdAt = Instant.now();

    // getters / setters / constructors
}

