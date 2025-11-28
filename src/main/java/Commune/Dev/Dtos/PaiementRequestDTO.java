package Commune.Dev.Dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaiementRequestDTO {

    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le montant doit être positif")
    private BigDecimal montant;

    @NotNull(message = "Le mode de paiement est obligatoire")
    private String modePaiement; // cash, mobile_money, autres

//    @NotNull(message = "Le mois de paiement est obligatoire")
//    @NotBlank(message = "Le mois de paiement ne peut pas être vide")
    private String moisdePaiement;

    // ID du marchand si enregistré, sinon null
    private Integer idMarchand;

    // Nom du marchand ambulant si non enregistré
    private String nomMarchands;

    @NotNull(message = "L'ID de l'agent est obligatoire")
    private Long idAgent;

    // Place peut être nullable selon votre modèle
    private Integer idPlace;

    @NotNull(message = "L'ID de la session est obligatoire")
    private Long sessionId;

    private Integer quittanceId;
}

