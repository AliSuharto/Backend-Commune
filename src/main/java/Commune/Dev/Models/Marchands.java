package Commune.Dev.Models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Marchands")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Marchands {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 255)
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 255)
    private String prenom;

    @Size(max = 255)
    private String adress;

    @Size(max = 255)
    private String description;

    @NotBlank(message = "Le numéro CIN est obligatoire")
    @Size(max = 255)
    private String numCIN;

    @Size(max = 255)
    private String photo;

    @Pattern(regexp = "^[0-9+\\-\\s()]+$", message = "Format de téléphone invalide")
    @Size(max = 255)
    private String numTel1;

    @Pattern(regexp = "^[0-9+\\-\\s()]+$", message = "Format de téléphone invalide")
    @Size(max = 255)
    private String numTel2;

    @Column(name = "date_enregistrement")
    private LocalDateTime dateEnregistrement;

    // Méthode calculée pour savoir s'il est endetté
    @Transient
    private Boolean estEndette;

    @PrePersist
    protected void onCreate() {
        dateEnregistrement = LocalDateTime.now();
    }

    // Relations
    @OneToMany(mappedBy = "marchand", cascade = CascadeType.ALL)
    private List<CarteMarchands> carteMarchands;

    @OneToMany(mappedBy = "marchand", cascade = CascadeType.ALL)
    private List<Paiement> paiements;

    @OneToMany(mappedBy = "marchand", cascade = CascadeType.ALL)
    private List<Contrat> contrats;

    @OneToMany(mappedBy = "marchands", cascade = CascadeType.ALL)
    private List<Place> places;
}
