//package Commune.Dev.Models;
//
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import java.util.List;
//
//@Entity
//@Table(name = "Role")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//public class Role {
//
//    @Id
//    private Integer id;
//
//    @Column(name = "nom")
//    private String nom;
//
//    @Column(name = "description")
//    private String description;
//
//    // Relations
//    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL)
//    private List<UtilisateurRole> utilisateurRoles;
//}
//
