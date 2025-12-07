package Commune.Dev.Models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DroitAnnuel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

//    @Enumerated(EnumType.STRING)
//    @Column(unique = true)
//    private String nom;

    @Size(max = 255)
    private String description;

    private LocalDateTime dateCreation;

    private BigDecimal montant;

    // Relations

    @OneToMany(mappedBy = "droitAnnuel", cascade = CascadeType.ALL)
    @JsonBackReference("droit-places")
    private List<Place> places;

}
