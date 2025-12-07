package Commune.Dev.Request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class QuittancePlageRequest {

    @NotNull(message = "L'ID du percepteur est obligatoire")
    private Long percepteurId;

    @NotBlank(message = "Le début est obligatoire")
    private String debut;

    @NotNull(message = "L'ID du Createur  Obligatoire")
    private Long controlleurId;

    @NotBlank(message = "La fin est obligatoire")
    private String fin;

    private String code;

    @Min(value = 0, message = "Le multiplicateur doit être positif")
    private Integer multiplicateur;
}