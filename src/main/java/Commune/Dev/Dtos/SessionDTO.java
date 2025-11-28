package Commune.Dev.Dtos;

import Commune.Dev.Models.Paiement;
import Commune.Dev.Models.Session.SessionStatus;
import Commune.Dev.Models.Session.SessionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SessionDTO {

    private Long id;

    // Nom de la session au format: id-sessionId-userId-date
    private String nomSession;

    // Informations de l'utilisateur
    private UserSummaryDTO user;

    // Type de session
    private SessionType type;

    // Date de la session
    private LocalDateTime dateSession;

    // Statut
    private SessionStatus status;

    // Montant total collecté
    private BigDecimal montantCollecte;

    // Est validée
    private Boolean isValid;

    // Liste des paiements
    private List<PaiementDTO> paiements;

    // Notes
    private String notes;
}
