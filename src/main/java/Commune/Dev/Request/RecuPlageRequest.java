package Commune.Dev.Request;

import Commune.Dev.Models.TypeRecu;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RecuPlageRequest {

    @NotNull(message = "L'ID du percepteur est obligatoire")
    private Long percepteurId;

    @NotBlank(message = "Le début est obligatoire")
    private String debut;

    @NotBlank(message = "La fin est obligatoire")
    private String fin;

    @NotNull(message = "Le type est obligatoire")
    private TypeRecu type;

    @Min(value = 0, message = "Le multiplicateur doit être positif")
    private Integer multiplicateur;
}