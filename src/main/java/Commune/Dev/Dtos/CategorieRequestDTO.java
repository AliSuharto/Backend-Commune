package Commune.Dev.Dtos;

import Commune.Dev.Models.Categorie;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CategorieRequestDTO {

    @NotNull(message = "Le nom de la catégorie est obligatoire")
    private Categorie.CategorieNom nom;

    private Integer idCreateur;

    @Size(max = 255)
    private String description;

    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le montant doit être positif")
    private BigDecimal montant;
}
