//package Commune.Dev.Models;
//
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//@Entity
//@Table(name = "recus")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class Recu_Utilise {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(name = "percepteur_id", nullable = false)
//    private Long percepteurId;
//
//    @Column(nullable = false, unique = true)
//    private String numero;
//
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
//    private EtatRecu etat;
//
//    @Column(name = "created_at")
//    private java.time.LocalDateTime createdAt;
//
//    @PrePersist
//    protected void onCreate() {
//        createdAt = java.time.LocalDateTime.now();
//        if (etat == null) {
//            etat = EtatRecu.LIBRE;
//        }
//    }
//}
