package Commune.Dev.Dtos;

import Commune.Dev.Models.Categorie.CategorieNom;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategorieResponseDTO {
    private Integer id;
    private CategorieNom nom;
    private String description;
    private BigDecimal montant;
    private LocalDateTime dateCreation;
}
