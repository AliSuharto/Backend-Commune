package Commune.Dev.Dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// DTO pour la réponse d'un paiement
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaiementDTO {
    private Integer id;
    private BigDecimal montant;
    private LocalDateTime datePaiement;
    private String motif;
    private String modePaiement;
    private String moisdePaiement;
    private String nomMarchands;
    private Integer idMarchand;
    private Integer idAgent;
    private String nomAgent;
    private Integer idPlace;
    private String nomPlace;
    private Integer sessionId;
    private String recuNumero;
    private Integer quittanceId;
    private LocalDateTime dernierePaiement;
}

// DTO pour créer un paiement unique

// DTO pour créer plusieurs paiements en même temps


// DTO pour les statistiques de paiement (bonus)

