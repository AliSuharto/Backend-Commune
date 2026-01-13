package Commune.Dev.Models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
//@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Marchands {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 255)
    private String nom;   //nom et prenom


    @Size(max = 255)
    private String prenom;

    @Size(max = 255)
    private String adress;

    private String description;

    @Size(max = 255)
//    @NotBlank(message = "L'activite est obligatoire")
    private String activite;

    private String NIF;

    private String STAT;

    private Boolean IsCarteGenerer;

    @NotBlank(message = "Le numéro CIN est obligatoire")
    @Size(max = 255)
    private String numCIN;

    @Size(max = 255)
    private String photo;


    @Size(max = 255)
    private String numTel1;

    @Pattern(regexp = "^[0-9+\\-\\s()]+$", message = "Format de téléphone invalide")
    @Size(max = 255)
    private String numTel2;

    @Column(name = "date_enregistrement")
    private LocalDateTime dateEnregistrement;

    // Méthode calculée pour savoir s'il est endette
    private Boolean estEndette;

    @Enumerated(EnumType.STRING)
    private StatutMarchands statut;

    @PrePersist
    protected void onCreate() {
        dateEnregistrement = LocalDateTime.now();
    }

    // Relations
    @OneToMany(mappedBy = "marchand", cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @JsonManagedReference("marchand-carte")
    private List<CarteMarchands> carteMarchands;

    @OneToMany(mappedBy = "marchand", cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @JsonManagedReference("marchand-paiement")
    private List<Paiement> paiements;

    @OneToMany(mappedBy = "marchand", cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @JsonManagedReference("marchand-contrat")
    private List<Contrat> contrats;

    @OneToMany(mappedBy = "marchands", cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @JsonManagedReference ("marchands-places")
    private List<Place> places;


}
