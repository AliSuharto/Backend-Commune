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

    @Size(max = 10, message = "La description ne peut pas dépasser 10 caractères")
    private String description;

    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le montant doit être positif")
    private BigDecimal montant;
}
