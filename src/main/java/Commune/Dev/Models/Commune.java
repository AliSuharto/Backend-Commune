package Commune.Dev.Models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "communes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Commune {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nom;

    @Column(nullable = false)
    private String codePostal;
    private String pays;
    private String mail;
    private String Telephone;
    private String region;
    private String localisation;
}
