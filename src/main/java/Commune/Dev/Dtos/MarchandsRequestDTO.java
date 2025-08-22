package Commune.Dev.Dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MarchandsRequestDTO {

    @NotNull(message = "L'ID est obligatoire")
    private Integer id;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 255, message = "Le nom ne peut pas dépasser 255 caractères")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 255, message = "Le prénom ne peut pas dépasser 255 caractères")
    private String prenom;

    @NotBlank(message = "Le numéro CIN est obligatoire")
    @Size(max = 255, message = "Le numéro CIN ne peut pas dépasser 255 caractères")
    private String numCIN;

    private LocalDateTime dateDelivrance;

    @Size(max = 255, message = "Le nom de fichier photo ne peut pas dépasser 255 caractères")
    private String photo;

    @Pattern(regexp = "^[0-9+\\-\\s()]*$", message = "Format de téléphone invalide")
    @Size(max = 255, message = "Le numéro de téléphone ne peut pas dépasser 255 caractères")
    private String numTel1;

    @Pattern(regexp = "^[0-9+\\-\\s()]*$", message = "Format de téléphone invalide")
    @Size(max = 255, message = "Le numéro de téléphone ne peut pas dépasser 255 caractères")
    private String numTel2;
}
