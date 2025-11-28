package Commune.Dev.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaiementStatistiquesDTO {
    private BigDecimal montantTotal;
    private Long nombrePaiements;
    private BigDecimal montantMoyen;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
}