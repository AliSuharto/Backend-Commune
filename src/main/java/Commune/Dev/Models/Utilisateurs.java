package Commune.Dev.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "Utilisateurs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Utilisateurs {

    @Id
    private Integer id;

    @Column(name = "nom")
    private String nom;

    @Column(name = "prenom")
    private String prenom;

    @Column(name = "pseudo")
    private String pseudo;

    @Column(name = "num_CIN")
    private String numCIN;

    @Column(name = "date_de_delivrance")
    private LocalDateTime dateDeDelivrance;

    @Column(name = "last_time_connect")
    private LocalDateTime lastTimeConnect;

    @Column(name = "isConnect")
    private Boolean isConnect;

    @Column(name = "num_tel")
    private String numTel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Roletype role;

    private Boolean isactive;


    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_permissions",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();


    // Relations
//    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL)
//    private List<UtilisateurRole> utilisateurRoles;

    @OneToMany(mappedBy = "agent", cascade = CascadeType.ALL)
    private List<Paiement> paiements;

    @OneToMany(mappedBy = "percepteur", cascade = CascadeType.ALL)
    private List<Quittance> quittances;
}
