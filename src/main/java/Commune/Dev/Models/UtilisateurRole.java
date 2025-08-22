//package Commune.Dev.Models;
//
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//
//@Entity
//@Table(name = "Utilisateur_Role")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//public class UtilisateurRole {
//
//    @EmbeddedId
//    private UtilisateurRoleId id;
//
//    // Relations
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "id_user", insertable = false, updatable = false)
//    private Utilisateurs utilisateur;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "id_role", insertable = false, updatable = false)
//    private Role role;
//}
